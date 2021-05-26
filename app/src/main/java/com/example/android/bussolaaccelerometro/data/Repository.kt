package com.example.android.bussolaaccelerometro.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

/**
 *
 */
class Repository private constructor(context: Context)
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
    var runInBackgroundAccepted get() = preferences.getBoolean(PREFERENCE_RUN_IN_BACKGROUND_ACCEPTED, false)
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_RUN_IN_BACKGROUND_ACCEPTED, value).apply()
        }

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
        private const val PREFERENCE_RUN_IN_BACKGROUND_ACCEPTED = "runInBackgroundAccepted"


        private var instance:Repository? = null


        fun getInstance(context: Context):Repository
        {
            synchronized(this)
            {
                return instance?: {
                    val newInstance = Repository(context)
                    instance = newInstance
                    newInstance
                }()
            }
        }
    }
}