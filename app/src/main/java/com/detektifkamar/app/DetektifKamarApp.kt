package com.detektifkamar.app

import android.app.Application
import android.content.Context
import com.detektifkamar.app.di.AppContainer
import com.detektifkamar.app.di.DefaultAppContainer

/**
 * Titik masuk utama aplikasi Android Detektif Kamar.
 * Menginisialisasi DI manual (AppContainer) saat aplikasi dijalankan.
 */
class DetektifKamarApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}

/**
 * Ekstensi praktis untuk mengambil AppContainer dari konteks mana pun di lapisan UI.
 */
val Context.appContainer: AppContainer
    get() = (applicationContext as DetektifKamarApp).container
