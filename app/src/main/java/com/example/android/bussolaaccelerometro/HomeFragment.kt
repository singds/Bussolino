package com.example.android.bussolaaccelerometro

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.EventLog
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels

class HomeFragment : Fragment(),
    SensorEventListener
{
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometro: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = context?.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        accelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensors) {
            Log.d("MYLOG", "${sensor.name} [${sensor.stringType}]")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val vaccX = view.findViewById<TextView>(R.id.accX)
        val vaccY = view.findViewById<TextView>(R.id.accY)
        val vaccZ = view.findViewById<TextView>(R.id.accZ)

        viewModel.accelX.observe(viewLifecycleOwner, { value -> vaccX.text = "%.2f".format(value)})
        viewModel.accelY.observe(viewLifecycleOwner, { value -> vaccY.text = "%.2f".format(value)})
        viewModel.accelZ.observe(viewLifecycleOwner, { value -> vaccZ.text = "%.2f".format(value)})

        return view
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometro, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            viewModel.onSensorChanged (it)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        sensor?.let {
            viewModel.onAccuracyChanged(it, accuracy)
        }
    }
}