package com.example.android.bussolaaccelerometro

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet
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
 * Imposta la finestra visibile dell'asse x.
 * Poi aggiorna il grafico.
 * @param xMin minimo valore x visibile (estrema sinistra)
 * @param xRange range di valori visibili sull'asse x.
 */
fun LineChart.setXMinMax(xMin: Float, xRange: Float) {
    val minScale = viewPortHandler.minScaleX
    val maxScale = viewPortHandler.maxScaleX

    setVisibleXRange(xRange, xRange)
    viewPortHandler.setMinimumScaleX(minScale)
    viewPortHandler.setMaximumScaleX(maxScale)
    refreshCircleVisibility()
    moveViewToX(xMin) // questo aggiorna anche il grafico
}

/**
 * Imposta la dimensione massima per la finestra visibile dell'asse x. Tutti i campioni saranno visibili.
 * Poi aggiorna il grafico.
 */
fun LineChart.setXMinMaxFitScreen() {
    fitScreen()
    refreshCircleVisibility()
    invalidate()
}

/**
 * Abilita la visualizzazione di cerchietto e valore dei campioni quando il numero di
 * secondi visibili sull'asse X è inferiore ad una certa soglia.
 * @param dstChart chart di destinazione
 */
fun LineChart.refreshCircleVisibility() {
    // numero di secondi visibili a schermo
    val visibleSec = visibleXRange
    val dataSet = data.getDataSetByIndex(0) as LineDataSet

    if (visibleSec < 10f) {
        dataSet.setDrawValues(true)
        dataSet.setDrawCircles(true)
    } else {
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
    }
}
