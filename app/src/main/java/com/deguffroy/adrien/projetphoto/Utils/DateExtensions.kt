package com.deguffroy.adrien.projetphoto.Utils

import java.text.DateFormat
import java.util.*

/**
 * Created by Adrien Deguffroy on 26/12/2018.
 */

// Format date to a string depending on user's language in a short format (dd/MM/yyyy)
fun Date.toLocaleStringDate():String {
    val dateFormat =  java.text.DateFormat.getDateInstance(DateFormat.SHORT)
    return dateFormat.format(this)
}

// Format date to a string depending on user's language in a medium format (dd/MM/yyyy hh:mm:ss)
fun Date.toLocaleStringDateMedium():String = java.text.DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM,Locale.getDefault()).format(this)