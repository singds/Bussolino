package com.example.android.bussolaaccelerometro.fragmentHome

import androidx.lifecycle.*
import com.example.android.bussolaaccelerometro.Repository
import java.util.Observer
import kotlin.math.roundToInt

/**
 * [HomeViewModel] contiene la logica di controllo per [HomeFragment].
 * Il viewModel recupera i dati grezzi e li trasforma preparandoli per la visualizzazione.
 */
class HomeViewModel(private val repo: Repository) : ViewModel() {
    private val pAccelX = MutableLiveData(0f)
    val accelX: LiveData<Float> by ::pAccelX

    private val pAccelY = MutableLiveData(0f)
    val accelY: LiveData<Float> by ::pAccelY

    private val pAccelZ = MutableLiveData(0f)
    val accelZ: LiveData<Float> by ::pAccelZ

    private val pGradiNord = MutableLiveData(0)
    val gradiNord: LiveData<Int> by ::pGradiNord

    // rendo disponibile al fragment l'accuratezza dell'ultima lettura del magnetometro
    val magneAccuracy by repo::currentMagneAccuracy

    // Quando è disponibile un nuovo campione ne estraggo le singole componenti e le dispongo in
    // una serie di campi facilmente utilizzabili dalla UI.
    private val newCurrentSampleObserver: Observer =
        Observer { _, _ ->
            repo.currentSample.let {
                pAccelX.value = it.accelX
                pAccelY.value = it.accelY
                pAccelZ.value = it.accelZ
                pGradiNord.value = it.gradiNord.roundToInt()
            }
        }

    private val pEvent = MutableLiveData<String?>()
    val event: LiveData<String?> by ::pEvent

    init {
        // Mi registro per essere notificato quando un nuovo campione è disponibile.
        repo.newCurrentSampleAvailable.addObserver(newCurrentSampleObserver)

        // Se il dialog con le informazioni sul funzionamento in background non è ancora stato
        // visto e confermato dall'utente lo mostro.
        if (!repo.dialogInfoDone)
            pEvent.value = EVENT_SHOW_DIALOG_INFO
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * It is useful when ViewModel observes some data and you need to clear this subscription
     * to prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        super.onCleared()
        // Cancello la registrazione quando il viewModel viene distrutto.
        repo.newCurrentSampleAvailable.deleteObserver(newCurrentSampleObserver)
    }

    //    /**
//     * Sfrutto un'interessante proprietà di kotlin: la property delegation.
//     * Questa mi permette di definire un campo i cui metodi di accesso (getter e setter) sono
//     * ridiretto verso un campo di un'altra classe.
//     */
//    var enableRecordInBackground by repo::enableRecordInBackground

    /**
     * Chiamato dalla view quando viene premuto il FAB chart.
     * Chiedo al fragment di passare alla pagina dei grafici.
     */
    fun onClickChartButton() {
        pEvent.value = EVENT_GOTO_CHART_PAGE
    }

    /**
     * Chiamato dalla view quando il dialog di informazione è stato confermato dall'utente.
     * Memorizzo permanentemente la conferma dell'utente. Il dialog non verrà più ripresentato.
     */
    fun onDialogInfoOk() {
        repo.dialogInfoDone = true
    }

    /**
     * Chiamato dalla view per notificare il controller che l'evento / comando è stato gestito.
     */
    fun eventHandled() {
        pEvent.value = null
    }

    companion object {
        const val EVENT_GOTO_CHART_PAGE = "eventGotoChartPage"
        const val EVENT_SHOW_DIALOG_INFO = "eventShowDialogInfo"
    }
}

class HomeViewModelFactory(private val repo: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
