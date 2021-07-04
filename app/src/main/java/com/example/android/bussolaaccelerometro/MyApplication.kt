package com.example.android.bussolaaccelerometro

import android.app.Application
import android.util.Log

/**
 * [MyApplication] estende Application e funge da contenitore per le risorse condivise da tutte le
 * componenti dell'applicazione. Vedi [Repository].
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

        Log.d(
            LOG_TAG,
            "onCreate [pid = ${android.os.Process.myPid()}] [tid = ${android.os.Process.myTid()}]"
        )

        // Alla creazione del processo creo un'istanza del repository che sarà condiviso da tutte
        // le componenti dell'app (Service e Activity).
        // Simile al pattern Singleton.
        pRepository = Repository(this)
    }

    companion object
    {
        const val LOG_TAG = "MyApplication"
    }
}