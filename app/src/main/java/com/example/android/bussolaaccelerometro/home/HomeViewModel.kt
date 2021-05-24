package com.example.android.bussolaaccelerometro.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.android.bussolaaccelerometro.data.Repository
import kotlin.math.roundToInt

class HomeViewModel: ViewModel()
{
    // Estraggo le singole componenti dal campione più recente predisponendo dei campi
    // comodamente utilizzabili dalla UI.
    val accelX = Transformations.map(Repository.currentSample) {it.accelX}
    val accelY = Transformations.map(Repository.currentSample) {it.accelY}
    val accelZ = Transformations.map(Repository.currentSample) {it.accelZ}
    val gradiNord = Transformations.map(Repository.currentSample) { it.gradiNord.roundToInt() }

    /**
     * Sfrutto un'interessante proprietà di kotlin: la property delegation.
     * Questa mi permette di definire un campo i cui metodi di accesso (getter e setter) sono
     * ridiretto verso un campo di un'altra classe.
     */
    var enableRecordInBackground by Repository::enableRecordInBackground

    private val pEvent = MutableLiveData<String?>()
    val event:LiveData<String?> by ::pEvent

    fun onClickChartButton() {
        pEvent.value = EVENT_GOTO_CHART_PAGE
    }

    fun eventHandled() {
        pEvent.value = null
    }

    companion object
    {
        const val EVENT_GOTO_CHART_PAGE = "eventGotoChartPage"
    }
}