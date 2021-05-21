package com.example.android.bussolaaccelerometro.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android.bussolaaccelerometro.R
import com.example.android.bussolaaccelerometro.databinding.CardAccelBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

        val bindingAccX = CardAccelBinding.inflate(layoutInflater)
        bindingAccX.accLabel.text = getString(R.string.accelerazione_x)
        bindingAccX.root.id = View.generateViewId()
        val bindingAccY = CardAccelBinding.inflate(layoutInflater)
        bindingAccY.accLabel.text = getString(R.string.accelerazione_y)
        bindingAccY.root.id = View.generateViewId()
        val bindingAccZ = CardAccelBinding.inflate(layoutInflater)
        bindingAccZ.accLabel.text = getString(R.string.accelerazione_z)
        bindingAccZ.root.id = View.generateViewId()

        val containerAcc = view.findViewById<ConstraintLayout>(R.id.containerAcc)

        containerAcc.addView(bindingAccX.root)
        containerAcc.addView(bindingAccY.root)
        containerAcc.addView(bindingAccZ.root)

        val cs = ConstraintSet()
        cs.connect(bindingAccX.root.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
        cs.connect(bindingAccX.root.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)

        cs.connect(bindingAccY.root.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
        cs.connect(bindingAccY.root.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)

        cs.connect(bindingAccZ.root.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
        cs.connect(bindingAccZ.root.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)

        val chain = arrayOf(bindingAccX.root.id, bindingAccY.root.id, bindingAccZ.root.id)
        cs.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP,
            ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
            chain.toIntArray(), null, ConstraintSet.CHAIN_SPREAD)

        cs.applyTo(containerAcc)


        val vgradiNord = view.findViewById<TextView>(R.id.gradiNord)
        val bussola = view.findViewById<ImageView>(R.id.bussolaImg)
        val abilita = view.findViewById<SwitchCompat>(R.id.abilita)
        val chart = view.findViewById<FloatingActionButton>(R.id.chart)

        chart.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chartFragment)
        }

        viewModel.accelX.observe(viewLifecycleOwner, { value ->
            bindingAccX.accValue.text = "%.2f".format(value)
        })
        viewModel.accelY.observe(viewLifecycleOwner, { value ->
            bindingAccY.accValue.text = "%.2f".format(value)
        })
        viewModel.accelZ.observe(viewLifecycleOwner, { value ->
            bindingAccZ.accValue.text = "%.2f".format(value)
        })
        viewModel.gradiNord.observe(viewLifecycleOwner, { value ->
            vgradiNord.text = "%d °".format(value)
            bussola.rotation = -getAngoloImmagine(value).toFloat()
        })
        viewModel.enableRecordInBackground.observe(viewLifecycleOwner, { value ->
            abilita.isChecked = value
        })
        abilita.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEnableRecordInBackground (isChecked)
        }
    }

    /**
     * Angoli positivi fanno ruotare la view in senso orario.
     */
    fun getAngoloImmagine(angolo:Int): Int {
        view?.display?.apply {
            return when (rotation){
                Surface.ROTATION_0 -> angolo
                Surface.ROTATION_90 -> angolo + 90
                Surface.ROTATION_180 -> angolo + 180
                Surface.ROTATION_270 -> angolo + 270
                else -> angolo
            }
        }
        return angolo;
    }
}