package com.example.android.bussolaaccelerometro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


class ChartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_chart, container, false)

        val chart = root.findViewById(R.id.chart) as LineChart
        val entries: MutableList<Entry> = ArrayList()
        entries.add(Entry(0f, 0f))
        entries.add(Entry(1f, 1f))
        entries.add(Entry(2f, 2f))
        val dataSet = LineDataSet(entries, "Label")
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        val lineData = LineData(dataSet)
        chart.xAxis.isEnabled = false
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.data = lineData
        chart.invalidate()
        chart.minOffset = 0f

        return root
    }
}