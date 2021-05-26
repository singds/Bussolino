package com.example.android.bussolaaccelerometro.chart

import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.android.bussolaaccelerometro.data.Repository
import com.example.android.bussolaaccelerometro.data.SensorSample
import com.example.android.bussolaaccelerometro.home.HomeViewModel

class ChartViewModel(private val repo:Repository, private val state: SavedStateHandle): ViewModel()
{
    val listSample: LiveData<List<SensorSample>> by repo::listSample

    private var pSampleListInPausa:ArrayList<SensorSample>? = state.get(STATE_SAMPLE_LIST_IN_PAUSA)
    val sampleListInPausa:List<SensorSample>? get() = pSampleListInPausa

    val inPausa:LiveData<Boolean> = state.getLiveData(STATE_IN_PAUSA)
    init {
        if (inPausa.value == null)
            state.set(STATE_IN_PAUSA, false)
    }

    fun onClickPlayPause() {
        listSample.value?.let { okList ->
            state.set(STATE_SAMPLE_LIST_IN_PAUSA, okList)
            pSampleListInPausa = state.get(STATE_SAMPLE_LIST_IN_PAUSA)
            state.set(STATE_IN_PAUSA, inPausa.value != true)
        }
    }

    companion object
    {
        const val STATE_IN_PAUSA = "inPausa"
        const val STATE_SAMPLE_LIST_IN_PAUSA = "sampleListInPausa"
    }
}


class ChartViewModelFactory(
        private val repo:Repository,
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

//class ChartViewModelFactory(private val repo:Repository, private val state: SavedStateHandle) : ViewModelProvider.Factory {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
//            return ChartViewModel(repo, state) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
