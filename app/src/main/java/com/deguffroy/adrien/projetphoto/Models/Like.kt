package com.deguffroy.adrien.projetphoto.Models

/**
 * Created by Adrien Deguffroy on 10/01/2019.
 */
data class Like(var pictureId:String,
           var userId:String) {

    constructor() : this("" ,"")
}