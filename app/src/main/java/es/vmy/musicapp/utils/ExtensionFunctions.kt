package es.vmy.musicapp.utils

import java.text.SimpleDateFormat
import java.util.Date

fun getFormattedDateString(): String {
    val date = Date()
    val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
    return dateFormat.format(date)
}

fun getDateFromFormattedString(formattedString: String): Date {
    val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
    return dateFormat.parse(formattedString)
}