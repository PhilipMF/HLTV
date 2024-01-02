package com.example.hltv.data

import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeStampConverter {
}
fun convertTimestampToDateDisplay(timestamp: Int?): String {
    val dateFormat = DateFormat.getDateInstance()
    if(timestamp != null) {
        val date = Date(timestamp.toLong()*1000L)// Assuming the timestamp is in seconds, multiply by 1000 for milliseconds
        return dateFormat.format(date)
    } else {
        return "Unknown date"
    }
}

fun convertTimestampToDateURL(timestamp: Int?): String {
    Log.i("convertTimestampToDateURL","timestamp: $timestamp")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    if (timestamp != null) {
        val date =
            Date(timestamp.toLong() * 1000) // Assuming the timestamp is in seconds, multiply by 1000 for milliseconds
        Log.i("convertTimestampToDateURL","date: $date")
        return dateFormat.format(date)
    } else {
        return "Unknown date"
    }
}