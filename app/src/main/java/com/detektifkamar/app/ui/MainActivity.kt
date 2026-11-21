package com.detektifkamar.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.appContainer
import com.detektifkamar.app.ui.navigation.AppNavHost
import com.detektifkamar.app.ui.theme.DetektifKamarTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val preferencesRepo = appContainer.preferencesRepository
            val themeMode by preferencesRepo.themeMode.collectAsStateWithLifecycle()

            DetektifKamarTheme(themeMode = themeMode) {
                AppNavHost()
            }
        }
    }
}
