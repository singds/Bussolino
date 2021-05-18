package com.example.android.bussolaaccelerometro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import java.util.*
import kotlin.collections.ArrayList


class SingleChartFragment : Fragment() {

    lateinit var chart:LineChart
    var startRefTime:Long = -1
    var chartViewMode = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_single_chart, container, false)

        chartViewMode = false
        chart = root.findViewById(R.id.chart) as LineChart

        setupEmptyChart(chart, -12f, 12f, getString(R.string.Accelerazione_x))
        return root
    }

    private fun setupEmptyChart(chart: LineChart, min: Float, max: Float, label: String, fill: Boolean = true, lineWidth: Float = 1.3f) {
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
            position = XAxis.XAxisPosition.BOTTOM
        }

        chart.axisLeft.apply {
            axisMaximum = max
            axisMinimum = min
        }

        chart.onChartGestureListener = GestureListener(chart)
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

    private fun refreshChart(chart: LineChart, values: List<Float>, times: List<Float>) {
        val data = chart.data
        val dataSet = data.getDataSetByIndex(0) as LineDataSet

        val arr = ArrayList<Entry>()
        for (k in values.indices)
            arr.add(Entry(times[k], values[k]))
        dataSet.values = arr

        chart.xAxis.valueFormatter = XAxisFormatter()
        dataSet.notifyDataSetChanged()
        data.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Repository.listSample.observe(viewLifecycleOwner) { list ->
            if (!chartViewMode) {
                if (startRefTime == -1L) {
                    val firstTimestamp = list[0].timestamp.time
                    startRefTime = firstTimestamp - (firstTimestamp % 60)
                }
                val listData = list.map { value -> value.accelX }

                val xvalues = ArrayList<Float>()
                for (elem in list) {
                    val floatTime = (elem.timestamp.time - startRefTime) / 1000f
                    xvalues.add(floatTime)
                }

                refreshChart(chart, listData, xvalues)
            }
        }
    }

    inner class XAxisFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val timestamp = startRefTime + (value * 1000).toLong()
            val calendar = Calendar.getInstance()
            calendar.setTime(Date(timestamp))
            return "%02d:%02d".format(calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND))
        }
    }

    override fun onStop() {
        super.onStop()
        startRefTime = -1
    }

    private inner class GestureListener(val chart:LineChart):
            OnChartGestureListener
    {
        override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
        }

        override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
        }

        override fun onChartLongPressed(me: MotionEvent?) {
        }

        override fun onChartDoubleTapped(me: MotionEvent?) {
            if (chartViewMode)
            {
                chart.isDragXEnabled = false
                chart.isScaleXEnabled = false
                chartViewMode = false
            } else
            {
                chart.isDragXEnabled = true
                chart.isScaleXEnabled = true
                chartViewMode = true
            }
        }

        override fun onChartSingleTapped(me: MotionEvent?) {
        }

        override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
        }

        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        }

        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        }

        private fun allineaGrafici(){
        }
    }
}