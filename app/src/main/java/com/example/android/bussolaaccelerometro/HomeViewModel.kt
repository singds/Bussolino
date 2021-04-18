package com.example.android.bussolaaccelerometro

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.reflect.KProperty

class HomeViewModel: ViewModel() {

    private val pAccelX = MutableLiveData<Float>()
    val accelX by ::pAccelX

    private val pAccelY = MutableLiveData<Float>()
    val accelY by ::pAccelY

    private val pAccelZ = MutableLiveData<Float>()
    val accelZ by ::pAccelZ

    fun onSensorChanged(event: SensorEvent) {
        Log.d ("MYTAG", "event [${event.sensor.name}}")
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            pAccelX.value = event.values[0]
            pAccelY.value = event.values[1]
            pAccelZ.value = event.values[2]
        }
    }

    fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d("MYTAG", "${sensor.name} accuracy [${accuracy}]")
    }
}