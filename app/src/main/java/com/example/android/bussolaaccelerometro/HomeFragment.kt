package com.example.android.bussolaaccelerometro

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels

class HomeFragment : Fragment()
{
    private val viewModel by viewModels<HomeViewModel>()

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
            vgradiNord.text = "%d °".format(value)
            bussola.rotation = -value.toFloat()
        })
    }
}