package com.example.android.bussolaaccelerometro.fragmentHome

import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.bussolaaccelerometro.R
import com.example.android.bussolaaccelerometro.databinding.CardAccelBinding
import com.example.android.bussolaaccelerometro.MyApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * [HomeFragment] visualizza le 3 accelerazioni (x,y,z) a cui è sottoposto il dispositivo e una
 * bussola che punta a nord.
 * Quando i valori letti dal magnetometro diventano inaffidabili, in sostituzione alla bussola
 * vengono mostrati un'immagine e un messaggio che indicano all'utente di ruotare il dispositivo
 * per eseguire la calibrazione del sensore.
 * Nel fragment è presente un FAB che permette di passare alla schermata di visualizzazione dei
 * grafici.
 */
class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel

    private lateinit var bindingAccX: CardAccelBinding
    private lateinit var bindingAccY: CardAccelBinding
    private lateinit var bindingAccZ: CardAccelBinding

    /**
     * Called to have the fragment instantiate its user interface view.
     * This will be called between onCreate(Bundle) and onViewCreated(View, Bundle).
     * It is recommended to only inflate the layout in this method and move logic that operates
     * on the returned View to onViewCreated(View, Bundle).
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val repo = (requireActivity().application as MyApplication).repository
        val viewModelFactory = HomeViewModelFactory(repo)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * Called immediately after onCreateView.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Per la creazione dell'interfaccia utente ho voluto sperimentare vari approcci.
        // In questo popolo una parte dell'interfaccia in modo programmatico.
        // Creo le view delle accelerazioni e le aggiungo al layout.
        addAccelerationBoxes(view.findViewById(R.id.containerAcc))


        val vGradiNord = view.findViewById<TextView>(R.id.vGradiNord)
        val imgBussola = view.findViewById<ImageView>(R.id.imgBussola)
//        val switchAbilita = view.findViewById<SwitchCompat>(R.id.switchAbilita)
        val fabChart = view.findViewById<FloatingActionButton>(R.id.fabChart)
        val cardCalibrazione = view.findViewById<CardView>(R.id.cardCalibrazione)


        // Osservo lo stato dell'angolo rispetto al nord magnetico.
        // Quando cambia aggiorno il numero visualizzato e la rotazione dell'immagine.
        viewModel.gradiNord.observe(viewLifecycleOwner, { value ->
            vGradiNord.text = "%d %s".format(value, getString(R.string.udm_gradi))
            imgBussola.rotation = getAngoloImmagine(-value).toFloat()

            // in base all'accuratezza dell'ultima lettura decido se rendere visibile o meno
            // la card che chiede all'utente di effettuare la calibrazione.
            if(viewModel.magneAccuracy < SensorManager.SENSOR_STATUS_ACCURACY_LOW)
                cardCalibrazione.visibility = View.VISIBLE
            else
                cardCalibrazione.visibility = View.INVISIBLE
        })


//        switchAbilita.isChecked = viewModel.enableRecordInBackground
//        switchAbilita.setOnCheckedChangeListener { _, isChecked ->
//            viewModel.enableRecordInBackground = isChecked
//        }


        // Alla pressione del FAB notifico il viewModel che deciderà l'azione da intraprendere.
        fabChart.setOnClickListener {
            viewModel.onClickChartButton()
        }

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
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
     * Crea le view per la visualizzazione delle accelerazioni e le aggiunge al layout destinato
     * a contenerle.
     * @param containerAcc il layout destinato a contenere i box delle accelerazioni.
     */
    private fun addAccelerationBoxes(containerAcc: ConstraintLayout) {
        // Il layout contenitore è unico per tutte le accelerazioni.
        // Dopo aver creato ciascun box lo personalizzo e gli assegno un identificatore.
        // L'identificatore è necessario per poter fare riferimento alla view nella definizione dei
        // vincoli per il ConstraintLayout.
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


        // Aggiungo le view all layout.
        containerAcc.addView(bindingAccX.root)
        containerAcc.addView(bindingAccY.root)
        containerAcc.addView(bindingAccZ.root)


        // Aggancio il lato destro e sinistro di ciascun box rispettivamente al lato destro e
        // sinistro del layout che li contiene. Ogni box occuperà l'intera larghezza del layout.
        val cs = ConstraintSet()
        cs.connect(
            bindingAccX.root.id,
            ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT
        )
        cs.connect(
            bindingAccX.root.id,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT
        )

        cs.connect(
            bindingAccY.root.id,
            ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT
        )
        cs.connect(
            bindingAccY.root.id,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT
        )

        cs.connect(
            bindingAccZ.root.id,
            ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT
        )
        cs.connect(
            bindingAccZ.root.id,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT
        )


        // Dispongo i 3 box in una catena verticale in modo tale che si distribuiscano equamente
        // lungo tutta l'altezza del layout.
        val chain = arrayOf(bindingAccX.root.id, bindingAccY.root.id, bindingAccZ.root.id)
        cs.createVerticalChain(
            ConstraintSet.PARENT_ID, ConstraintSet.TOP,
            ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
            chain.toIntArray(), null, ConstraintSet.CHAIN_SPREAD
        )
        cs.applyTo(containerAcc)


        // Osservo le accelerazioni del model e aggiorno i valori visualizzati appena cambiano.
        viewModel.accelX.observe(viewLifecycleOwner, { value ->
            bindingAccX.accValue.text = "%.2f".format(value)

            // Quando la UI è ruotata rispetto all'orientamento naturale del dispositivo devo
            // correggere la direzione delle freccette che indicano gli assi del sensore.
            val angolo = getAngoloImmagine(0).toFloat()
            if (angolo != bindingAccX.accDirection.rotation)
                bindingAccX.accDirection.rotation = angolo
        })

        viewModel.accelY.observe(viewLifecycleOwner, { value ->
            bindingAccY.accValue.text = "%.2f".format(value)

            val angolo = getAngoloImmagine(0).toFloat()
            if (angolo != bindingAccY.accDirection.rotation)
                bindingAccY.accDirection.rotation = angolo
        })

        viewModel.accelZ.observe(viewLifecycleOwner, { value ->
            bindingAccZ.accValue.text = "%.2f".format(value)
        })
    }

    /**
     * Mostra un dialog che informa l'utente sul funzionamento in background.
     * Il dialog deve essere confermato.
     */
    private fun showDialogInfo() {
        context?.let { context ->
            MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.informazione))
                .setMessage(getString(R.string.funzionamento_in_background))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    viewModel.onDialogInfoOk()
                }
                .setCancelable(false)
                .show()
        }
    }

    /**
     * Quando ruoto il dispositivo tutta la UI ruota.
     * Con la UI ruotano anche le immagini che contiene.
     * Quando il dispositivo è nel suo orientamento naturale (generalmente portrait per uno smartphone)
     * la UI è orientata in modo tale che l'immagine di una freccia che punta verso l'alto risulta
     * parallela e concorde all'asse y del dispositivo.
     * Per fare in modo che anche ruotando la UI l'immagine rimanga concorde con il sistema di
     * riferimento del dispositivo, devo correggere l'angolo di rotazione dell'immagine sulla base
     * dell'orientamento attuale della UI.
     *
     * The coordinate-system is defined relative to the screen of the phone in its default orientation.
     * See [SensorEvent](https://developer.android.com/reference/android/hardware/SensorEvent).
     *
     * @param angolo angolo desiderato fra immagine e asse Y del device.
     * @return angolo di cui deve essere ruotata l'immagine per risultare inclinata dell'angolo richiesto
     * rispetto all'asse y.
     */
    private fun getAngoloImmagine(angolo: Int): Int {
        view?.display?.apply {
            return when (rotation) {
                Surface.ROTATION_0 -> angolo
                Surface.ROTATION_90 -> angolo - 90
                Surface.ROTATION_180 -> angolo - 180
                Surface.ROTATION_270 -> angolo - 270
                else -> angolo
            }
        }
        return angolo
    }
}