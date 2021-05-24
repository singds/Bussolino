package com.example.android.bussolaaccelerometro.data

import androidx.lifecycle.MutableLiveData

/**
 *
 */
object Repository
{
    /**
     * L'ultimo campione acquisito aggiornato periodicamente con frequenza elevata.
     *
     * 0 = 360 = ago magnetico diretto a nord.
     * Ruotando in senso orario i gradi rispetto al nord aumentano da 0 a 360.
     * Asse y positivo del sistema di riferimento del dispositivo = ago magnetico.
     */
    val currentSample = MutableLiveData<SensorSample>()

    /**
     * La lista dei campioni raccolti negli ultimi 5 minuti.
     * Il primo elemento della lista è quello più recente.
     */
    var listSample = MutableLiveData<List<SensorSample>>()

    /**
     * True quando è abilitata la memorizzazione dei dati in background.
     */
    var enableRecordInBackground = false

    var runInBackgroundAccepted = false
}