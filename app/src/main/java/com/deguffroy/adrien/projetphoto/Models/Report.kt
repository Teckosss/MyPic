package com.deguffroy.adrien.projetphoto.Models

/**
 * Created by Adrien Deguffroy on 09/01/2019.
 */
data class Report(var commentId:String, var userId:String) {

    constructor() : this("", "")
}