package com.example.android.bussolaaccelerometro.chart

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
import com.example.android.bussolaaccelerometro.R
import com.example.android.bussolaaccelerometro.data.Repository
import com.example.android.bussolaaccelerometro.data.SensorSample
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
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

    lateinit var viewModel:ChartViewModel

    lateinit var chartAccX:LineChart
    lateinit var chartAccY:LineChart
    lateinit var chartAccZ:LineChart
    lateinit var chartGradiNord:LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val repo = Repository.getInstance(requireActivity().applicationContext)
        val viewModelFactory = ChartViewModelFactory(repo, this)
        viewModel = ViewModelProvider(this,viewModelFactory).get(ChartViewModel::class.java)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    private fun setupEmptyChart(
        chart: LineChart,
        min: Float,
        max: Float,
        label: String,
        fill: Boolean = true,
        lineWidth: Float = 1.3f
    ) {

        // Comincio con un dataset con un singolo valore (0,0)
        val dataSet = LineDataSet(listOf(Entry(0f,0f)), label)
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = lineWidth
        dataSet.color = getColor(requireContext(), R.color.chart_line)
        dataSet.fillColor = getColor(requireContext(), R.color.chart_line_fill)
        dataSet.setDrawFilled(fill)
        // Modifico il filler in modo tale che il riempimento colorato sotto al grafico sia
        // sempre compreso fra  y = 0 e y = valore del punto.
        dataSet.setFillFormatter { _, _ -> 0f }

        val chartData = LineData(dataSet)

        chart.xAxis.apply {
            // granularity garantisce una distanza minima fra due label lungo l'asse x
            granularity = 1f
            isGranularityEnabled = true
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = XAxisFormatter()
        }
        chart.axisRight.isEnabled = false
        chart.axisLeft.apply {
            axisMaximum = max
            axisMinimum = min
        }

//        chart.isDragDecelerationEnabled = false
        chart.onChartGestureListener = GestureListener(chart, listOf(chartAccX, chartAccY, chartAccZ, chartGradiNord))
        chart.description.isEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.isHighlightPerTapEnabled = false

        chart.isDragXEnabled = false
        chart.isScaleXEnabled = false
        chart.isDragYEnabled = false
        chart.isScaleYEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)


        chart.data = chartData
        chart.invalidate()
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

        val playPause = view.findViewById<FloatingActionButton>(R.id.playPause)
        playPause.setOnClickListener {
            viewModel.onClickPlayPause()
        }



        viewModel.listSample.observe(viewLifecycleOwner) { samples ->
            if ((viewModel.inPausa.value == false) && (samples != null))
                setSensorSamplesInCharts(samples)
        }

        val allCharts = listOf(chartAccX, chartAccY, chartAccZ, chartGradiNord)
        viewModel.inPausa.observe(viewLifecycleOwner) { inPausa ->
            when(inPausa)
            {
                true -> {
                    setSensorSamplesInCharts (viewModel.sampleListInPausa)
                    playPause.setImageResource(R.drawable.ic_play)
                    for (c in allCharts) {
                        c.isDragXEnabled = true
                        c.isScaleXEnabled = true
                    }
                }
                else -> {
                    playPause.setImageResource(R.drawable.ic_pause)
                    for (c in allCharts) {
                        // per interrompere l'eventuale scrolling in corso
                        val listener = c.onTouchListener as BarLineChartTouchListener
                        listener.stopDeceleration()
                        c.isDragXEnabled = false
                        c.isScaleXEnabled = false
                    }
                }
            }
        }
    }

    /**
     * Rinfresca la visualizzazione del grafico con i nuovi valori forniti.
     * @param chart grafico da aggiornare
     * @param yValues lista di coordinate y per i punti del grafico.
     * @param xTimes lista di coordinate x per i punti del grafico.
     * @param oldestTimestamp timestamp
     */
    private fun refreshChart(chart: LineChart, yValues: List<Float>, xTimes: List<Float>, oldestTimestamp:Long)
    {
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
        // Quando i grafici sono visualizzati dinamicamente nascondo i cerchietti dei campioni e il loro valore
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)

        // Aggiorno il timestamp di riferimento per l'asse x del grafico.
        // Questa libreria non accetta date o timestamp come tipo di dato per l'asse x.
        // Sono così costretto ad usare dei float per rappresentare gli istanti temporali.
        val formatter = chart.xAxis.valueFormatter as XAxisFormatter
        formatter.oldestTimestamp = oldestTimestamp


        dataSet.notifyDataSetChanged()
        chartData.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.fitScreen()
        chart.invalidate()
    }

    private fun setSensorSamplesInCharts(list:List<SensorSample>?)
    {
        if (list != null) {
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

            refreshChart(chartAccX, listAccX, xValues, oldestTimestamp)
            refreshChart(chartAccY, listAccY, xValues, oldestTimestamp)
            refreshChart(chartAccZ, listAccZ, xValues, oldestTimestamp)
            refreshChart(chartGradiNord, listGradiNord, xValues, oldestTimestamp)
        }
    }

    class XAxisFormatter() : ValueFormatter()
    {
        var oldestTimestamp:Long = 0

        override fun getAxisLabel(value: Float, axis: AxisBase?): String
        {
            var label = ""
            if (oldestTimestamp != 0L) {
                val timestamp = oldestTimestamp + (value * 1000).toLong()
                val calendar = Calendar.getInstance()
                calendar.time = Date(timestamp)
                label = "%02d:%02d".format(calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))
            }
            return label
        }
    }


    private class GestureListener(val chart: LineChart, val allCharts: List<LineChart>):
            OnChartGestureListener
    {
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

        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
            Log.d(LOG_TAG, "onChartScale  ${chart.toString()}")

            setVisibilitaPunti(chart)

//            allineaGrafici ( )
//            for (c in allCharts) {
//                setVisibilitaPunti(c)
//            }
        }

        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            Log.d(LOG_TAG, "onChartTranslate  ${chart.toString()}")

//            allineaGrafici()
        }

//        fun allineaGrafici()
//        {
//            val srcMatrix: Matrix
//            val srcVals = FloatArray(9)
//            var dstMatrix: Matrix
//            val dstVals = FloatArray(9)
//
//            for (c in allCharts) {
//                if (c != chart) {
//                    // per interrompere l'eventuale scrolling in corso
//                    val listener = c.onTouchListener as BarLineChartTouchListener
//                    listener.stopDeceleration()
//                }
//            }
//
//            // get src chart translation matrix:
//            srcMatrix = chart.getViewPortHandler().getMatrixTouch()
//            srcMatrix.getValues(srcVals)
//
//            // apply X axis scaling and position to dst charts:
//            for (dstChart in allCharts) {
//                if (dstChart != chart) {
//                    if (dstChart.visibility == View.VISIBLE) {
//                        dstMatrix = dstChart.viewPortHandler.matrixTouch
//                        dstMatrix.getValues(dstVals)
//                        dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X]
//                        dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X]
//                        dstMatrix.setValues(dstVals)
//                        dstChart.viewPortHandler.refresh(dstMatrix, dstChart, true)
//                    }
//                }
//            }
//        }

        fun setVisibilitaPunti(c:LineChart)
        {
            val xMinSec = c.lowestVisibleX
            val xMaxSec = c.highestVisibleX
            // numero di secondi visibili a schermo
            val visibleSec = xMaxSec - xMinSec
            val dataSet = c.data.getDataSetByIndex(0) as LineDataSet

            if (visibleSec < 10f) {
                dataSet.setDrawValues(true)
                dataSet.setDrawCircles(true)
            } else {
                dataSet.setDrawValues(false)
                dataSet.setDrawCircles(false)
            }
        }

        companion object
        {
            const val LOG_TAG = "GestureListener"
        }
    }

}