package com.example.android.bussolaaccelerometro.chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.android.bussolaaccelerometro.data.Repository
import com.example.android.bussolaaccelerometro.data.SensorSample

class ChartViewModel: ViewModel() {
    val listSample: LiveData<List<SensorSample>> by Repository::listSample
}