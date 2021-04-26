package com.example.android.bussolaaccelerometro

import androidx.lifecycle.MutableLiveData

object Database
{
    /* 0 = 360 = ago magnetico diretto a nord
    ruotando in senso orario i gradi rispetto al nord aumentano da 0 a 360
    asse y positivo del sistema di riferimento del dispositivo = ago magnetico
     */
    val fastGradiNord = MutableLiveData<Int>()
    val fastAccX = MutableLiveData<Float>()
    val fastAccY = MutableLiveData<Float>()
    val fastAccZ = MutableLiveData<Float>()
}