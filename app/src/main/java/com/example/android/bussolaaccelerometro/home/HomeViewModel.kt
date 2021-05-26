package com.example.android.bussolaaccelerometro.home

import androidx.lifecycle.*
import com.example.android.bussolaaccelerometro.data.Repository
import com.example.android.bussolaaccelerometro.main.MainActivityViewModel
import kotlin.math.roundToInt

class HomeViewModel(private val repo:Repository): ViewModel()
{
    // Estraggo le singole componenti dal campione più recente predisponendo dei campi
    // comodamente utilizzabili dalla UI.
    val accelX = Transformations.map(repo.currentSample) {it.accelX}
    val accelY = Transformations.map(repo.currentSample) {it.accelY}
    val accelZ = Transformations.map(repo.currentSample) {it.accelZ}
    val gradiNord = Transformations.map(repo.currentSample) { it.gradiNord.roundToInt() }

    /**
     * Sfrutto un'interessante proprietà di kotlin: la property delegation.
     * Questa mi permette di definire un campo i cui metodi di accesso (getter e setter) sono
     * ridiretto verso un campo di un'altra classe.
     */
    var enableRecordInBackground by repo::enableRecordInBackground

    private val pEvent = MutableLiveData<String?>()
    val event:LiveData<String?> by ::pEvent

    fun onClickChartButton() {
        pEvent.value = EVENT_GOTO_CHART_PAGE
    }

    fun onDialogInfoBackgoundOk() {
        repo.enableRecordInBackground
    }

    fun eventHandled() {
        pEvent.value = null
    }

    companion object
    {
        const val EVENT_GOTO_CHART_PAGE = "eventGotoChartPage"
        const val EVENT_SHOW_DIALOG_INFO_BACKGROUND = "eventShowDialogInfoBackground"
    }
}

class HomeViewModelFactory(private val repo:Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
