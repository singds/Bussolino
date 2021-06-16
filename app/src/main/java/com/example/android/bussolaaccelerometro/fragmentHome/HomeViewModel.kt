package com.example.android.bussolaaccelerometro.fragmentHome

import androidx.lifecycle.*
import com.example.android.bussolaaccelerometro.Repository
import kotlin.math.roundToInt

/**
 * [HomeViewModel] contiene la logica di controllo per [HomeFragment].
 * Il viewModel recupera i dati grezzi e li trasforma preparandoli per la visualizzazione.
 */
class HomeViewModel(private val repo: Repository) : ViewModel() {
    // Estraggo le singole componenti dal campione più recente predisponendo dei fields comodamente
    // utilizzabili dalla UI.
    val accelX = Transformations.map(repo.currentSample) { it.accelX }
    val accelY = Transformations.map(repo.currentSample) { it.accelY }
    val accelZ = Transformations.map(repo.currentSample) { it.accelZ }
    val gradiNord = Transformations.map(repo.currentSample) { it.gradiNord.roundToInt() }

//    /**
//     * Sfrutto un'interessante proprietà di kotlin: la property delegation.
//     * Questa mi permette di definire un campo i cui metodi di accesso (getter e setter) sono
//     * ridiretto verso un campo di un'altra classe.
//     */
//    var enableRecordInBackground by repo::enableRecordInBackground

    private val pEvent = MutableLiveData<String?>()
    val event: LiveData<String?> by ::pEvent

    init {
        if (!repo.dialogInfoDone)
            pEvent.value = EVENT_SHOW_DIALOG_INFO
    }

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
