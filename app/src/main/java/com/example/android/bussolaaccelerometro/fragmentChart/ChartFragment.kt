package com.example.android.bussolaaccelerometro.fragmentChart

import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.bussolaaccelerometro.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.BarLineChartTouchListener
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.collections.ArrayList


class ChartFragment : Fragment() {
    lateinit var viewModel: ChartViewModel

    lateinit var chartAccX: LineChart
    lateinit var chartAccY: LineChart
    lateinit var chartAccZ: LineChart
    lateinit var chartGradiNord: LineChart
    lateinit var allCharts: List<LineChart>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val repo = (requireActivity().application as MyApplication).repository
        val viewModelFactory = ChartViewModelFactory(repo, this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ChartViewModel::class.java)

        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chartAccX = view.findViewById(R.id.chartAccX) as LineChart
        chartAccY = view.findViewById(R.id.chartAccY) as LineChart
        chartAccZ = view.findViewById(R.id.chartAccZ) as LineChart
        chartGradiNord = view.findViewById(R.id.chartGradiNord) as LineChart

        setupEmptyChart(chartAccX, -12f, 12f, getString(R.string.accelerazione_x_udm))
        setupEmptyChart(chartAccY, -12f, 12f, getString(R.string.accelerazione_y_udm))
        setupEmptyChart(chartAccZ, -12f, 12f, getString(R.string.accelerazione_z_udm))
        setupEmptyChart(chartGradiNord, 0f, 360f, getString(R.string.gradi_nord), false)

        allCharts = listOf(chartAccX, chartAccY, chartAccZ, chartGradiNord)

        val accCharts = listOf(chartAccX, chartAccY, chartAccZ)
        for (chart in accCharts)
            chart.onChartGestureListener = ChartGestureListener(chart, accCharts)
        chartGradiNord.onChartGestureListener =
            ChartGestureListener(chartGradiNord, listOf(chartGradiNord))


        val playPause = view.findViewById<FloatingActionButton>(R.id.playPause)
        playPause.setOnClickListener {
            viewModel.onClickPlayPause()
        }



        viewModel.listSample.observe(viewLifecycleOwner) { samples ->
            samples?.let { okSamples ->
                if (viewModel.inPausa.value == false) {
                    setSensorSamplesInCharts(okSamples)
                    for (chart in allCharts)
                        chart.setXMinMaxFitScreen()
                }
            }
        }

        viewModel.inPausa.observe(viewLifecycleOwner) { inPausa ->
            when (inPausa) {
                true -> {
                    playPause.setImageResource(R.drawable.ic_play)

                    setSensorSamplesInCharts(viewModel.sampleListOnPause!!)
                    for (k in allCharts.indices) {
                        viewModel.chartsState?.let { states ->
                            allCharts[k].setXMinMax(states[k].xMin, states[k].xRange)
                        }
                        if (viewModel.chartsState == null)
                            allCharts[k].setXMinMaxFitScreen()

                        allCharts[k].isDragXEnabled = true
                        allCharts[k].isScaleXEnabled = true
                    }
                }
                else -> {
                    playPause.setImageResource(R.drawable.ic_pause)

                    // elimino lo stato dei grafici precedentemente salvato
                    viewModel.saveChartsState(null)
                    for (chart in allCharts) {
                        chart.stopAnimations()
                        chart.isDragXEnabled = false
                        chart.isScaleXEnabled = false
                    }
                }
            }
        }
    }

    /**
     * Called when the Fragment is no longer resumed.
     */
    override fun onPause() {
        super.onPause()
        val states:MutableList<ChartViewModel.ChartState> = mutableListOf()
        for (chart in allCharts)
            states.add(ChartViewModel.ChartState(chart.lowestVisibleX, chart.visibleXRange))
        viewModel.saveChartsState(states)
    }

    /**
     * Inizializza il chart e il suoi stile grafico.
     * Aggiunge al chart un DataSet placeholder vuoto.
     * @param chart il grafico da inizializzare
     * @param yMin il minimo valore visibile sull'asse y
     * @param yMax il massimo valore visibile sull'asse y
     * @param label una breve descrizione dei dati visualizzati
     * @param fill (default true) true per riempire con un colore chiaro la zona compresa fra la
     * curva del grafico e l'asse x.
     * @param lineWidth spessore della linea (1 = spessore di default)
     */
    private fun setupEmptyChart(
        chart: LineChart,
        yMin: Float,
        yMax: Float,
        label: String,
        fill: Boolean = true,
        lineWidth: Float = 1.3f
    ) {

        // Ogni grafico può visualizzare uno o più Dataset.
        // Un DataSet incapsula un insieme di dati e diverse configurazioni per la loro visualizzazione.
        // Creo un DataSet inizialmente vuoto per il grafico.
        val dataSet = LineDataSet(listOf(Entry(0f, 0f)), label)
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = lineWidth
        dataSet.color = getColor(requireContext(), R.color.chart_line)
        dataSet.fillColor = getColor(requireContext(), R.color.chart_line_fill)
        dataSet.setDrawFilled(fill)
        if (fill) {
            // Modifico il filler.
            // La zona riempita sotto al grafico si estende così dalla linea della curva all'asse x.
            dataSet.setFillFormatter { _, _ -> 0f }
        }


        chart.xAxis.apply {
            // granularity = 1f garantisce che le label nell'asse x siano distanziate di almeno 1 sec
            granularity = 1f
            isGranularityEnabled = true
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = XAxisFormatter()
        }
        chart.axisRight.isEnabled = false
        chart.axisLeft.apply {
            axisMaximum = yMax
            axisMinimum = yMin
        }
        //chart.isDragDecelerationEnabled = false
        chart.description.isEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.isHighlightPerTapEnabled = false
        chart.isDragXEnabled = false
        chart.isScaleXEnabled = false
        chart.isDragYEnabled = false
        chart.isScaleYEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
        chart.data = LineData(dataSet)

        chart.invalidate()
    }

    /**
     * Imposta la lista di punti visualizzata sul grafico.
     * @param chart grafico da aggiornare.
     * @param yValues lista di coordinate y per i punti del grafico.
     * @param xTimes lista di coordinate x per i punti del grafico.
     * @param oldestTimestamp timestamp del primo punto della lista.
     */
    private fun setChartSamples(
        chart: LineChart,
        yValues: List<Float>,
        xTimes: List<Float>,
        oldestTimestamp: Long
    ) {
        // recuper i dati attualmente visualizzati nel grafico
        val chartData = chart.data
        val dataSet = chartData.getDataSetByIndex(0) as LineDataSet

        // Popolo un array con le coppie (x,y) che sono state passate alla funzione.
        // Mi assicuro inoltre che la lista sia ordinata con x crescente.
        val points = mutableListOf<Entry>()
        for (k in xTimes.indices)
            points.add(Entry(xTimes[k], yValues[k]))
        points.sortBy { it.x }

        dataSet.values = points

        // Aggiorno il timestamp di riferimento per l'asse x del grafico.
        // Questa libreria non accetta date o timestamp come tipo di dato per l'asse x.
        // Sono così costretto ad usare dei float per rappresentare gli istanti temporali.
        val formatter = chart.xAxis.valueFormatter as XAxisFormatter
        formatter.oldestTimestamp = oldestTimestamp

        dataSet.notifyDataSetChanged()
        chartData.notifyDataChanged()
        chart.notifyDataSetChanged()
    }

    /**
     * Visualizza sui grafici la lista di campioni fornita.
     * @param list lista di campioni da visualizzare.
     */
    private fun setSensorSamplesInCharts(list: List<SensorSample>) {
        val oldestTimestamp = list[list.size - 1].timestamp.time

        val listAccX = list.map { value -> value.accelX }
        val listAccY = list.map { value -> value.accelY }
        val listAccZ = list.map { value -> value.accelZ }
        val listGradiNord = list.map { value -> value.gradiNord }

        val xValues = ArrayList<Float>()
        for (elem in list) {
            val floatTime = (elem.timestamp.time - oldestTimestamp) / 1000f
            xValues.add(floatTime)
        }

        setChartSamples(chartAccX, listAccX, xValues, oldestTimestamp)
        setChartSamples(chartAccY, listAccY, xValues, oldestTimestamp)
        setChartSamples(chartAccZ, listAccZ, xValues, oldestTimestamp)
        setChartSamples(chartGradiNord, listGradiNord, xValues, oldestTimestamp)
    }

    /**
     * Un formatter che restituisce le stringhe da visualizzare come label dell'asse x.
     */
    class XAxisFormatter : ValueFormatter() {
        /**
         * Il timestamp che corrisponde al valore 0 nell'asse x.
         */
        var oldestTimestamp: Long = 0

        /**
         * Called when a value from an axis is to be formatted before being drawn.
         * In questo caso value è una misura di tempo in secondi.
         *
         * @param value the value to be formatted
         * @param axis  the axis the value belongs to
         * @return stringa di testo che rappresenta *value*
         */
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            var label = ""
            if (oldestTimestamp != 0L) {

                // calcolo il timestamp corrispondente a questo valore
                val timestamp = oldestTimestamp + (value * 1000).toLong()
                val calendar = Calendar.getInstance()
                calendar.time = Date(timestamp)
                label =
                    "%02d:%02d".format(calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))
            }
            return label
        }
    }

    /**
     * Un listener che intercetta le gesture del grafico.
     * Quando questo grafico viene zoomato o traslato la lista di grafici collegati subisce le stesse
     * trasformazioni.
     * @param chart il grafico a cui è collegato il listener. il grafico che subisce l'azione.
     * @param relatedCharts la lista di grafici collegati. Quando *chart* subisce una trasformazione
     * tutti i grafici di questa lista la subiscono a loro volta.
     */
    private class ChartGestureListener(val chart: LineChart, val relatedCharts: List<LineChart>) :
        OnChartGestureListener {

        override fun onChartGestureStart(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
        }

        override fun onChartGestureEnd(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
        }

        override fun onChartLongPressed(me: MotionEvent?) {
        }

        override fun onChartDoubleTapped(me: MotionEvent?) {
        }

        override fun onChartSingleTapped(me: MotionEvent?) {
        }

        override fun onChartFling(
            me1: MotionEvent?,
            me2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ) {
        }

        /**
         * Callbacks when the chart is scaled / zoomed via pinch zoom gesture.
         * Quando questo grafico viene zoomato eseguo lo stesso zoom su tutti gli altri
         * grafici collegati.
         * @param me
         * @param scaleX scalefactor on the x-axis
         * @param scaleY scalefactor on the y-axis
         */
        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
            Log.d(LOG_TAG, "onChartScale  ${chart.toString()}")
            alignChartsAndSetCirclesVisibility()
        }

        /**
         * Callbacks when the chart is moved / translated via drag gesture.
         * Quando questo grafico viene traslato eseguo la stessa traslazione su tutti gli altri
         * grafici collegati.
         * @param me
         * @param dX translation distance on the x-axis
         * @param dY translation distance on the y-axis
         */
        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            Log.d(LOG_TAG, "onChartTranslate  ${chart.toString()}")
            alignChartsAndSetCirclesVisibility()
        }

        /**
         * Allinea rispetto a questo i grafici collegati.
         * Inoltre imposta la visibilità di cerchietto e valore dei campioni su questo grafico e su
         * tutti quelli collegati.
         */
        private fun alignChartsAndSetCirclesVisibility() {
            chart.performTransformation()
            for (dstChart in relatedCharts) {
                if (dstChart != chart) {
                    alignChart(dstChart, chart)
                }
                dstChart.refreshCircleVisibility()
            }
        }

        /**
         * Copia zoom e posizione x da un grafico sorgente a un grafico destinazione.
         * Il grafico sorgente non viene modificato.
         * Al termine dell'operazione il grafico di destinazione visualizzerà lo stesso range di
         * valori X del grafico sorgente.
         * @param dstChart grafico di destinazione
         * @param srcChart grafico sorgente
         */
        private fun alignChart(dstChart: LineChart, srcChart: LineChart) {
            dstChart.stopAnimations()

            val srcVals = FloatArray(9)
            val dstVals = FloatArray(9)

            // get src chart translation matrix:
            val srcMatrix: Matrix = chart.viewPortHandler.matrixTouch
            srcMatrix.getValues(srcVals)

            // apply X axis scaling and position to dst charts:
            var dstMatrix: Matrix = dstChart.viewPortHandler.matrixTouch
            dstMatrix.getValues(dstVals)

            dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X]
            dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X]
            dstMatrix.setValues(dstVals)
            dstChart.viewPortHandler.refresh(dstMatrix, dstChart, true)
        }

        companion object {
            const val LOG_TAG = "GestureListener"
        }
    }

}