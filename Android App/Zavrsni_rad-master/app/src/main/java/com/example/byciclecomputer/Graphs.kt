package com.example.byciclecomputer

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter


@Composable
fun DistanceChart(week: Week) {
    val chartData = listOf(
        "P" to week.GetMonday().getDistance().toFloat(),
        "U" to week.GetTuesday().getDistance().toFloat(),
        "S" to week.GetWednesday().getDistance().toFloat(),
        "Č" to week.GetThursday().getDistance().toFloat(),
        "P" to week.GetFriday().getDistance().toFloat(),
        "S" to week.GetSaturday().getDistance().toFloat(),
        "N" to week.GetSunday().getDistance().toFloat(),
    )
    CustomBarChart(chartData, Color(0xFF04e762),"Distanca(km)")
}

@Composable
fun VelocityChart(week: Week) {
    val chartData = listOf(
        "P" to week.GetMonday().getAverageVelocity().toFloat(),
        "U" to week.GetTuesday().getAverageVelocity().toFloat(),
        "S" to week.GetWednesday().getAverageVelocity().toFloat(),
        "Č" to week.GetThursday().getAverageVelocity().toFloat(),
        "P" to week.GetFriday().getAverageVelocity().toFloat(),
        "S" to week.GetSaturday().getAverageVelocity().toFloat(),
        "N" to week.GetSunday().getAverageVelocity().toFloat(),
    )
    CustomBarChart(chartData, Color(0xFF008bf8),"Prosječna brzina(km/h)")
}

@Composable
fun CaloriesChart(week: Week) {
    val chartData = listOf(
        "P" to week.GetMonday().getCalories().toFloat(),
        "U" to week.GetTuesday().getCalories().toFloat(),
        "S" to week.GetWednesday().getCalories().toFloat(),
        "Č" to week.GetThursday().getCalories().toFloat(),
        "P" to week.GetFriday().getCalories().toFloat(),
        "S" to week.GetSaturday().getCalories().toFloat(),
        "N" to week.GetSunday().getCalories().toFloat(),
    )
    CustomBarChart(chartData, Color(0xFFdc0073),"Kalorije(cal)")
}

    @Composable
    fun TimeChart(week: Week) {
        val chartData = listOf(
            "P" to week.GetMonday().getTime().toFloat() / 60f,
            "U" to week.GetTuesday().getTime().toFloat() / 60f,
            "S" to week.GetWednesday().getTime().toFloat() / 60f,
            "Č" to week.GetThursday().getTime().toFloat() / 60f,
            "P" to week.GetFriday().getTime().toFloat() / 60f,
            "S" to week.GetSaturday().getTime().toFloat() / 60f,
            "N" to week.GetSunday().getTime().toFloat() / 60f,
        )
        CustomBarChart(chartData, Color(0xFFf5b700),"Vrijeme(min)")
    }








