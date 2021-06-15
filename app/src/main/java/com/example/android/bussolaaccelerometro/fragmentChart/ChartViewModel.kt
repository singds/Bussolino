package com.example.android.bussolaaccelerometro.fragmentChart

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.android.bussolaaccelerometro.Repository
import com.example.android.bussolaaccelerometro.SensorSample
import com.github.mikephil.charting.charts.LineChart
import kotlinx.parcelize.Parcelize

class ChartViewModel(private val repo: Repository, private val state: SavedStateHandle): ViewModel()
{
    val realtimeListSample: LiveData<List<SensorSample>> by repo::listSample

    val stopped:LiveData<Boolean> = state.getLiveData(STATE_STOPPED, false)

    private var pSampleListOnPause:List<SensorSample>? = state.get(STATE_SAMPLE_LIST_ON_PAUSE)
    val sampleListStopped:List<SensorSample>? by ::pSampleListOnPause

    private var pChartsState:List<ChartState>? = state.get(STATE_CHARTS)
    val chartsState:List<ChartState>? by ::pChartsState

    fun onClickPlayPause() {
        realtimeListSample.value?.let { okList ->
            state.set(STATE_SAMPLE_LIST_ON_PAUSE, okList)
            pSampleListOnPause = state.get(STATE_SAMPLE_LIST_ON_PAUSE)
            state.set(STATE_STOPPED, stopped.value != true)
        }
    }

    fun saveChartsState(states: List<ChartState>?) {
        state.set(STATE_CHARTS, states)
        pChartsState = states
    }

    @Parcelize
    data class ChartState(
        // minimo valore visibile sull'asse x
        val xMin:Float,
        // range di valori visibili sull'asse x
        val xRange:Float
    ) : Parcelable

    companion object
    {
        const val STATE_STOPPED = "stopped"
        const val STATE_SAMPLE_LIST_ON_PAUSE = "sampleListOnPause"
        const val STATE_CHARTS = "charts"
    }
}


class ChartViewModelFactory(
    private val repo: Repository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs)
{
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
            return ChartViewModel(repo, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
