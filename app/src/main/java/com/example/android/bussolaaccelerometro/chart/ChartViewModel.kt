package com.example.android.bussolaaccelerometro.chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.bussolaaccelerometro.data.Repository
import com.example.android.bussolaaccelerometro.data.SensorSample
import com.example.android.bussolaaccelerometro.home.HomeViewModel

class ChartViewModel(private val repo:Repository): ViewModel() {
    val listSample: LiveData<List<SensorSample>> by repo::listSample
}

class ChartViewModelFactory(private val repo:Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
            return ChartViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}