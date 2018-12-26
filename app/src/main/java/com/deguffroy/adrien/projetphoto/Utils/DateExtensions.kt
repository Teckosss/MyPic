package com.deguffroy.adrien.projetphoto.Utils

import java.text.DateFormat
import java.util.*

/**
 * Created by Adrien Deguffroy on 26/12/2018.
 */

fun Date.toLocaleStringDate():String {
    val dateFormat =  java.text.DateFormat.getDateInstance(DateFormat.SHORT)
    return dateFormat.format(this)
}