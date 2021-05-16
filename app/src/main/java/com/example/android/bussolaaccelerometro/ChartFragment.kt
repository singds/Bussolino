package com.example.android.bussolaaccelerometro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*
import kotlin.collections.ArrayList


class ChartFragment : Fragment() {

    lateinit var chartAccX:LineChart
    lateinit var chartAccY:LineChart
    lateinit var chartAccZ:LineChart
    lateinit var chartGradiNord:LineChart
    var firstXTime = Date()
    var firstXValue = 0f

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

        setupEmptyChart(chartAccX, -12f, 12f, getString(R.string.Accelerazione_x))
        setupEmptyChart(chartAccY, -12f, 12f, getString(R.string.Accelerazione_y))
        setupEmptyChart(chartAccZ, -12f, 12f, getString(R.string.Accelerazione_z))
        setupEmptyChart(chartGradiNord, 0f, 360f, getString(R.string.Gradi_nord), false)

        return root
    }

    private fun setupEmptyChart(chart: LineChart, min: Float, max: Float, label: String, fill: Boolean = true, lineWidth: Float = 1.5f) {
        val arr = ArrayList<Entry>()
        for (k in 0 until ReaderService.NUM_CAMPIONI)
            arr.add(Entry(0f, 0f))

        val dataSet = LineDataSet(arr, label)
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = lineWidth
        dataSet.fillColor = 0x02dac5
        dataSet.setDrawFilled(fill)
        dataSet.setFillFormatter { _, _ -> 0f }
        val chartData = LineData(dataSet)


        chart.xAxis.apply {
            granularity = 60 * 1000f // granularità del minuto
            isGranularityEnabled = true
            position = XAxis.XAxisPosition.BOTTOM
        }

        chart.axisLeft.apply {
            axisMaximum = max
            axisMinimum = min
        }

        chart.description.isEnabled = false
//        chart.onChartGestureListener = GestureListener(chart)
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

    private fun refreshChart(chart: LineChart, values: List<Float>, times: List<Float>) {
        val data = chart.data
        val dataSet = data.getDataSetByIndex(0) as LineDataSet

        val arr = ArrayList<Entry>()
        for (k in values.indices)
            arr.add(Entry(times[k], values[k]))
        dataSet.values = arr

        // minimo 30 secondi di campioni
//        chart.setVisibleXRange(60f, 60f)

        chart.xAxis.valueFormatter = XAxisFormatter()
        dataSet.notifyDataSetChanged()
        data.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Repository.listSampleTime.observe(viewLifecycleOwner) { time ->
            Repository.listSample?.let { list ->
                val listAccX = list.map { value -> value.accelX }
                val listAccY = list.map { value -> value.accelY }
                val listAccZ = list.map { value -> value.accelZ }
                val listGradiNord = list.map { value -> value.gradiNord }

                val listTimes = ArrayList<Float>()

                firstXTime = Date(time.time - ReaderService.MS_FRA_CAMPIONI * listAccX.size)
                val calendar = Calendar.getInstance()
                calendar.setTime(firstXTime)

                firstXValue = (calendar.get(Calendar.SECOND) * 1000 + calendar.get(Calendar.MILLISECOND)).toFloat()
                for (i in listAccX.indices)
                    listTimes.add(firstXValue + i * ReaderService.MS_FRA_CAMPIONI)

                refreshChart(chartAccX, listAccX, listTimes)
                refreshChart(chartAccY, listAccY, listTimes)
                refreshChart(chartAccZ, listAccZ, listTimes)
                refreshChart(chartGradiNord, listGradiNord, listTimes)
            }
        }
    }

    inner class XAxisFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val thisTime = Date(firstXTime.time + (value - firstXValue).toInt())
            val calendar = Calendar.getInstance()
            calendar.setTime(thisTime)
            return "%d:%02d".format(calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE))
        }
    }

//    private inner class GestureListener(val chart:LineChart):
//            OnChartGestureListener
//    {
//        override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
//        }
//
//        override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
//        }
//
//        override fun onChartLongPressed(me: MotionEvent?) {
//        }
//
//        override fun onChartDoubleTapped(me: MotionEvent?) {
//        }
//
//        override fun onChartSingleTapped(me: MotionEvent?) {
//        }
//
//        override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
//        }
//
//        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
//            allineaGrafici()
//        }
//
//        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
//            allineaGrafici()
//        }
//
//        private fun allineaGrafici(){
//            val xindex = chart.lowestVisibleX
//            val xrange = chart.visibleXRange
//            for (c in listOf(chartAccX, chartAccY, chartAccZ, chartGradiNord))
//            {
//                c.setVisibleXRange(xrange,xrange)
//                c.moveViewToX(xindex)
//                c.setVisibleXRange(60f, ReaderService.MAX_CAMPIONI.toFloat())
//            }
//        }
//    }
}