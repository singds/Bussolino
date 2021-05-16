package com.example.android.bussolaaccelerometro

import androidx.lifecycle.MutableLiveData

object Repository
{
    /* 0 = 360 = ago magnetico diretto a nord
    ruotando in senso orario i gradi rispetto al nord aumentano da 0 a 360
    asse y positivo del sistema di riferimento del dispositivo = ago magnetico
     */
    val currentSample = MutableLiveData<SensorSample>()
    val listSample = MutableLiveData<List<SensorRecord>>()

    val enableRecordInBackground = MutableLiveData<Boolean>()

    data class SensorSample(
            val gradiNord:Float,
            val accelX:Float,
            val accelY:Float,
            val accelZ:Float
    )

    data class SensorRecord(
            val sample: SensorSample,
            val time: Int
    )
}