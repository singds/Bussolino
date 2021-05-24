package com.example.android.bussolaaccelerometro.main

import androidx.lifecycle.ViewModel
import com.example.android.bussolaaccelerometro.data.Repository

class MainActivityViewModel:ViewModel()
{
    var enableRecordInBackground by Repository::enableRecordInBackground
    var runInBackgroundAccepted by Repository::runInBackgroundAccepted
}