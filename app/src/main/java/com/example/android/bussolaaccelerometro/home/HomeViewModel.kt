package com.example.android.bussolaaccelerometro.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.android.bussolaaccelerometro.data.Repository

class HomeViewModel: ViewModel() {

    val accelX = Transformations.map(Repository.currentSample) {it.accelX}
    val accelY = Transformations.map(Repository.currentSample) {it.accelY}
    val accelZ = Transformations.map(Repository.currentSample) {it.accelZ}
    val gradiNord = Transformations.map(Repository.currentSample) {Math.round(it.gradiNord)}
    val enableRecordInBackground:LiveData<Boolean> by Repository::enableRecordInBackground

    fun onEnableRecordInBackground(checked:Boolean)
    {
        Repository.enableRecordInBackground.value = checked
    }
}