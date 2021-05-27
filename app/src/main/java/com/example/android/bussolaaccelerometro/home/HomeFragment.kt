package com.example.android.bussolaaccelerometro.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.bussolaaccelerometro.R
import com.example.android.bussolaaccelerometro.data.ReaderService
import com.example.android.bussolaaccelerometro.data.Repository
import com.example.android.bussolaaccelerometro.databinding.CardAccelBinding
import com.example.android.bussolaaccelerometro.main.MyApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment()
{
    private lateinit var viewModel:HomeViewModel

    private lateinit var bindingAccX:CardAccelBinding
    private lateinit var bindingAccY:CardAccelBinding
    private lateinit var bindingAccZ:CardAccelBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val repo = (requireActivity().application as MyApplication).repository
        val viewModelFactory = HomeViewModelFactory(repo)
        viewModel = ViewModelProvider(this,viewModelFactory).get(HomeViewModel::class.java)

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
            viewModel.onClickChartButton()
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

        viewModel.event.observe(viewLifecycleOwner) {
            when(it) {
                HomeViewModel.EVENT_GOTO_CHART_PAGE -> {
                    findNavController().navigate(R.id.action_homeFragment_to_chartFragment)
                }
                HomeViewModel.EVENT_SHOW_DIALOG_INFO -> {
                    showDialogInfo()
                }
            }
            if (it != null)
                viewModel.eventHandled()
        }
    }

    /**
     * Creo le view per la visualizzazione delle accelerazioni in modo programmatico.
     * @param containerAcc il layout destinato a contenere i box delle accelerazioni.
     */
    private fun addAccelerationBoxes(containerAcc: ConstraintLayout)
    {
        // Il layout è unico per tutte le accelerazioni.
        // Dopo aver creato ciascun box lo personalizzo gli assegno un identificatore.
        // L'identificatore è necessario per poter fare riferimento alla view nel ConstraintLayout
        bindingAccX = CardAccelBinding.inflate(layoutInflater)
                .apply {
            accLabel.text = getString(R.string.accelerazione_x)
            root.id = View.generateViewId()
            accDirection.setImageResource(R.drawable.ic_arrow_x)
        }

        bindingAccY = CardAccelBinding.inflate(layoutInflater)
                .apply {
            accLabel.text = getString(R.string.accelerazione_y)
            root.id = View.generateViewId()
            accDirection.setImageResource(R.drawable.ic_arrow_y)
        }

        bindingAccZ = CardAccelBinding.inflate(layoutInflater)
                .apply {
            accLabel.text = getString(R.string.accelerazione_z)
            root.id = View.generateViewId()
            accDirection.setImageResource(R.drawable.ic_arrow_z)
        }


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

            // quando il dispositivo è ruotato rispetto al suo orientamento naturale devo aggiustare
            // la rotazione delle freccette che indicano la direzione degli assi del device.
            val angolo = -getAngoloImmagine(0).toFloat()
            if (angolo != bindingAccX.accDirection.rotation)
                bindingAccX.accDirection.rotation = angolo
        })
        viewModel.accelY.observe(viewLifecycleOwner, { value ->
            bindingAccY.accValue.text = "%.2f".format(value)

            val angolo = -getAngoloImmagine(0).toFloat()
            if (angolo != bindingAccY.accDirection.rotation)
                bindingAccY.accDirection.rotation = angolo
        })
        viewModel.accelZ.observe(viewLifecycleOwner, { value ->
            bindingAccZ.accValue.text = "%.2f".format(value)
        })
    }

    /**
     * Determina l'angolo di cui deve essere ruotata l'immagine affinche la punta dell'ago
     * magnetico (dell'immagine) punti verso il nord magnetico.
     * Per fare questo calcolo è necessario conoscere l'orientamento dell'interfaccia grafica
     * rispetto all'orientamento naturale del dispositivo.
     *
     * Angoli positivi fanno ruotare la view in senso orario.
     *
     * @param angolo Angolo fra asse y del dispositivo e nord magnetico (in gradi). Per maggiori
     * dettagli sulla convenzione utilizzata per la misura dell'angolo vedere [ReaderService.getGradiNord].
     * @return Angolo da applicare all'immagine affinchè l'ago punti verso il nord magnetico.
     */
    private fun getAngoloImmagine(angolo: Int): Int {
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

    /**
     * Mostra un dialog che informa l'utente sulla possibilità di abilitare il campionamento
     * in background. Il dialog deve essere confermato.
     */
    private fun showDialogInfo() {
        context?.let { context ->
            MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.informazione))
                    .setMessage(getString(R.string.puoi_abilitare_la_registrazione))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        viewModel.onDialogInfoOk()
                    }
                    .setCancelable(false)
                    .show()
        }
    }
}