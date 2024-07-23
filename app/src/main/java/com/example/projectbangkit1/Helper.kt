package com.example.projectbangkit1

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class  Helper {
    companion object {
        fun String.withDateFormat(): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
            val date = format.parse(this) as Date
            return DateFormat.getDateInstance(DateFormat.FULL).format(date)
        }

        fun String.withTimeFormat() : String {
            val format = SimpleDateFormat("HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val time = format.parse(this) as Date
            return SimpleDateFormat.getTimeInstance(DateFormat.LONG).format(time)
        }
    }
}