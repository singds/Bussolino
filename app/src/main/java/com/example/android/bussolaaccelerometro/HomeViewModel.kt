package com.example.android.bussolaaccelerometro

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {
    fun onSensorChanged(event: SensorEvent) {
        Log.d ("MYTAG", "event [${event.sensor.name}}")
    }

    fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d("MYTAG", "${sensor.name} accuracy [${accuracy}]")
    }
}