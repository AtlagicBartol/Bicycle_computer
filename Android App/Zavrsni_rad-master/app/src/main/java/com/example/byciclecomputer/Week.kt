package com.example.byciclecomputer

import java.io.Serializable

class Week(
    private val monday: Day,
    private val tuesday: Day,
    private val wednesday: Day,
    private val thursday: Day,
    private val friday: Day,
    private val saturday: Day,
    private val sunday: Day
) : Serializable {
    fun GetMonday() : Day{
        return monday
    }
    fun GetTuesday() : Day{
        return tuesday
    }
    fun GetWednesday() : Day{
        return wednesday
    }
    fun GetThursday() : Day{
        return thursday
    }
    fun GetFriday() : Day{
        return friday
    }
    fun GetSaturday() : Day{
        return saturday
    }
    fun GetSunday() : Day{
        return sunday
    }

        fun copy(
            monday: Day = this.monday,
            tuesday: Day = this.tuesday,
            wednesday: Day = this.wednesday,
            thursday: Day = this.thursday,
            friday: Day = this.friday,
            saturday: Day = this.saturday,
            sunday: Day = this.sunday
        ): Week {
            return Week(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
        }
}