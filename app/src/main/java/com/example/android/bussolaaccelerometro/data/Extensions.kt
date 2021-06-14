package com.example.android.bussolaaccelerometro.data

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.listener.BarLineChartTouchListener

/**
 * Interrompi l'animazione del grafico.
 * Un esempio di animazione è quella che si verifica a causa di un flick: il grafico scorre manifestando
 * una certa inerzia anche dopo che il dito è stato sollevato dallo schermo.
 */
fun LineChart.stopAnimations() {
    val listener = onTouchListener as BarLineChartTouchListener
    listener.stopDeceleration()
}

/**
 *
 */
fun LineChart.performTransformation() {
    val listener = onTouchListener as BarLineChartTouchListener
    viewPortHandler.refresh(listener.matrix, this, false);
}

/**
 * Imposta la finestra visibile dell'asse x nel grafico.
 * @param xMin minimo valore x visibile (estrema sinistra)
 * @param xMax massimo valore x visibile (estrema destra)
 */
fun LineChart.setVisibleXRange(xMin: Float, xMax: Float) {
    val xMin = lowestVisibleX
    val xRange = visibleXRange
    val minScale = viewPortHandler.minScaleX
    val maxScale = viewPortHandler.maxScaleX

    setVisibleXRange(xRange, xRange)
    viewPortHandler.setMinimumScaleX(minScale)
    viewPortHandler.setMaximumScaleX(maxScale)
//    setVisibleXRangeMinimum(0f)
//    setVisibleXRangeMaximum(10000f)
    moveViewToX(xMin)
}
