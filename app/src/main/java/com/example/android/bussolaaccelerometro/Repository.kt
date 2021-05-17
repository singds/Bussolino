package com.example.android.bussolaaccelerometro

import androidx.lifecycle.MutableLiveData
import java.util.*

object Repository
{
    /* 0 = 360 = ago magnetico diretto a nord
    ruotando in senso orario i gradi rispetto al nord aumentano da 0 a 360
    asse y positivo del sistema di riferimento del dispositivo = ago magnetico
     */
    val currentSample = MutableLiveData<SensorSample>()
    var listSample = MutableLiveData<List<SensorSample>>()

    val enableRecordInBackground = MutableLiveData<Boolean>()

    data class SensorSample(
            val gradiNord:Float,
            val accelX:Float,
            val accelY:Float,
            val accelZ:Float,
            val timestamp:Date
    )
}