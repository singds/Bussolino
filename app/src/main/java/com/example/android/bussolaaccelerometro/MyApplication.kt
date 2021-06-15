package com.example.android.bussolaaccelerometro

import android.app.Application

/**
 * [MyApplication] estende Application e funge da contenitore per le risorse condivise da tutte le
 * componenti dell'applicazione.
 */
class MyApplication : Application() {
    private lateinit var pRepository: Repository
    val repository by ::pRepository

    /**
     * Called when the application is starting, before any activity, service, or receiver objects
     * (excluding content providers) have been created.
     */
    override fun onCreate() {
        super.onCreate()

        // Alla creazione del processo creo un'istanza del repository che sarà condiviso da tutte
        // le componenti dell'app (Service e Activity).
        // Simile al pattern Singleton.
        pRepository = Repository(this)
    }
}