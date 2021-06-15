package com.example.android.bussolaaccelerometro

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * [Repository] svolge il ruolo di accentratore per i dati comuni dell'applicazione.
 * Esiste una sola istanza di questa classe che viene creata all'avvio dell'app.
 * L'istanza è accessibile attraverso [MyApplication].
 *
 * [ReaderService] raccoglie i dati dai sensori e li pubblica nel repository.
 *
 * Il repository gestisce anche il salvataggio dei dati permanenti attraverso le [SharedPreferences].
 */
class Repository(private val context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    /**
     * Variabile memorizzata in modo permanente.
     * True quando è abilitata la memorizzazione dei dati in background.
     */
    var enableRecordInBackground
        get() = preferences.getBoolean(PREFERENCE_ENABLE_RECORD, false)
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_ENABLE_RECORD, value).apply()
        }

    /**
     * Variabile memorizzata in modo permanente.
     * True quando il dialog di informazione sull'esecuzione in background è stato confermato.
     */
    var dialogInfoDone
        get() = preferences.getBoolean(PREFERENCE_DIALOG_INFO_DONE, false)
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_DIALOG_INFO_DONE, value).apply()
        }


    /**
     * La lista dei campioni raccolti negli ultimi 5 minuti.
     * Il primo elemento della lista è quello più recente.
     */
    private val pListSampleValues = mutableListOf<SensorSample>()

    /**
     * Un LiveData che permette di osservare la lista dei campioni.
     */
    private var pListSample = MutableLiveData<List<SensorSample>>(pListSampleValues)

    // Versione pubblica readonly di pListSample
    val listSample: LiveData<List<SensorSample>> by ::pListSample

    /**
     * LiveData che racchiude l'ultimo campione acquisito.
     * Viene aggiornato periodicamente con frequenza elevata.
     */
    private val pCurrentSample = MutableLiveData<SensorSample>()

    // Versione pubblic readonly di pCurrentSample
    val currentSample: LiveData<SensorSample> by ::pCurrentSample

    /**
     * Aggiunge un nuovo campione alla lista dello storico dei campioni.
     * La lista è una FIFO: il primo campione ad entrare è il primo ad uscire.
     * La lista raggiunge una dimensione che copre l'intervallo temporale di 5 minuti.
     * @param sample il nuovo campione
     */
    fun putSensorSampleToList(sample: SensorSample) {
        // Se lo storico ha raggiunto la dimensione massima rimuovo i campioni più vecchi in eccesso.
        while (pListSampleValues.size >= NUM_CAMPIONI)
            pListSampleValues.removeLast()

        // Aggiungo in testa il nuovo campione.
        pListSampleValues.add(0, sample)

        // Scateno una notifica scrivendo il valore del LiveData.
        pListSample.value = pListSampleValues
    }

    /**
     * Aggiorna i dati realtime dei sensori.
     * @param sample campione con i nuovi dati acquisiti dai sensori.
     */
    fun putSensorSampleToCurrent(sample: SensorSample) {
        pCurrentSample.value = sample
    }

    /**
     * Svuota lo storico dei campioni.
     */
    fun clearSampleList() {
        pListSampleValues.clear()

        // Notifico gli osservatori.
        pListSample.value = pListSampleValues
    }

    companion object {
        /**
         * Shared Preferences key: true quando il campionamento in background è abilitato.
         */
        private const val PREFERENCE_ENABLE_RECORD = "enableRecord"

        /**
         * Shared Preferences key: true quando il popup di informazione sul campionamento in
         * background è stato confermato.
         */
        private const val PREFERENCE_DIALOG_INFO_DONE = "dialogInfoDone"

        /**
         * Massimo numero di campioni nello storico.
         */
        const val NUM_CAMPIONI = 600
    }
}