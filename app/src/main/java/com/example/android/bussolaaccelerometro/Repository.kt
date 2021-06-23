package com.example.android.bussolaaccelerometro

import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import java.util.*

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

//    /**
//     * Variabile memorizzata in modo permanente.
//     * True quando è abilitata la memorizzazione dei dati in background.
//     */
//    var enableRecordInBackground
//        get() = preferences.getBoolean(PREFERENCE_ENABLE_RECORD, false)
//        set(value) {
//            preferences.edit().putBoolean(PREFERENCE_ENABLE_RECORD, value).apply()
//        }

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
    private val pListSample = mutableListOf<SensorSample>()
    // Versione del campo pListSample, accessibile solo in lettura
    val listSample: List<SensorSample> get() = pListSample

    // Un oggetto osservabile che notifica gli osservatore quando la lista di campioni viene aggiornata.
    val newListSampleAvailable = object : Observable() {
        override fun hasChanged(): Boolean {
            return true
        }
    }

    /**
     * L'ultimo campione raccolto.
     */
    private var pCurrentSample = SensorSample(0f, 0f, 0f, 0f, Date())
    // Versione del campo pCurrentSample, accessibile solo in lettura
    val currentSample: SensorSample get() = pCurrentSample
    private var pCurrentMagneAccuracy:Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    // valore di accuratezza del magnetometro legato all'ultimo campione
    val currentMagneAccuracy by ::pCurrentMagneAccuracy

    // Un oggetto osservabile che notifica gli osservatore quando è disponibile un nuovo campione realtime.
    val newCurrentSampleAvailable = object : Observable() {
        override fun hasChanged(): Boolean {
            return true
        }
    }

    /**
     * Aggiunge un nuovo campione alla lista dello storico dei campioni.
     * La lista è una FIFO: il primo campione ad entrare è il primo ad uscire.
     * La lista raggiunge una dimensione che copre l'intervallo temporale di 5 minuti.
     * @param sample il nuovo campione
     */
    fun putSensorSampleToList(sample: SensorSample) {
        // Se lo storico ha raggiunto la dimensione massima rimuovo i campioni più vecchi in eccesso.
        while (pListSample.size >= NUM_CAMPIONI)
            pListSample.removeLast()

        // Aggiungo in testa il nuovo campione.
        pListSample.add(0, sample)

        newListSampleAvailable.notifyObservers()
    }

    /**
     * Aggiorna i dati realtime dei sensori.
     * @param sample campione con i nuovi dati acquisiti dai sensori.
     * @param magneAccuracy valore di accuratezza del magnetometro.
     */
    fun putSensorSampleToCurrent(sample: SensorSample, magneAccuracy: Int) {
        pCurrentSample = sample
        pCurrentMagneAccuracy = magneAccuracy

        newCurrentSampleAvailable.notifyObservers()
    }

    /**
     * Svuota lo storico dei campioni.
     */
    fun clearSampleList() {
        pListSample.clear()

        // Notifico gli osservatori.
        newListSampleAvailable.notifyObservers()
    }

    companion object {
//        /**
//         * Shared Preferences key: true quando il campionamento in background è abilitato.
//         */
//        private const val PREFERENCE_ENABLE_RECORD = "enableRecord"

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