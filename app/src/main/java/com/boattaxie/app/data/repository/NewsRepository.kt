package com.boattaxie.app.data.repository

import android.content.Context
import com.boattaxie.app.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class NewsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    // Bocas del Toro coordinates for weather
    private val bocasLatitude = 9.3403
    private val bocasLongitude = -82.2419
    
    // OpenWeatherMap API key - free tier
    // Users can get their own at https://openweathermap.org/api
    private val weatherApiKey = "demo" // Replace with actual key for production
    
    /**
     * Fetch all news from multiple sources
     */
    suspend fun fetchAllNews(): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
        try {
            val allArticles = mutableListOf<NewsArticle>()
            
            // Fetch from Bocas Breeze
            fetchRssFeed(NewsSource.BOCAS_BREEZE).getOrNull()?.let { articles ->
                allArticles.addAll(articles)
            }
            
            // Fetch from Panama News
            fetchRssFeed(NewsSource.PANAMA_NEWS).getOrNull()?.let { articles ->
                allArticles.addAll(articles)
            }
            
            // Sort by publish date, newest first
            val sortedArticles = allArticles.sortedByDescending { it.publishedAt }
            
            Result.success(sortedArticles)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Error fetching news: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Fetch RSS feed from a news source
     */
    suspend fun fetchRssFeed(source: NewsSource): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(source.url)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "OmniMap/1.0")
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("HTTP error: $responseCode"))
            }
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            
            val articles = parseRssFeed(response, source)
            Result.success(articles)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Error fetching RSS from ${source.displayName}: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse RSS XML feed into NewsArticle list
     */
    private fun parseRssFeed(xml: String, source: NewsSource): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))
            
            var eventType = parser.eventType
            var currentArticle: MutableMap<String, String>? = null
            var currentTag = ""
            var inItem = false
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item" || currentTag == "entry") {
                            inItem = true
                            currentArticle = mutableMapOf()
                        }
                        // Check for media:content or enclosure for images
                        if (inItem && (currentTag == "enclosure" || currentTag == "media:content" || currentTag == "content")) {
                            val imageUrl = parser.getAttributeValue(null, "url")
                            if (imageUrl != null && (imageUrl.contains(".jpg") || imageUrl.contains(".png") || imageUrl.contains(".jpeg"))) {
                                currentArticle?.put("image", imageUrl)
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inItem && currentArticle != null && parser.text.isNotBlank()) {
                            when (currentTag) {
                                "title" -> currentArticle["title"] = parser.text.trim()
                                "description", "summary" -> {
                                    var desc = parser.text.trim()
                                    // Strip HTML tags
                                    desc = desc.replace(Regex("<[^>]*>"), "")
                                    // Decode HTML entities
                                    desc = desc.replace("&amp;", "&")
                                        .replace("&lt;", "<")
                                        .replace("&gt;", ">")
                                        .replace("&quot;", "\"")
                                        .replace("&#39;", "'")
                                        .replace("&nbsp;", " ")
                                    currentArticle["description"] = desc
                                }
                                "link" -> currentArticle["link"] = parser.text.trim()
                                "pubDate", "published", "updated" -> currentArticle["pubDate"] = parser.text.trim()
                                "author", "dc:creator" -> currentArticle["author"] = parser.text.trim()
                                "content:encoded" -> {
                                    val content = parser.text
                                    // Try to extract image from content
                                    val imgMatch = Regex("src=[\"']([^\"']+\\.(jpg|jpeg|png|gif))[\"']").find(content)
                                    imgMatch?.groupValues?.get(1)?.let { imgUrl ->
                                        if (currentArticle?.get("image") == null) {
                                            currentArticle?.put("image", imgUrl)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" || parser.name == "entry") {
                            inItem = false
                            currentArticle?.let { article ->
                                val title = article["title"] ?: ""
                                if (title.isNotEmpty()) {
                                    articles.add(
                                        NewsArticle(
                                            id = "${source.name}_${articles.size}",
                                            title = title,
                                            description = article["description"] ?: "",
                                            imageUrl = article["image"],
                                            link = article["link"] ?: "",
                                            source = source,
                                            publishedAt = parseDate(article["pubDate"]),
                                            author = article["author"]
                                        )
                                    )
                                }
                            }
                            currentArticle = null
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Error parsing RSS: ${e.message}", e)
        }
        
        return articles.take(10) // Limit to 10 articles per source
    }
    
    /**
     * Parse various date formats from RSS feeds
     */
    private fun parseDate(dateStr: String?): Long {
        if (dateStr == null) return System.currentTimeMillis()
        
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd HH:mm:ss",
            "dd MMM yyyy HH:mm:ss"
        )
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                return sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        return System.currentTimeMillis()
    }
    
    /**
     * Fetch current weather for Bocas del Toro
     */
    suspend fun fetchWeather(): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            // Using OpenWeatherMap API
            val url = URL(
                "https://api.openweathermap.org/data/2.5/weather?" +
                "lat=$bocasLatitude&lon=$bocasLongitude" +
                "&appid=$weatherApiKey&units=metric"
            )
            
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                // Return mock weather data if API fails
                return@withContext Result.success(getMockWeather())
            }
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            
            val weather = parseWeatherResponse(response)
            Result.success(weather)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Error fetching weather: ${e.message}", e)
            // Return mock weather for Bocas (tropical climate)
            Result.success(getMockWeather())
        }
    }
    
    /**
     * Parse OpenWeatherMap API response
     */
    private fun parseWeatherResponse(json: String): WeatherData {
        try {
            val obj = JSONObject(json)
            val main = obj.getJSONObject("main")
            val weather = obj.getJSONArray("weather").getJSONObject(0)
            val wind = obj.optJSONObject("wind")
            
            val weatherId = weather.getInt("id")
            val condition = WeatherCondition.fromOpenWeatherCode(weatherId)
            
            return WeatherData(
                temperature = main.getDouble("temp"),
                feelsLike = main.getDouble("feels_like"),
                humidity = main.getInt("humidity"),
                windSpeed = wind?.optDouble("speed", 0.0) ?: 0.0,
                description = weather.getString("description").replaceFirstChar { it.uppercaseChar() },
                icon = weather.getString("icon"),
                condition = condition,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Error parsing weather: ${e.message}", e)
            return getMockWeather()
        }
    }
    
    /**
     * Get mock weather for Bocas del Toro (tropical climate default)
     */
    private fun getMockWeather(): WeatherData {
        // Bocas del Toro is tropical - typically 26-30°C, humid, occasional rain
        val random = Random()
        val temp = 27.0 + random.nextDouble() * 3 // 27-30°C
        val conditions = listOf(
            WeatherCondition.PARTLY_CLOUDY,
            WeatherCondition.CLEAR,
            WeatherCondition.CLOUDY,
            WeatherCondition.DRIZZLE
        )
        val condition = conditions[random.nextInt(conditions.size)]
        
        return WeatherData(
            temperature = temp,
            feelsLike = temp + 2,
            humidity = 75 + random.nextInt(20), // 75-95%
            windSpeed = 5.0 + random.nextDouble() * 10, // 5-15 km/h
            description = condition.description,
            icon = "02d",
            condition = condition,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Fetch weather forecast for next 5 days
     */
    suspend fun fetchForecast(): Result<List<WeatherForecast>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(
                "https://api.openweathermap.org/data/2.5/forecast?" +
                "lat=$bocasLatitude&lon=$bocasLongitude" +
                "&appid=$weatherApiKey&units=metric&cnt=40"
            )
            
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.success(getMockForecast())
            }
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            
            val forecast = parseForecastResponse(response)
            Result.success(forecast)
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Error fetching forecast: ${e.message}", e)
            Result.success(getMockForecast())
        }
    }
    
    /**
     * Parse forecast API response
     */
    private fun parseForecastResponse(json: String): List<WeatherForecast> {
        val forecasts = mutableListOf<WeatherForecast>()
        
        try {
            val obj = JSONObject(json)
            val list = obj.getJSONArray("list")
            
            // Group by day and get daily min/max
            val dailyData = mutableMapOf<String, MutableList<JSONObject>>()
            
            for (i in 0 until list.length()) {
                val item = list.getJSONObject(i)
                val dt = item.getLong("dt") * 1000
                val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(dt))
                dailyData.getOrPut(dayKey) { mutableListOf() }.add(item)
            }
            
            // Create daily forecasts
            dailyData.entries.take(5).forEach { (dayKey, items) ->
                val temps = items.map { it.getJSONObject("main").getDouble("temp") }
                val firstItem = items.first()
                val weather = firstItem.getJSONArray("weather").getJSONObject(0)
                val weatherId = weather.getInt("id")
                
                forecasts.add(
                    WeatherForecast(
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey)?.time ?: 0,
                        tempMin = temps.minOrNull() ?: 0.0,
                        tempMax = temps.maxOrNull() ?: 0.0,
                        description = weather.getString("description").replaceFirstChar { it.uppercaseChar() },
                        icon = weather.getString("icon"),
                        condition = WeatherCondition.fromOpenWeatherCode(weatherId)
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("NewsRepo", "Error parsing forecast: ${e.message}", e)
            return getMockForecast()
        }
        
        return forecasts
    }
    
    /**
     * Get mock forecast for Bocas del Toro
     */
    private fun getMockForecast(): List<WeatherForecast> {
        val random = Random()
        val forecasts = mutableListOf<WeatherForecast>()
        val calendar = Calendar.getInstance()
        
        val conditions = listOf(
            WeatherCondition.PARTLY_CLOUDY,
            WeatherCondition.CLEAR,
            WeatherCondition.RAIN,
            WeatherCondition.CLOUDY
        )
        
        for (i in 0..4) {
            calendar.add(Calendar.DAY_OF_YEAR, if (i == 0) 0 else 1)
            val condition = conditions[random.nextInt(conditions.size)]
            
            forecasts.add(
                WeatherForecast(
                    date = calendar.timeInMillis,
                    tempMin = 24.0 + random.nextDouble() * 2,
                    tempMax = 29.0 + random.nextDouble() * 3,
                    description = condition.description,
                    icon = "02d",
                    condition = condition
                )
            )
        }
        
        return forecasts
    }
}
