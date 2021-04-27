package com.example.android.bussolaaccelerometro

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.reflect.KProperty

class HomeViewModel: ViewModel() {

    val accelX:LiveData<Float> by Database::fastAccX
    val accelY:LiveData<Float> by Database::fastAccY
    val accelZ:LiveData<Float> by Database::fastAccZ
    val gradiNord:LiveData<Int> by Database::fastGradiNord
    val enableRecordInBackground:LiveData<Boolean> by Database::enableRecordInBackground

    fun onEnableRecordInBackground(checked:Boolean)
    {
        Database.enableRecordInBackground.value = checked
    }
}