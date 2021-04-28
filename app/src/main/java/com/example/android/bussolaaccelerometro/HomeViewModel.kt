package com.example.android.bussolaaccelerometro

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {

    val accelX:LiveData<Float> by Repository::fastAccX
    val accelY:LiveData<Float> by Repository::fastAccY
    val accelZ:LiveData<Float> by Repository::fastAccZ
    val gradiNord = Transformations.map(Repository.fastGradiNord) { Math.round(it) }
    val enableRecordInBackground:LiveData<Boolean> by Repository::enableRecordInBackground

    fun onEnableRecordInBackground(checked:Boolean)
    {
        Repository.enableRecordInBackground.value = checked
    }
}