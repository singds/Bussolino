package com.example.android.bussolaaccelerometro

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.listener.BarLineChartTouchListener

/**
 * [MyLineChart] estende la classe [LineChart] per alcuni campi al grafico.
 *
 * Ho cercato per quanto possibile di ampliare le funzionalità dell'oggetto [LineChart] attraverso
 */
class MyLineChart(context: Context, attrs: AttributeSet) : LineChart(context, attrs) {
    var yUdm: String = ""
}

/**
 * Interrompi l'animazione del grafico.
 * Un esempio di animazione è quella che si verifica a causa di un flick: il grafico scorre manifestando
 * una certa inerzia anche dopo che il dito è stato sollevato dallo schermo.
 *
 * Interrompere l'animazione è necessario quando in un dato istante si desidera forzare la
 * visualizzazione di una precisa finestra del grafico. Se non viene interrotta, l'animazione
 * sovrascrive le impostazioni che vengono fatte.
 * Arrivare a queste due righe di codice è stato difficile ed ha richiesto l'analisi dei sorgenti
 * della libreria.
 */
fun LineChart.stopAnimations() {
    val listener = onTouchListener as BarLineChartTouchListener
    listener.stopDeceleration()
}

/**
 * Un fix per bypassare alcuni problemi riscontrati nella libreria MPAndroidChart.
 */
fun LineChart.performTransformation() {
    val listener = onTouchListener as BarLineChartTouchListener
    viewPortHandler.refresh(listener.matrix, this, false)
}

/**
 * Imposta la finestra visibile dell'asse x.
 * Poi aggiorna il grafico.
 * @param xMin minimo valore x visibile (estrema sinistra)
 * @param xRange range di valori visibili sull'asse x.
 */
fun LineChart.setXMinAndRange(xMin: Float, xRange: Float) {
    val minScale = viewPortHandler.minScaleX
    val maxScale = viewPortHandler.maxScaleX

    setVisibleXRange(xRange, xRange)
    viewPortHandler.setMinimumScaleX(minScale)
    viewPortHandler.setMaximumScaleX(maxScale)
    refreshCircleVisibility()
    moveViewToX(xMin) // questo aggiorna anche il grafico
}

/**
 * Imposta la visualizzazione dell'intero range di valor dell'asse x.
 * Poi aggiorna il grafico.
 */
fun LineChart.setXMinMaxFitScreen() {
    fitScreen()
    refreshCircleVisibility()
    invalidate()
}

/**
 * Abilita la visualizzazione dei cerchietto e dei valori dei campioni quando il numero di
 * secondi visibili sull'asse X è inferiore ad una certa soglia.
 */
fun LineChart.refreshCircleVisibility() {
    val visibleSec = visibleXRange
    val dataSet = data.getDataSetByIndex(0) as LineDataSet

    if (visibleSec < 10f) {
        //dataSet.setDrawValues(true)
        dataSet.setDrawCircles(true)
    } else {
        //dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
    }
}
