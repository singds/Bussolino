package com.example.android.bussolaaccelerometro

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.reflect.KProperty

class HomeViewModel: ViewModel() {

    private val pAccelX = MutableLiveData<Float>()
    val accelX by ::pAccelX

    private val pAccelY = MutableLiveData<Float>()
    val accelY by ::pAccelY

    private val pAccelZ = MutableLiveData<Float>()
    val accelZ by ::pAccelZ

    /* 0 = 360 = ago magnetico diretto a nord
    ruotando in senso orario i gradi rispetto al nord aumentano da 0 a 360
    asse y positivo del sistema di riferimento del dispositivo = ago magnetico
     */
    private val pGradiNord = MutableLiveData<Float>()
    val gradiNord by ::pGradiNord

    private var lastAccel = FloatArray(3)
    private var lastMagne = FloatArray(3)


    fun onSensorChanged(event: SensorEvent) {
        Log.d ("MYTAG", "event [${event.sensor.name}}")
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            lastAccel[0] = filtroAccel (lastAccel[0], event.values[0])
            lastAccel[1] = filtroAccel (lastAccel[1], event.values[1])
            lastAccel[2] = filtroAccel (lastAccel[2], event.values[2])

            pAccelX.value = lastAccel[0]
            pAccelY.value = lastAccel[1]
            pAccelZ.value = lastAccel[2]

            /*
            val matriceRotazione = FloatArray(9)
            SensorManager.getRotationMatrix(matriceRotazione, null, lastAccel, lastMagne)

            val orientazione = FloatArray(3)
            SensorManager.getOrientation(matriceRotazione, orientazione)
            gradiNord.value = orientazione[0] * 180f / PI.toFloat()
            */
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            lastMagne[0] = filtroMagne (lastMagne[0], event.values[0])
            lastMagne[1] = filtroMagne (lastMagne[1], event.values[1])
            lastMagne[2] = filtroMagne (lastMagne[2], event.values[2])

            val magnex = lastMagne[0]
            val magney = lastMagne[1]
            /* calcolo angolo di rotazione rispetto nord magnetico secondo convenzione bussola */
            gradiNord.value = ((-atan2(magnex, magney) * 180f / PI.toFloat()) + 360) % 360
        }
    }

    fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d("MYTAG", "${sensor.name} accuracy [${accuracy}]")
    }

    companion object {
        private const val pesoFiltroMagne = 0.02f
        private fun filtroMagne(oldval: Float?, newval: Float) = (oldval ?: 0.0f) * (1f - pesoFiltroMagne) + newval * pesoFiltroMagne
        private const val pesoFiltroAccel = 0.1f
        private fun filtroAccel(oldval: Float?, newval: Float) = (oldval ?: 0.0f) * (1f - pesoFiltroAccel) + newval * pesoFiltroAccel
    }
}