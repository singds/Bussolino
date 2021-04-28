package com.example.android.bussolaaccelerometro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity()
{
    lateinit var preferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent()
            .setClass(this, ReaderService::class.java)
            .setAction(ReaderService.ACTION_START)
        startService(intent)

        preferences = getPreferences(MODE_PRIVATE)
        Repository.enableRecordInBackground.value = preferences.getBoolean(PREFERENCE_ENABLE_RECORD, false)
    }

    override fun onPause() {
        super.onPause()

        val action = when (Repository.enableRecordInBackground.value) {
            true -> ReaderService.ACTION_RUN_IN_BACKGROUND
            else -> ReaderService.ACTION_STOP
        }
        val intent = Intent()
            .setClass(this, ReaderService::class.java)
            .setAction(action)
        startService(intent)

        preferences.edit()
            .putBoolean(PREFERENCE_ENABLE_RECORD, Repository.enableRecordInBackground.value ?: false)
            .apply()
    }

    private fun createNotificationChannel() {
        // nelle API 26+ devo creare un notification channel per mostrare notifica
        // NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(ID_NOTIF_CH_MAIN,
                    getString(R.string.notif_ch_main),
                    NotificationManager.IMPORTANCE_LOW)
            channel.description = getString(R.string.notif_ch_main_description)

            // Un canale può essere registrato più volte senza errori
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object
    {
        const val ID_NOTIF_CH_MAIN = "NotifChMain"
        const val ID_NOTIF_READING = 1
        const val PREFERENCE_ENABLE_RECORD = "enableRecord"
    }
}