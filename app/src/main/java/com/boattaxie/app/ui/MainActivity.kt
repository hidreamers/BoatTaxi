package com.boattaxie.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.boattaxie.app.navigation.BoatTaxieNavHost
import com.boattaxie.app.navigation.Screen
import com.boattaxie.app.ui.theme.BoatTaxieTheme
import com.boattaxie.app.util.LanguageManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language preference
        LanguageManager.applyLanguage(this)
        
        enableEdgeToEdge()
        
        setContent {
            BoatTaxieTheme {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BoatTaxieNavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        val data = intent?.data
        if (data != null) {
            val uri = data.toString()
            if (uri.contains("success")) {
                Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show()
                // Navigate to success screen or update UI
            } else if (uri.contains("ad-payment-success")) {
                Toast.makeText(this, "Ad payment successful! Your ad will be activated soon.", Toast.LENGTH_LONG).show()
            } else if (uri.contains("cancel") || uri.contains("ad-payment-cancel")) {
                Toast.makeText(this, "Payment cancelled", Toast.LENGTH_LONG).show()
            }
        }
    }
}
