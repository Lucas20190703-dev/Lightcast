package com.blueshark.lightcast.utils

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//fun getDateTime(s: Long?): String? {
//    if (s == null) return ""
//    return try {
//        val sdf = SimpleDateFormat("MMM dd, yyyy")
//        val netDate = Date(s)
//        sdf.format(netDate)
//    } catch (e: Exception) {
//        e.toString()
//    }
//}
//
//fun getTimeFormatFromSecond(s: Int): String? {
//    val hours = s / 3600
//    val minutes = (s- hours * 3600) / 60
//    val seconds = s - hours * 3600 - minutes * 60
//    var formattedTime: String? = ""
//    if (hours < 10) formattedTime += "0"
//    formattedTime += "$hours:"
//
//    if (minutes < 10) formattedTime += "0"
//    formattedTime += "$minutes:"
//
//    if (seconds < 10) formattedTime += "0"
//    formattedTime += seconds
//    return formattedTime
//}
//
//fun getTimeFormatFromSecondString(str: String): String? {
//    val timeInDouble = str.toDoubleOrNull()
//    if (timeInDouble == null) {
//        return str
//    } else {
//        val s = timeInDouble.toInt()
//        val hours = s / 3600
//        val minutes = (s- hours * 3600) / 60
//        val seconds = s - hours * 3600 - minutes * 60
//        var formattedTime: String? = ""
//        if (hours < 10) formattedTime += "0"
//        formattedTime += "$hours:"
//
//        if (minutes < 10) formattedTime += "0"
//        formattedTime += "$minutes:"
//
//        if (seconds < 10) formattedTime += "0"
//        formattedTime += seconds
//        return formattedTime
//    }
//}
//
//fun getTimeFormatFromMillisecondString(str : String) : String? {
//    val timeInDouble = str.toDoubleOrNull()
//    if (timeInDouble == null) {
//        return str
//    } else {
//        val s = timeInDouble.toInt()
//        val hours = s / 3600
//        val minutes = (s- hours * 3600) / 60
//        val seconds = s - hours * 3600 - minutes * 60
//        var formattedTime: String? = ""
//        if (hours < 10) formattedTime += "0"
//        formattedTime += "$hours:"
//
//        if (minutes < 10) formattedTime += "0"
//        formattedTime += "$minutes:"
//
//        if (seconds < 10) formattedTime += "0"
//        formattedTime += seconds
//        return formattedTime
//    }
//}
//
//fun getTimeFormatFromMillisecond(ms : Long) : String? {
//    val s = (ms + 500) / 1000
//    val hours = s / 3600
//    val minutes = (s- hours * 3600) / 60
//    val seconds = s - hours * 3600 - minutes * 60
//    var formattedTime: String? = ""
//    if (hours < 10) formattedTime += "0"
//    formattedTime += "$hours:"
//
//    if (minutes < 10) formattedTime += "0"
//    formattedTime += "$minutes:"
//
//    if (seconds < 10) formattedTime += "0"
//    formattedTime += seconds
//    return formattedTime
//}