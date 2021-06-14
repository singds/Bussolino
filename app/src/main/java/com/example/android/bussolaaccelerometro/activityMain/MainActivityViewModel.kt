package com.example.android.bussolaaccelerometro.activityMain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.bussolaaccelerometro.Repository

class MainActivityViewModel(private val repo: Repository):ViewModel()
{
}

class MainActivityViewModelFactory(private val repo: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}