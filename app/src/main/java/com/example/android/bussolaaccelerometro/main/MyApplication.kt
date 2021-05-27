package com.example.android.bussolaaccelerometro.main

import android.app.Application
import com.example.android.bussolaaccelerometro.data.Repository

class MyApplication: Application()
{
    private lateinit var repositorySingleton:Repository
    val repository by ::repositorySingleton

    override fun onCreate() {
        super.onCreate()

        repositorySingleton = Repository(this)
    }
}