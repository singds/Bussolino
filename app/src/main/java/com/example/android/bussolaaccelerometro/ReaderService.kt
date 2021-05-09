package com.example.android.bussolaaccelerometro

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlin.math.PI
import kotlin.math.atan2

class ReaderService : Service(),
        SensorEventListener
{

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometro: Sensor
    private lateinit var magnetometro: Sensor
    private var lastAccel = FloatArray(3)
    private var lastMagne = FloatArray(3)

    override fun onCreate() {
        super.onCreate()
        Log.d("MYTAG", "service create")

        val notification = NotificationCompat.Builder(this, MainActivity.ID_NOTIF_CH_MAIN)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        // notification id must not be 0
        startForeground(MainActivity.ID_NOTIF_READING, notification)

        sensorManager = getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        accelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MYTAG", "service start [${flags}][$startId]")

        intent?.let {
            when(it.action)
            {
                ACTION_RUN_IN_BACKGROUND -> {
                    val notification = NotificationCompat.Builder(this, MainActivity.ID_NOTIF_CH_MAIN)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .build()
                    // notification id must not be 0
                    startForeground(MainActivity.ID_NOTIF_READING, notification)
                }
                ACTION_START -> {
                    stopForeground(true)
                }
                ACTION_STOP -> {
                    stopSelfResult(startId)
                }
                else -> {

                }
            }
        }

        sensorManager.registerListener(this, accelerometro, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometro, SensorManager.SENSOR_DELAY_GAME)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MYTAG", "service destroy")
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.apply {
            if (sensor.type == Sensor.TYPE_ACCELEROMETER) {
                lastAccel[0] = filtroAccel(lastAccel[0], values[0])
                lastAccel[1] = filtroAccel(lastAccel[1], values[1])
                lastAccel[2] = filtroAccel(lastAccel[2], values[2])

                Repository.fastAccX.value = lastAccel[0]
                Repository.fastAccY.value = lastAccel[1]
                Repository.fastAccZ.value = lastAccel[2]
            }
            if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                lastMagne[0] = filtroMagne(lastMagne[0], values[0])
                lastMagne[1] = filtroMagne(lastMagne[1], values[1])
                lastMagne[2] = filtroMagne(lastMagne[2], values[2])

                val magnex = lastMagne[0]
                val magney = lastMagne[1]


                /* calcolo angolo di rotazione rispetto nord magnetico secondo convenzione bussola */
                Repository.fastGradiNord.value = getGradiNord(magnex, magney)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        sensor?.let {

        }
    }

    companion object
    {
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
        const val ACTION_RUN_IN_BACKGROUND = "background"

        private const val pesoFiltroMagne = 0.02f
        private fun filtroMagne(oldval: Float?, newval: Float) = (oldval ?: 0.0f) * (1f - pesoFiltroMagne) + newval * pesoFiltroMagne

        private const val pesoFiltroAccel = 0.1f
        private fun filtroAccel(oldval: Float?, newval: Float) = (oldval ?: 0.0f) * (1f - pesoFiltroAccel) + newval * pesoFiltroAccel

        private fun getGradiNord(magx:Float, magy:Float) = ((-atan2(magx, magy) * 180f / PI.toFloat()) + 360) % 360
    }
}