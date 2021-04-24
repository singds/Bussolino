package com.example.android.bussolaaccelerometro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent()
            .setClass(this, ReaderService::class.java)
            .setAction(ReaderService.ACTION_START)
        startService(intent)
    }

    override fun onPause() {
        super.onPause()
//        val intent = Intent()
//            .setClass(this, ReaderService::class.java)
//        stopService(intent)
    }
}