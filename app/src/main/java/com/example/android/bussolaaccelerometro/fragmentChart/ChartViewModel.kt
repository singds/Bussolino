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
    val stopped: LiveData<Boolean> = state.getLiveData(STATE_STOPPED, false)

    private val pChartSampleList = MutableLiveData<List<SensorSample>>()
    val chartSampleList: LiveData<List<SensorSample>> by ::pChartSampleList

    private var pChartsState: List<ChartState>? = state.get(STATE_CHARTS)
    val chartsState: List<ChartState>? by ::pChartsState

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
            pChartSampleList.value = state.get(STATE_SAMPLE_LIST_ON_PAUSE)
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
        repo.newCurrentSampleAvailable.deleteObserver(newListSampleObserver)
    }

    fun onClickPlayPause() {
        state.set(STATE_STOPPED, stopped.value != true)
        if (stopped.value == true) {

            // Cancello lo stato dei grafici precedentemente salvato.
            saveChartsState(null)

            val listSnapshot = repo.listSample.toList()
            state.set(STATE_SAMPLE_LIST_ON_PAUSE, listSnapshot)
            pChartSampleList.value = listSnapshot
        }
    }

    fun saveChartsState(states: List<ChartState>?) {
        state.set(STATE_CHARTS, states)
        pChartsState = states
    }

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
        const val STATE_SAMPLE_LIST_ON_PAUSE = "sampleListOnPause"
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
