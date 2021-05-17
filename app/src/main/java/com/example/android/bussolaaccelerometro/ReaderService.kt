package com.example.android.bussolaaccelerometro

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.*
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
    private val pesoFiltroMagne = 0.02f
    private val pesoFiltroAccel = 0.1f
    private lateinit var handler:Handler
    private var sampleCounter:Int = 0

    private val timerTick = object :Runnable{
        override fun run() {
            val list = Repository.listSample

            val newlist = list.value?.toMutableList() ?: mutableListOf()
            newlist.add(getLastSample())
            sampleCounter++
            while (newlist.size > NUM_CAMPIONI)
                newlist.removeFirst()
            Repository.listSample.value = newlist

            handler.postDelayed(this,500)
        }
    }

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

        handler = Handler(mainLooper)
        handler.post(timerTick)
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
        handler.removeCallbacks(timerTick)
        sensorManager.unregisterListener(this)
        Repository.listSample.value = null
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

                Repository.currentSample.value = getLastSample()
            }
            if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                lastMagne[0] = filtroMagne(lastMagne[0], values[0])
                lastMagne[1] = filtroMagne(lastMagne[1], values[1])
                lastMagne[2] = filtroMagne(lastMagne[2], values[2])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        sensor?.let {

        }
    }

    private fun getLastSample():Repository.SensorSample {
        val magnex = lastMagne[0]
        val magney = lastMagne[1]

        return Repository.SensorSample(
                getGradiNord(magnex,magney),
                lastAccel[0],
                lastAccel[1],
                lastAccel[2],
                Date()
        )
    }

    private fun filtroMagne(oldval: Float?, newval: Float) = (oldval ?: 0.0f) * (1f - pesoFiltroMagne) + newval * pesoFiltroMagne

    private fun filtroAccel(oldval: Float?, newval: Float) = (oldval ?: 0.0f) * (1f - pesoFiltroAccel) + newval * pesoFiltroAccel

    private fun getGradiNord(magx:Float, magy:Float) = ((-atan2(magx, magy) * 180f / PI.toFloat()) + 360) % 360

    companion object
    {
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
        const val ACTION_RUN_IN_BACKGROUND = "background"
        const val NUM_CAMPIONI = 600
        const val MS_FRA_CAMPIONI = 500
    }
}