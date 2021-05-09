package com.example.android.bussolaaccelerometro

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


class ChartFragment : Fragment() {

    lateinit var chart:LineChart
    lateinit var chartArr:MutableList<Entry>
    lateinit var chartData:LineData
    lateinit var chartSet:LineDataSet

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_chart, container, false)

        chart = root.findViewById(R.id.chart) as LineChart

        chartArr = ArrayList()
        chartArr.add(Entry(0f, 0f))

        chartSet = LineDataSet(chartArr, "Label")
        chartSet.setDrawValues(false)
        chartSet.setDrawCircles(false)

        chartData = LineData(chartSet)

        chart.data = chartData
//        chart.xAxis.isEnabled = false
//        chart.axisLeft.isEnabled = false
//        chart.axisRight.isEnabled = false
//        chart.legend.isEnabled = false
//        chart.description.isEnabled = false
//        chart.minOffset = 0f
        chart.invalidate()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Repository.listSample.observe(viewLifecycleOwner) {
            chartArr.clear()
            for (k in it.indices) {
                chartArr.add(Entry(k.toFloat(), it[k].accelY))
            }
            chartSet.notifyDataSetChanged()
            chartData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    }
}