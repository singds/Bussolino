package com.example.android.bussolaaccelerometro.fragmentChart

import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.android.bussolaaccelerometro.Repository
import com.example.android.bussolaaccelerometro.SensorSample

class ChartViewModel(private val repo: Repository, private val state: SavedStateHandle): ViewModel()
{
    val listSample: LiveData<List<SensorSample>> by repo::listSample

    private var pSampleListInPausa:ArrayList<SensorSample>? = state.get(STATE_SAMPLE_LIST_IN_PAUSA)
    val sampleListInPausa:List<SensorSample>? get() = pSampleListInPausa

    val inPausa:LiveData<Boolean> = state.getLiveData(STATE_IN_PAUSA, false)

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
