package com.example.byciclecomputer

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.stream.DoubleStream.DoubleMapMultiConsumer

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey val id: Int = 1,
    val bodyWeight: Double,
    val tireSize: Double,
    val dailyGoal: Double,
    val name: String,
    val calories: List<Double>,
    val distances: List<Double>,
    val averageVelocities: List<Double>,
    val durations: List<Double>
    )

fun resetDataAtStartOfWeek(context: Context, week: Week, onReset: () -> Unit) {
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    val lastResetDateStr = sharedPreferences.getString("lastResetDate", "")

    val currentCalendar = Calendar.getInstance()
    val currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR)
    val currentYear = currentCalendar.get(Calendar.YEAR)

    var shouldReset = false

    if (!lastResetDateStr.isNullOrEmpty()) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastResetCalendar = Calendar.getInstance()
        lastResetCalendar.time = sdf.parse(lastResetDateStr) ?: return

        val lastResetWeek = lastResetCalendar.get(Calendar.WEEK_OF_YEAR)
        val lastResetYear = lastResetCalendar.get(Calendar.YEAR)

        // Reset ako je tjedan ili godina različita
        if (currentWeek != lastResetWeek || currentYear != lastResetYear) {
            shouldReset = true
        }
    } else {
        // Ako nikada prije nije resetirano
        shouldReset = true
    }

    if (shouldReset) {
        // Resetiraj sve dane u tjednu
        week.GetMonday().setAtributes(0.0, 0.0, 0.0, 0.0)
        week.GetTuesday().setAtributes(0.0, 0.0, 0.0, 0.0)
        week.GetWednesday().setAtributes(0.0, 0.0, 0.0, 0.0)
        week.GetThursday().setAtributes(0.0, 0.0, 0.0, 0.0)
        week.GetFriday().setAtributes(0.0, 0.0, 0.0, 0.0)
        week.GetSaturday().setAtributes(0.0, 0.0, 0.0, 0.0)
        week.GetSunday().setAtributes(0.0, 0.0, 0.0, 0.0)

        // Spremi novi datum reseta
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentCalendar.time)
        sharedPreferences.edit().putString("lastResetDate", todayDate).apply()

        onReset()
    }
}






