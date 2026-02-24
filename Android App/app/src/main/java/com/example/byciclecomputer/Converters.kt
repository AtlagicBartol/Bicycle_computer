package com.example.byciclecomputer

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromDoubleList(value: List<Double>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toDoubleList(value: String): List<Double> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { it.toDouble() }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { it.toInt() }
    }
}


