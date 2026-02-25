package com.boattaxie.app.data.model

import com.google.firebase.Timestamp

/**
 * News article from RSS feeds or news sources
 */
data class NewsArticle(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String? = null,
    val imageUrl: String? = null,
    val link: String = "",
    val source: NewsSource = NewsSource.BOCAS_BREEZE,
    val publishedAt: Long = System.currentTimeMillis(),
    val author: String? = null
)

/**
 * Weather data for Bocas del Toro
 */
data class WeatherData(
    val temperature: Double = 0.0,
    val feelsLike: Double = 0.0,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val description: String = "",
    val icon: String = "",
    val condition: WeatherCondition = WeatherCondition.CLEAR,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Weather forecast item
 */
data class WeatherForecast(
    val date: Long = 0L,
    val tempMin: Double = 0.0,
    val tempMax: Double = 0.0,
    val description: String = "",
    val icon: String = "",
    val condition: WeatherCondition = WeatherCondition.CLEAR
)

/**
 * News sources
 */
enum class NewsSource(val displayName: String, val url: String, val icon: String) {
    BOCAS_BREEZE("Bocas Breeze", "https://thebocasbreeze.com/feed/", "📰"),
    PANAMA_NEWS("Panama News", "https://www.newsroompanama.com/feed", "🇵🇦"),
    WEATHER("Weather", "", "🌤️");
    
    fun getIconEmoji(): String = icon
}

/**
 * Weather conditions
 */
enum class WeatherCondition(val icon: String, val description: String) {
    CLEAR("☀️", "Clear"),
    PARTLY_CLOUDY("⛅", "Partly Cloudy"),
    CLOUDY("☁️", "Cloudy"),
    RAIN("🌧️", "Rain"),
    THUNDERSTORM("⛈️", "Thunderstorm"),
    DRIZZLE("🌦️", "Drizzle"),
    FOG("🌫️", "Fog"),
    SNOW("🌨️", "Snow");
    
    companion object {
        fun fromOpenWeatherCode(code: Int): WeatherCondition {
            return when (code) {
                in 200..232 -> THUNDERSTORM
                in 300..321 -> DRIZZLE
                in 500..531 -> RAIN
                in 600..622 -> SNOW
                in 701..781 -> FOG
                800 -> CLEAR
                in 801..802 -> PARTLY_CLOUDY
                in 803..804 -> CLOUDY
                else -> CLEAR
            }
        }
    }
}

/**
 * Combined news feed state
 */
data class NewsFeedState(
    val articles: List<NewsArticle> = emptyList(),
    val weather: WeatherData? = null,
    val forecast: List<WeatherForecast> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastRefresh: Long = 0L
)
