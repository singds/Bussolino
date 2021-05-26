package com.example.android.bussolaaccelerometro.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.bussolaaccelerometro.data.Repository

class MainActivityViewModel(private val repo:Repository):ViewModel()
{
    var enableRecordInBackground by repo::enableRecordInBackground
    var runInBackgroundAccepted by repo::runInBackgroundAccepted
}

class MainActivityViewModelFactory(private val repo:Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}