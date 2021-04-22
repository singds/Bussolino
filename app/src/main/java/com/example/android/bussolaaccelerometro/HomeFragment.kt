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
import android.widget.ImageView
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
    private lateinit var magnetometro: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = context?.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        accelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val vaccX = view.findViewById<TextView>(R.id.accX)
        val vaccY = view.findViewById<TextView>(R.id.accY)
        val vaccZ = view.findViewById<TextView>(R.id.accZ)
        val vgradiNord = view.findViewById<TextView>(R.id.gradiNord)
        val bussola = view.findViewById<ImageView>(R.id.bussola)

        viewModel.accelX.observe(viewLifecycleOwner, { value -> vaccX.text = "%.2f".format(value)})
        viewModel.accelY.observe(viewLifecycleOwner, { value -> vaccY.text = "%.2f".format(value)})
        viewModel.accelZ.observe(viewLifecycleOwner, { value -> vaccZ.text = "%.2f".format(value)})
        viewModel.gradiNord.observe(viewLifecycleOwner, { value ->
            vgradiNord.text = "%.2f".format(value)
            bussola.rotation = -value
        })
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometro, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometro, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        // Unregisters a listener for all sensors.
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