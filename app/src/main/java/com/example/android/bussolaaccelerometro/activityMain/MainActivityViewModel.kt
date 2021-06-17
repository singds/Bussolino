package com.example.android.bussolaaccelerometro.activityMain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.bussolaaccelerometro.Repository

/**
 * [MainActivityViewModel] contiene la logica di controllo per [MainActivity].
 * Il viewModel recupera i dati grezzi e li trasforma preparandoli per la visualizzazione.
 * Per il momento il viewModel è vuoto ma resta a disposizione per sviluppi futuri.
 */
class MainActivityViewModel(private val repo: Repository):ViewModel()


class MainActivityViewModelFactory(private val repo: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}