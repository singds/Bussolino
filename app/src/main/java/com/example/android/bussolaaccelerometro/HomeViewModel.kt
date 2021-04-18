package com.example.android.bussolaaccelerometro

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.PI
import kotlin.reflect.KProperty

class HomeViewModel: ViewModel() {

    private val pAccelX = MutableLiveData<Float>()
    val accelX by ::pAccelX

    private val pAccelY = MutableLiveData<Float>()
    val accelY by ::pAccelY

    private val pAccelZ = MutableLiveData<Float>()
    val accelZ by ::pAccelZ

    private val pGradiNord = MutableLiveData<Float>()
    val gradiNord by ::pGradiNord

    private var lastAccel = FloatArray(3)
    private var lastMagne = FloatArray(3)


    fun onSensorChanged(event: SensorEvent) {
        Log.d ("MYTAG", "event [${event.sensor.name}}")
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            pAccelX.value = event.values[0]
            pAccelY.value = event.values[1]
            pAccelZ.value = event.values[2]

            lastAccel[0] = event.values[0]
            lastAccel[1] = event.values[1]
            lastAccel[2] = event.values[2]

            val matriceRotazione = FloatArray(9)
            SensorManager.getRotationMatrix(matriceRotazione, null, lastAccel, lastMagne)

            val orientazione = FloatArray(3)
            SensorManager.getOrientation(matriceRotazione, orientazione)
            gradiNord.value = orientazione[0] * 180f / PI.toFloat()
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            lastMagne[0] = event.values[0]
            lastMagne[1] = event.values[1]
            lastMagne[2] = event.values[2]
        }
    }

    fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d("MYTAG", "${sensor.name} accuracy [${accuracy}]")
    }
}