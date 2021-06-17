package com.example.android.bussolaaccelerometro.fragmentChart

import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.bussolaaccelerometro.*
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.collections.ArrayList

/**
 * [ChartFragment] visualizza i 4 grafici di accelerazione x,y,z e gradiNord.
 * I campioni sono raccolti in background con frequenza di 2Hz dal servizio [ReaderService].
 * Il fragment può essere nello stato running o nello stato stopped.
 * Il cambio di stato avviene premendo il FAB (play / stop).
 *
 * Nello stato running i grafici sono aggiornati appena la lista dello storico dei campioni viene
 * modificata. In questo stato i grafici visualizzano l'intero range di campioni e non permettono
 * zoom e traslazioni.
 *
 * Entrando nello stato stopped viene fatto uno screenshot dello storico dei campioni.
 * In questo stato i grafici mostrano lo screenshot dei campioni permettendo lo zoom e lo scorrimento
 * per ispezionare i singoli valori.
 * Il campionamento non viene mai interrotto e continua in background.
 * Ritornando allo stato running lo screenshot viene scartato e riprende la visualizzazione dei campioni
 * acquisti negli ultimi 5 minuti.
 */
class ChartFragment : Fragment() {
    private lateinit var viewModel: ChartViewModel

    // i quattro grafici
    private lateinit var chartAccX: MyLineChart
    private lateinit var chartAccY: MyLineChart
    private lateinit var chartAccZ: MyLineChart
    private lateinit var chartGradiNord: MyLineChart

    /**
     * Il timestamp che corrisponde al valore 0 nell'asse x.
     */
    private var oldestTimestamp: Long = 0

    private lateinit var cursorTime: TextView
    private lateinit var cursorValue: TextView
    private lateinit var cursorUdm: TextView
    private lateinit var cursorStatus: TextView

    // una lista che contiene tutti e quattro grafici
    private lateinit var allCharts: List<MyLineChart>

    /**
     * Called to have the fragment instantiate its user interface view.
     * This will be called between onCreate(Bundle) and onViewCreated(View, Bundle).
     * It is recommended to only inflate the layout in this method and move logic that operates
     * on the returned View to onViewCreated(View, Bundle).
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val repo = (requireActivity().application as MyApplication).repository
        val viewModelFactory = ChartViewModelFactory(repo, this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ChartViewModel::class.java)

        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    /**
     * Called immediately after onCreateView.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupero i riferimenti ai quattro grafici e li inizializzo.
        chartAccX = view.findViewById(R.id.chartAccX) as MyLineChart
        chartAccY = view.findViewById(R.id.chartAccY) as MyLineChart
        chartAccZ = view.findViewById(R.id.chartAccZ) as MyLineChart
        chartGradiNord = view.findViewById(R.id.chartGradiNord) as MyLineChart

        setupEmptyChart(
            chartAccX,
            -12f,
            12f,
            getString(R.string.accelerazione_x_udm),
            getString(R.string.udm_ms2)
        )
        setupEmptyChart(
            chartAccY,
            -12f,
            12f,
            getString(R.string.accelerazione_y_udm),
            getString(R.string.udm_ms2)
        )
        setupEmptyChart(
            chartAccZ,
            -12f,
            12f,
            getString(R.string.accelerazione_z_udm),
            getString(R.string.udm_ms2)
        )
        setupEmptyChart(
            chartGradiNord,
            0f,
            360f,
            getString(R.string.gradi_nord),
            getString(R.string.udm_gradi),
            false
        )

        allCharts = listOf(chartAccX, chartAccY, chartAccZ, chartGradiNord)

        val cursor = view.findViewById<View>(R.id.cursor)
        cursorTime = cursor.findViewById(R.id.txtTime)
        cursorValue = cursor.findViewById(R.id.txtValue)
        cursorUdm = cursor.findViewById(R.id.txtUdm)
        cursorStatus = cursor.findViewById(R.id.txtStatus)


        // Imposto un listener custom per le gesture dei grafici.
        // Voglio che i grafici delle accelerazioni siano sempre sincronizzati: zoom e traslazioni
        // su un grafico si ripercuotono sugli altri.
        // Il grafico dei gradi nord è invece indipendente.
        val accCharts = listOf(chartAccX, chartAccY, chartAccZ)
        for (chart in accCharts)
            chart.onChartGestureListener = ChartGestureListener(chart, accCharts)
        chartGradiNord.onChartGestureListener =
            ChartGestureListener(chartGradiNord, listOf(chartGradiNord))


        // Quando viene premuto il pulsante play/pausa inoltro l'azione al viewModel.
        val playPause = view.findViewById<FloatingActionButton>(R.id.playPause)
        playPause.setOnClickListener {
            viewModel.onClickPlayPause()
        }


        // Il viewModel espone la lista dei campioni che in ogni istante deve essere visibile sui grafici.
        // Quando la lista cambia rinfresco i grafici.
        // La lista cambia periodicamente quando il fragment è in stato running.
        // La lista rimane costante quando il fragment è in stato stopped.
        viewModel.chartSampleList.observe(viewLifecycleOwner) { samples ->
            samples?.let {
                if (viewModel.stopped.value == true) {
                    setChartsInStoppedMode(it)
                } else {
                    setChartsInRunningMode(it)
                }
            }
        }


        // Osservo lo stato running/stopped del fragment e aggiorno di conseguenza l'immagine del FAB.
        viewModel.stopped.observe(viewLifecycleOwner) { stopped ->
            when (stopped) {
                true -> {
                    cursorStatus.text = getString(R.string.stopped)
                    playPause.setImageResource(R.drawable.ic_play)
                }
                else -> {
                    cursorStatus.text = getString(R.string.running)
                    playPause.setImageResource(R.drawable.ic_pause)
                }
            }
        }
    }

    /**
     * Called when the Fragment is no longer resumed.
     */
    override fun onPause() {
        super.onPause()

        // Quando fragment viene messo in pausa salvo lo stato di tutti i grafici.
        // Lo stato di ciascun grafico include le coordinate dell'intervallo sull'asse X che sta visualizzando.
        val states: MutableList<ChartViewModel.ChartState> = mutableListOf()
        for (chart in allCharts)
            states.add(getChartState(chart))
        viewModel.saveChartsState(states)
    }

    /**
     * Genera un'oggetto che contiene lo stato attuale del grafico.
     * @param chart il grafico di cui ottenere lo stato
     */
    private fun getChartState(chart: MyLineChart): ChartViewModel.ChartState {
        var xHighlight: Float? = null
        if (chart.highlighted != null && chart.highlighted.isNotEmpty()) {
            xHighlight = chart.highlighted[0].x
        }
        return ChartViewModel.ChartState(
            chart.lowestVisibleX,
            chart.visibleXRange,
            xHighlight,
        )
    }

    /**
     * Ripristina lo stato del grafico precedentemente salvato.
     * @param chart il grafico di cui riprisinare lo stato
     * @param state lo stato memorizzato del grafico
     */
    private fun restoreChartState(chart: MyLineChart, state: ChartViewModel.ChartState) {
        chart.setXMinAndRange(state.xMin, state.xRange)
        if (state.xHighlight != null) {
            val yHighlight =
                chart.data.getDataSetByIndex(0).getEntryForXValue(state.xHighlight, 0f).y
            chart.highlightValue(state.xHighlight, 0)
            viewHighlightValue(state.xHighlight, yHighlight, chart.yUdm)
        }
    }

    /**
     * Aggiorna i grafici per la modalità stopped.
     * @param list lo screenshot statico dello storico dei campioni.
     */
    private fun setChartsInStoppedMode(list: List<SensorSample>) {
        // I grafici visualizzano uno screenshot dei campioni.
        setSensorSamplesInCharts(list)

        // Verifico se precedentemente è stato salvato lo stato dei grafici.
        // Lo stato dei grafici viene salvato ogni volta che il fragment viene messo in pausa, e
        // viene eliminato all'ingresso dello stato stopped dopo la pressione del FAP pausa.
        // Se esiste uno stato valido lo ripristino.
        val savedChartsState = viewModel.chartsState
        if (savedChartsState != null) {
            for (k in allCharts.indices)
                restoreChartState(allCharts[k], savedChartsState[k])
        } else {
            for (chart in allCharts)
                chart.setXMinMaxFitScreen()
        }

        for (chart in allCharts) {
            chart.setVisibleXRangeMinimum(1f) // 1 secondo di minimo intervallo visualizzabile sull'asse x
            chart.isDragXEnabled = true
            chart.isScaleXEnabled = true
            chart.isHighlightPerDragEnabled = true
            chart.isHighlightPerTapEnabled = true
        }
    }

    /**
     * Aggiorna i grafici per la modalità running.
     * @param list la lista dinamica dello storico dei campioni.
     */
    private fun setChartsInRunningMode(list: List<SensorSample>) {
        setSensorSamplesInCharts(list)

        removeAllHighlight()

        for (chart in allCharts) {
            chart.stopAnimations()
            chart.isDragXEnabled = false
            chart.isScaleXEnabled = false
            chart.isHighlightPerTapEnabled = false
            chart.isHighlightPerDragEnabled = false
            chart.setXMinMaxFitScreen()
        }
    }


    /**
     * Inizializza il chart e il suoi stile grafico.
     * Aggiunge al chart un DataSet placeholder vuoto.
     * @param chart il grafico da inizializzare
     * @param yMin il minimo valore visibile sull'asse y
     * @param yMax il massimo valore visibile sull'asse y
     * @param label una breve descrizione dei dati visualizzati
     * @param yUdm unità di misura dell'asse y
     * @param fill (default true) true per riempire con un colore chiaro la zona compresa fra la
     * curva del grafico e l'asse x.
     * @param lineWidth spessore della linea (1 = spessore di default)
     */
    private fun setupEmptyChart(
        chart: MyLineChart,
        yMin: Float,
        yMax: Float,
        label: String,
        yUdm: String,
        fill: Boolean = true,
        lineWidth: Float = 1.3f
    ) {

        // Ogni grafico può visualizzare uno o più DataSet.
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

        dataSet.highlightLineWidth = lineWidth
        dataSet.highLightColor = getColor(requireContext(), R.color.chart_highlight)


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
        chart.yUdm = yUdm

        chart.setOnChartValueSelectedListener(ChartValueSelectedListener(chart))

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
     */
    private fun setChartSamples(
        chart: MyLineChart,
        yValues: List<Float>,
        xTimes: List<Float>,
    ) {
        // Recuper i dati attualmente visualizzati nel grafico.
        val chartData = chart.data
        val dataSet = chartData.getDataSetByIndex(0) as LineDataSet

        // Popolo un array con le coppie (x,y) che sono state passate alla funzione.
        // Mi assicuro inoltre che la lista sia ordinata con x crescente.
        val points = mutableListOf<Entry>()
        for (k in xTimes.indices)
            points.add(Entry(xTimes[k], yValues[k]))
        points.sortBy { it.x }

        // Aggiorno il dataSet con il nuovo array di punti
        dataSet.values = points

        dataSet.notifyDataSetChanged()
        chartData.notifyDataChanged()
        chart.notifyDataSetChanged()
    }

    /**
     * Visualizza sui grafici la lista di campioni fornita.
     * @param list lista di campioni da visualizzare.
     */
    private fun setSensorSamplesInCharts(list: List<SensorSample>) {
        // Aggiorno il timestamp di riferimento per l'asse x del grafico.
        // Questa libreria non accetta date o timestamp come tipo di dato per l'asse x.
        // Sono così costretto ad usare dei float per rappresentare gli istanti temporali.
        oldestTimestamp = list[list.size - 1].timestamp.time

        val listAccX = list.map { value -> value.accelX }
        val listAccY = list.map { value -> value.accelY }
        val listAccZ = list.map { value -> value.accelZ }
        val listGradiNord = list.map { value -> value.gradiNord }

        val xValues = ArrayList<Float>()
        for (elem in list) {
            val floatTime = (elem.timestamp.time - oldestTimestamp) / 1000f
            xValues.add(floatTime)
        }

        setChartSamples(chartAccX, listAccX, xValues)
        setChartSamples(chartAccY, listAccY, xValues)
        setChartSamples(chartAccZ, listAccZ, xValues)
        setChartSamples(chartGradiNord, listGradiNord, xValues)
    }

    /**
     * Un formatter che restituisce le stringhe da visualizzare come label dell'asse x.
     */
    inner class XAxisFormatter : ValueFormatter() {
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

    private fun viewHighlightValue(x: Float, y: Float, udm: String) {
        // calcolo il timestamp corrispondente a questo valore
        val timestamp = oldestTimestamp + (x * 1000).toLong()
        val calendar = Calendar.getInstance()
        calendar.time = Date(timestamp)

        cursorUdm.text = udm
        cursorValue.text = "%.2f".format(y)
        cursorTime.text = "%02d:%02d:%02d".format(
            calendar.get(Calendar.HOUR),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )
    }

    /**
     * Rimuove l'highlight da tutti i grafici.
     */
    private fun removeAllHighlight() {
        for (chart in allCharts)
            chart.highlightValue(0f, -1, false)
        cursorUdm.text = ""
        cursorValue.text = ""
        cursorTime.text = ""
    }

    private inner class ChartValueSelectedListener(val chart: MyLineChart) :
        OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            // Rimuovo l'Highlight da tutti gli altri grafici diversi da quello appena selezionato.
            for (c in allCharts) {
                if (c != chart)
                    c.highlightValue(0f, -1, false)
            }
            e?.let { entry ->
                viewHighlightValue(entry.x, entry.y, chart.yUdm)
            }
        }

        override fun onNothingSelected() {
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
    private class ChartGestureListener(
        val chart: MyLineChart,
        val relatedCharts: List<MyLineChart>
    ) :
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
            Log.d(LOG_TAG, "onChartScale  $chart")
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
            Log.d(LOG_TAG, "onChartTranslate  $chart")
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
        private fun alignChart(dstChart: MyLineChart, srcChart: MyLineChart) {
            dstChart.stopAnimations()

            val srcVals = FloatArray(9)
            val dstVals = FloatArray(9)

            // get src chart translation matrix:
            val srcMatrix: Matrix = chart.viewPortHandler.matrixTouch
            srcMatrix.getValues(srcVals)

            // apply X axis scaling and position to dst charts:
            val dstMatrix: Matrix = dstChart.viewPortHandler.matrixTouch
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