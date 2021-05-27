package com.example.android.bussolaaccelerometro.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 *
 */
class Repository (private val context: Context)
{
    private val preferences:SharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    /**
     * True quando è abilitata la memorizzazione dei dati in background.
     */
    var enableRecordInBackground get() = preferences.getBoolean(PREFERENCE_ENABLE_RECORD, false)
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_ENABLE_RECORD, value).apply()
        }

    /**
     * True quando il dialog di informazione sull'esecuzione in background è stato confermato.
     */
    var dialogInfoDone get() = preferences.getBoolean(PREFERENCE_DIALOG_INFO_DONE, false)
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_DIALOG_INFO_DONE, value).apply()
        }


    /**
     * La lista dei campioni raccolti negli ultimi 5 minuti.
     * Il primo elemento della lista è quello più recente.
     */
    private val values = mutableListOf<SensorSample>()
    private var pListSample = MutableLiveData<List<SensorSample>>().apply { value = values }
    val listSample:LiveData<List<SensorSample>> by ::pListSample

    /**
     * Acquisisce un nuovo campione e lo aggiunge alla lista dello storico dei campioni.
     * La lista è una FIFO: ul primo campione ad entrare è il primo ad uscire.
     * La lista raggiunge una dimensione che copre l'intervallo temporale di 5 minuti.
     */
    fun putSensorSampleToList(sample:SensorSample)
    {
        // rimuovo i campioni più vecchi
        while (values.size >= NUM_CAMPIONI)
            values.removeLast()

        // aggiungo in testa il nuovo campione
        values.add(0, sample)

        // scateno una notifica degli osservatori
        pListSample.value = values
    }

    /**
     * L'ultimo campione acquisito aggiornato periodicamente con frequenza elevata.
     *
     * 0 = 360 = ago magnetico diretto a nord.
     * Ruotando in senso orario i gradi rispetto al nord aumentano da 0 a 360.
     * Asse y positivo del sistema di riferimento del dispositivo = ago magnetico.
     */
    private val pCurrentSample = MutableLiveData<SensorSample>()
    val currentSample:LiveData<SensorSample> by ::pCurrentSample

    /**
     * Aggiorna l'ultimo campione acquisito dai sensori.
     */
    fun putSensorSampleToCurrent(sample:SensorSample)
    {
        pCurrentSample.value = sample
    }


    companion object
    {
        /**
         * Shared Preference key: true quando il campionamento in background è abilitato.
         */
        private const val PREFERENCE_ENABLE_RECORD = "enableRecord"

        /**
         * Shared Preference key: true quando il popup di informazione sul campionamento in
         * background è stato confermato.
         */
        private const val PREFERENCE_DIALOG_INFO_DONE = "dialogInfoDone"

        /**
         * Numero di campioni che costituisce lo storico.
         */
        const val NUM_CAMPIONI = 600
    }
}