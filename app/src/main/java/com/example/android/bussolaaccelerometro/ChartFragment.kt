package com.example.android.bussolaaccelerometro

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultFillFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import java.lang.invoke.LambdaConversionException


class ChartFragment : Fragment() {

    lateinit var chartAccX:LineChart
    lateinit var chartAccY:LineChart
    lateinit var chartAccZ:LineChart
    lateinit var chartGradiNord:LineChart

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

        setupEmptyChart(chartAccX, -12f,12f, getString(R.string.Accelerazione_x))
        setupEmptyChart(chartAccY, -12f,12f, getString(R.string.Accelerazione_y))
        setupEmptyChart(chartAccZ, -12f,12f, getString(R.string.Accelerazione_z))
        setupEmptyChart(chartGradiNord, 0f,360f, getString(R.string.Gradi_nord),false)

        return root
    }

    private fun setupEmptyChart(chart:LineChart, min:Float, max:Float, label:String, fill:Boolean=true, lineWidth:Float=1.5f) {
        val arr = ArrayList<Entry>()
        for (k in 0 until ReaderService.MAX_CAMPIONI)
            arr.add(Entry(0f, 0f))

        val dataSet = LineDataSet(arr, label)
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = lineWidth
        dataSet.fillColor = 0x02dac5
        dataSet.setDrawFilled(fill)
        dataSet.setFillFormatter { _, _ -> 0f }
        val chartData = LineData(dataSet)


        chart.description.isEnabled = false
        chart.axisLeft.axisMaximum = max
        chart.axisLeft.axisMinimum = min
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//        chart.onChartGestureListener = GestureListener(chart)
        chart.isHighlightPerDragEnabled = false
        chart.isHighlightPerTapEnabled = false
        chart.isDragXEnabled = true
        chart.isScaleXEnabled = true
        chart.isDragYEnabled = false
        chart.isScaleYEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
        chart.axisRight.isEnabled = false
        chart.data = chartData
        chart.invalidate()
    }

    private fun refreshChart(chart:LineChart, values:List<Float>, times:List<Float>) {
        val data = chart.data
        val dataSet = data.getDataSetByIndex(0) as LineDataSet

        val arr = ArrayList<Entry>()
        for (k in values.indices)
            arr.add(Entry(times[k],values[k]))
        dataSet.values = arr

        // minimo 30 secondi di campioni
//        chart.setVisibleXRange(60f, 60f)

        dataSet.notifyDataSetChanged()
        data.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Repository.listSample.observe(viewLifecycleOwner) {
            it?.let {
                val listAccX = it.map { value -> value.sample.accelX }
                val listAccY = it.map { value -> value.sample.accelY }
                val listAccZ = it.map { value -> value.sample.accelZ }
                val listGradiNord = it.map { value -> value.sample.gradiNord }
                val listTimes = it.map { value -> value.time.toFloat() }

                refreshChart(chartAccX,listAccX,listTimes)
                refreshChart(chartAccY,listAccY,listTimes)
                refreshChart(chartAccZ,listAccZ,listTimes)
                refreshChart(chartGradiNord,listGradiNord,listTimes)
            }
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