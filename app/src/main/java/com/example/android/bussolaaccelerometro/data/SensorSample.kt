package com.example.android.bussolaaccelerometro.data

import java.util.*

/**
 * Un classe che memorizza i dati di un campionamento.
 */
data class SensorSample(
        /**
         * Angolo fra asse y del dispositivo e nord magnetico (in gradi).
         */
        val gradiNord:Float,

        /**
         * Accelerazione x (in m/s²).
         */
        val accelX:Float,

        /**
         * Accelerazione y (in m/s²).
         */
        val accelY:Float,

        /**
         * Accelerazione y (in m/s²).
         */
        val accelZ:Float,

        /**
         * Istante temporale in cui il campione è stato acquisito.
         */
        val timestamp: Date
)