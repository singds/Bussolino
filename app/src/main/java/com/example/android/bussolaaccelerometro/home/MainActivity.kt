package com.example.android.bussolaaccelerometro.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.bussolaaccelerometro.R
import com.example.android.bussolaaccelerometro.data.ReaderService
import com.example.android.bussolaaccelerometro.data.Repository

class MainActivity : AppCompatActivity()
{
    lateinit var preferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Creo il canale di notifica.
        // Se il canale di notifica esiste già la creazione non ha alcun effetto.
        createNotificationChannel()
    }

    override fun onResume() {
        super.onResume()

        // Metto in esecuzione il servizio che eseguira l'acquisizione dei dati dai sensori.
        val intent = Intent()
            .setClass(this, ReaderService::class.java)
            .setAction(ReaderService.ACTION_START)
        startService(intent)

        // recupero lo stato persistente
        preferences = getPreferences(MODE_PRIVATE)
        Repository.enableRecordInBackground.value = preferences.getBoolean(PREFERENCE_ENABLE_RECORD, false)
    }

    override fun onPause() {
        super.onPause()

        // se sto soltanto cambiando configurazione non modifico lo stato del servizio
        if (!isChangingConfigurations) {
            when (Repository.enableRecordInBackground.value) {
                true -> {
                    val intentStartInBackground = Intent()
                            .setClass(this, ReaderService::class.java)
                            .setAction(ReaderService.ACTION_RUN_IN_BACKGROUND)
                    startService(intentStartInBackground)
                }
                else -> {
                    val intentStop = Intent()
                            .setClass(this, ReaderService::class.java)
                   stopService(intentStop)
                }
            }
        }

        // salvo lo stato persistente
        preferences.edit()
            .putBoolean(PREFERENCE_ENABLE_RECORD, Repository.enableRecordInBackground.value ?: false)
            .apply()
    }

    /**
     * Nelle api 26+ le applicazioni che vogliono mostrare notifiche devono prima creare un
     * notification channel.
     * I notification channel permettono di dividere i tipi di notifiche mostrate dall'applicazione
     * in gruppi (canali). Dal momento in cui un canale viene creato dall'applicazione l'utente
     * ne assume il piano controllo: l'applicazione non sarà più in grado di modificare le
     * impostazioni del canale.
     * L'applicazione può specificare le impostazioni del canale solo al momento della sua
     * creazione.
     * Per ogni canale l'utente può scegliere alcune impostazioni (come silenziare le notifiche)
     * che vengono applicate a tutte le notifiche che l'app pubblicherà su quel canale.
     *
     * I notification channel danno all'utente un controllo più fine sulle impostazioni delle
     * notifiche rendendo inoltre queste impostazioni uniformi fra le diverse applicazioni.
     */
    private fun createNotificationChannel() {
        // nelle API 26+ devo creare un notification channel per mostrare notifica
        // NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(ID_NOTIF_CH_MAIN,
                    getString(R.string.canale_principale),
                    NotificationManager.IMPORTANCE_LOW)
            channel.description = getString(R.string.tutte_le_notifiche)

            // Un canale può essere registrato più volte senza errori
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object
    {
        /**
         * L'id del notification channel principale (l'unico).
         */
        const val ID_NOTIF_CH_MAIN = "NotifChMain"

        /**
         * Id della notifica di app running in background.
         */
        const val ID_NOTIF_READING = 1

        const val PREFERENCE_ENABLE_RECORD = "enableRecord"
    }
}