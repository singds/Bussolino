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
import androidx.fragment.app.viewModels
import com.example.android.bussolaaccelerometro.R
import com.example.android.bussolaaccelerometro.data.ReaderService
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
    private val viewModel by viewModels<ChartViewModel>()

    lateinit var chartAccX:LineChart
    lateinit var chartAccY:LineChart
    lateinit var chartAccZ:LineChart
    lateinit var chartGradiNord:LineChart
    var holdMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_chart, container, false)

        chartAccX = root.findViewById(R.id.chartAccX) as LineChart
        chartAccY = root.findViewById(R.id.chartAccY) as LineChart
        chartAccZ = root.findViewById(R.id.chartAccZ) as LineChart
        chartGradiNord = root.findViewById(R.id.chartGradiNord) as LineChart

        setupEmptyChart(chartAccX, -12f, 12f, getString(R.string.accelerazione_x_udm))
        setupEmptyChart(chartAccY, -12f, 12f, getString(R.string.accelerazione_y_udm))
        setupEmptyChart(chartAccZ, -12f, 12f, getString(R.string.accelerazione_z_udm))
        setupEmptyChart(chartGradiNord, 0f, 360f, getString(R.string.gradi_nord), false)

        val hold = root.findViewById<FloatingActionButton>(R.id.hold)
        hold.setOnClickListener {
            if (holdMode) {
                holdMode = false
                hold.setImageResource(R.drawable.ic_lock_data)
                for (c in listOf(chartAccX, chartAccY, chartAccZ, chartGradiNord)) {
                    // per interrompere l'eventuale scrolling in corso
                    val listener = c.onTouchListener as BarLineChartTouchListener
                    listener.stopDeceleration()
                    c.isDragXEnabled = false
                    c.isScaleXEnabled = false
                }
            } else {
                holdMode = true
                hold.setImageResource(R.drawable.ic_unlock_data)
                for (c in listOf(chartAccX, chartAccY, chartAccZ, chartGradiNord)) {
                    c.isDragXEnabled = true
                    c.isScaleXEnabled = true
                }
            }
        }

        return root
    }

    private fun setupEmptyChart(
        chart: LineChart,
        min: Float,
        max: Float,
        label: String,
        fill: Boolean = true,
        lineWidth: Float = 1.3f
    ) {
        val arr = ArrayList<Entry>()
        for (k in 0 until ReaderService.NUM_CAMPIONI)
            arr.add(Entry(0f, 0f))

        val dataSet = LineDataSet(arr, label)
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = lineWidth
        dataSet.color = getColor(requireContext(), R.color.chart_line)
        dataSet.fillColor = getColor(requireContext(), R.color.chart_line_fill)
        dataSet.setDrawFilled(fill)
        dataSet.setFillFormatter { _, _ -> 0f }
        val chartData = LineData(dataSet)


        chart.xAxis.apply {
            granularity = 1f
            isGranularityEnabled = true
            position = XAxis.XAxisPosition.BOTTOM
        }

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
        chart.axisRight.isEnabled = false
        chart.data = chartData
        chart.invalidate()
    }

    private fun refreshChart(chart: LineChart, values: List<Float>, times: List<Float>, firstTimestamp:Long) {
        val data = chart.data
        val dataSet = data.getDataSetByIndex(0) as LineDataSet

        val arr = ArrayList<Entry>()
        for (k in values.size - 1 downTo 0)
            arr.add(Entry(times[k], values[k]))
        dataSet.values = arr
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)

        chart.xAxis.valueFormatter = XAxisFormatter(firstTimestamp)
        dataSet.notifyDataSetChanged()
        data.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.fitScreen()
        chart.invalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.listSample.observe(viewLifecycleOwner) {
            it?.let { list ->
                if (!holdMode) {
                    val firstTimestamp = list[list.size - 1].timestamp.time

                    val listAccX = list.map { value -> value.accelX }
                    val listAccY = list.map { value -> value.accelY }
                    val listAccZ = list.map { value -> value.accelZ }
                    val listGradiNord = list.map { value -> value.gradiNord }

                    val xvalues = ArrayList<Float>()
                    for (elem in list) {
                        val floatTime = (elem.timestamp.time - firstTimestamp) / 1000f
                        xvalues.add(floatTime)
                    }

                    refreshChart(chartAccX, listAccX, xvalues, firstTimestamp)
                    refreshChart(chartAccY, listAccY, xvalues, firstTimestamp)
                    refreshChart(chartAccZ, listAccZ, xvalues, firstTimestamp)
                    refreshChart(chartGradiNord, listGradiNord, xvalues, firstTimestamp)
                }
            }
        }
    }

    class XAxisFormatter(val firstTimestamp: Long) : ValueFormatter()
    {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String
        {
            val timestamp = firstTimestamp + (value * 1000).toLong()
            val calendar = Calendar.getInstance()
            calendar.time = Date(timestamp)
            val label = "%02d:%02d".format(calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))
            return label
        }
    }

    override fun onStop() {
        super.onStop()
    }

//    private class GestureListener(val chart: LineChart):
//            OnChartGestureListener
//    {
//        override fun onChartGestureStart(
//            me: MotionEvent?,
//            lastPerformedGesture: ChartTouchListener.ChartGesture?
//        ) {
//        }
//
//        override fun onChartGestureEnd(
//            me: MotionEvent?,
//            lastPerformedGesture: ChartTouchListener.ChartGesture?
//        ) {
//        }
//
//        override fun onChartLongPressed(me: MotionEvent?) {
//        }
//
//        override fun onChartDoubleTapped(me: MotionEvent?) {
//
//        }
//
//        override fun onChartSingleTapped(me: MotionEvent?) {
//        }
//
//        override fun onChartFling(
//            me1: MotionEvent?,
//            me2: MotionEvent?,
//            velocityX: Float,
//            velocityY: Float
//        ) {
//        }
//
//        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
//            val xMinSec = chart.lowestVisibleX
//            val xMaxSec = chart.highestVisibleX
//            // numero di secondi visibili a schermo
//            val visibleSec = xMaxSec - xMinSec
//            val dataSet = chart.data.getDataSetByIndex(0) as LineDataSet
//
//            if (visibleSec < 10f) {
//                dataSet.setDrawValues(true)
//                dataSet.setDrawCircles(true)
//            } else {
//                dataSet.setDrawValues(false)
//                dataSet.setDrawCircles(false)
//            }
//        }
//
//        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
//        }
//    }


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

            allineaGrafici ( )
            for (c in allCharts) {
                setVisibilitaPunti(c)
            }
        }

        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            Log.d(LOG_TAG, "onChartTranslate  ${chart.toString()}")

            allineaGrafici()
        }

        fun allineaGrafici()
        {
            val srcMatrix: Matrix
            val srcVals = FloatArray(9)
            var dstMatrix: Matrix
            val dstVals = FloatArray(9)

            for (c in allCharts) {
                if (c != chart) {
                    // per interrompere l'eventuale scrolling in corso
                    val listener = c.onTouchListener as BarLineChartTouchListener
                    listener.stopDeceleration()
                }
            }

            // get src chart translation matrix:
            srcMatrix = chart.getViewPortHandler().getMatrixTouch()
            srcMatrix.getValues(srcVals)

            // apply X axis scaling and position to dst charts:
            for (dstChart in allCharts) {
                if (dstChart != chart) {
                    if (dstChart.visibility == View.VISIBLE) {
                        dstMatrix = dstChart.viewPortHandler.matrixTouch
                        dstMatrix.getValues(dstVals)
                        dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X]
                        dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X]
                        dstMatrix.setValues(dstVals)
                        dstChart.viewPortHandler.refresh(dstMatrix, dstChart, true)
                    }
                }
            }
        }

        fun setVisibilitaPunti(c:LineChart) {
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