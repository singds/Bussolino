package com.example.android.bussolaaccelerometro

import android.app.Application
import com.example.android.bussolaaccelerometro.Repository

class MyApplication: Application()
{
    private lateinit var repositorySingleton: Repository
    val repository by ::repositorySingleton

    override fun onCreate() {
        super.onCreate()

        repositorySingleton = Repository(this)
    }
}