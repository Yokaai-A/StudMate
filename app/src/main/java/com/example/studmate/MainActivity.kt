package com.example.studmate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.studmate.data.prefs.AppSettings
import com.example.studmate.data.prefs.SessionStore
import com.example.studmate.ui.MainScreen
import com.example.studmate.ui.theme.StudMateTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val sessionStore: SessionStore by inject()
    private var currentDeepLink by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        currentDeepLink = intent?.data

        setContent {
            val settings by sessionStore.settings.collectAsState(initial = AppSettings())

            StudMateTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
            ) {
                MainScreen(initialDeepLink = currentDeepLink)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentDeepLink = intent.data
    }
}
