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
import com.example.android.bussolaaccelerometro.data.ReaderService

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

        // Aggiungo le view delle accelerazioni in modo programmatico
        addAccelerationBoxes(view.findViewById(R.id.containerAcc))


        val vGradiNord = view.findViewById<TextView>(R.id.vGradiNord)
        val imgBussola = view.findViewById<ImageView>(R.id.imgBussola)
        val switchAbilita = view.findViewById<SwitchCompat>(R.id.switchAbilita)
        val fabChart = view.findViewById<FloatingActionButton>(R.id.fabChart)


        // Alla pressione del FAB apro la pagina dei grafici.
        fabChart.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chartFragment)
        }

        // Osservo l'angolo rispetto al nord magnetico.
        // Quando cambia aggiorno il numero visualizzato e la rotazione dell'immagine.
        viewModel.gradiNord.observe(viewLifecycleOwner, { value ->
            vGradiNord.text = "%d °".format(value)
            imgBussola.rotation = -getAngoloImmagine(value).toFloat()
        })


        switchAbilita.isChecked = viewModel.enableRecordInBackground
        switchAbilita.setOnCheckedChangeListener { _, isChecked ->
            viewModel.enableRecordInBackground = isChecked
        }
    }

    /**
     * Creo le view per la visualizzazione delle accelerazioni in modo programmatico.
     * @param containerAcc il layout destinato a contenere i box delle accelerazioni.
     */
    private fun addAccelerationBoxes(containerAcc:ConstraintLayout)
    {
        // Il layout è unico per tutte le accelerazioni.
        // Dopo aver creato ciascun box lo personalizzo gli assegno un identificatore.
        // L'identificatore è necessario per poter fare riferimento alla view nel ConstraintLayout
        val bindingAccX = CardAccelBinding.inflate(layoutInflater)
        bindingAccX.accLabel.text = getString(R.string.accelerazione_x)
        bindingAccX.root.id = View.generateViewId()
        val bindingAccY = CardAccelBinding.inflate(layoutInflater)
        bindingAccY.accLabel.text = getString(R.string.accelerazione_y)
        bindingAccY.root.id = View.generateViewId()
        val bindingAccZ = CardAccelBinding.inflate(layoutInflater)
        bindingAccZ.accLabel.text = getString(R.string.accelerazione_z)
        bindingAccZ.root.id = View.generateViewId()


        // Aggiungo le view all layout
        containerAcc.addView(bindingAccX.root)
        containerAcc.addView(bindingAccY.root)
        containerAcc.addView(bindingAccZ.root)


        // Aggancio il lato destro e sinistro di ciascun box rispettivamente al lato destro e
        // sinistro del layout che li contiene. Ogni box occuperà l'intera larghezza del layout.
        val cs = ConstraintSet()
        cs.connect(bindingAccX.root.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
        cs.connect(bindingAccX.root.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)

        cs.connect(bindingAccY.root.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
        cs.connect(bindingAccY.root.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)

        cs.connect(bindingAccZ.root.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
        cs.connect(bindingAccZ.root.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)


        // Dispongo i 3 box in una catena verticale in modo tale che si distribuiscano equamente
        // lungo tutta l'altezza del layout.
        val chain = arrayOf(bindingAccX.root.id, bindingAccY.root.id, bindingAccZ.root.id)
        cs.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                chain.toIntArray(), null, ConstraintSet.CHAIN_SPREAD)
        cs.applyTo(containerAcc)


        // Osservo le accelerazioni del model e aggiorno la grafica non appena cambiano.
        viewModel.accelX.observe(viewLifecycleOwner, { value ->
            bindingAccX.accValue.text = "%.2f".format(value)
        })
        viewModel.accelY.observe(viewLifecycleOwner, { value ->
            bindingAccY.accValue.text = "%.2f".format(value)
        })
        viewModel.accelZ.observe(viewLifecycleOwner, { value ->
            bindingAccZ.accValue.text = "%.2f".format(value)
        })
    }

    /**
     * Determina l'angolo di cui deve essere ruotata l'immagine affinche la punta dell'ago
     * magnetico (dell'immagine) punti verso il nord magnetico.
     * Per fare questo calcolo è necessario conoscere l'orientazione dell'interfaccia grafica
     * rispetto all'orientazione naturale del dispositivo.
     *
     * Angoli positivi fanno ruotare la view in senso orario.
     *
     * @param angolo Angolo fra asse y del dispositivo e nord magnetico (in gradi). Per maggiori
     * dettagli sulla convenzione utilizzata per la misura dell'angolo vedere [ReaderService.getGradiNord].
     * @return Angolo da applicare all'immagine affinchè l'ago punti verso il nord magnetico.
     */
    private fun getAngoloImmagine(angolo:Int): Int {
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