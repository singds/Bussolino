package com.example.android.bussolaaccelerometro.fragmentChart

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.android.bussolaaccelerometro.Repository
import com.example.android.bussolaaccelerometro.SensorSample
import kotlinx.parcelize.Parcelize
import java.util.Observer

/**
 * [ChartViewModel] contiene la logica di controllo per [ChartFragment].
 * Il viewModel recupera i dati grezzi e li trasforma preparandoli per la visualizzazione.
 */
class ChartViewModel(private val repo: Repository, private val state: SavedStateHandle) :
    ViewModel() {

    // true quando il chart è in modalità stopped
    val stopped: LiveData<Boolean> = state.getLiveData(STATE_STOPPED, false)

    /**
     * La lista dei campioni che sono visibili sui grafici.
     * Il fragment osserva questa lista e quando cambia aggiorna i grafici.
     * In modalità running contiene lo storico realtime dei campioni.
     * In modalità stopped contiene uno screenshot della lista dei campioni.
     */
    private val pChartSampleList = MutableLiveData<List<SensorSample>>()
    val chartSampleList: LiveData<List<SensorSample>> by ::pChartSampleList

    /**
     * Lista che contiene lo stato dei 4 grafici.
     * Viene memorizzata nell'instance state.
     */
    private var pChartsState: List<ChartState>? = state.get(STATE_CHARTS)
    val chartsState: List<ChartState>? by ::pChartsState


    // Observer che gestisce la disponibilità di nuovi campioni.
    // Appena è pronta una nuova lista, se sono in running, prelevo la lista dal repository e aggiorno
    // quella a disposizione dei grafici.
    private val newListSampleObserver: Observer =
        Observer { _, _ ->
            repo.listSample.let {
                if (stopped.value != true) {
                    pChartSampleList.value = it
                }
            }
        }

    init {
        repo.newListSampleAvailable.addObserver(newListSampleObserver)
        if (stopped.value == true) {
            pChartSampleList.value = state.get(STATE_SAMPLE_LIST_STOPPED)
        } else {
            pChartSampleList.value = repo.listSample
        }
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * It is useful when ViewModel observes some data and you need to clear this subscription
     * to prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        super.onCleared()

        // Smetto di osservare la lista dei campioni
        repo.newCurrentSampleAvailable.deleteObserver(newListSampleObserver)
    }

    /**
     * Chiamata dal fragment per notificare la pressione del FAB play/pausa.
     * Il viewModel implementa la logica dietro il pulsante.
     * Cambia lo stato del fragment (running/stopped) e fa uno screenshot della lista dei campioni
     * entrando nello stato stopped.
     */
    fun onClickPlayPause() {
        state.set(STATE_STOPPED, stopped.value != true)
        if (stopped.value == true) {

            // Cancello lo stato dei grafici precedentemente salvato.
            saveChartsState(null)

            val listSnapshot = repo.listSample.toList()
            state.set(STATE_SAMPLE_LIST_STOPPED, listSnapshot)
            pChartSampleList.value = listSnapshot
        }
    }

    /**
     * Invocato dal fragment per richiedere la memorizzazione dello stato dei grafici.
     * Lo stato dei grafici viene memorizzato nell'instance state.
     */
    fun saveChartsState(states: List<ChartState>?) {
        state.set(STATE_CHARTS, states)
        pChartsState = states
    }

    /**
     * Data class che racchiude le informazioni di stato di un grafico.
     */
    @Parcelize
    data class ChartState(
        // minimo valore visibile sull'asse x
        val xMin: Float,
        // range di valori visibili sull'asse x
        val xRange: Float,
        // il valore evidenziato del grafico se presente
        val xHighlight: Float?
    ) : Parcelable

    companion object {
        const val STATE_STOPPED = "stopped"
        const val STATE_SAMPLE_LIST_STOPPED = "sampleListStopped"
        const val STATE_CHARTS = "charts"
    }
}


class ChartViewModelFactory(
    private val repo: Repository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
            return ChartViewModel(repo, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
