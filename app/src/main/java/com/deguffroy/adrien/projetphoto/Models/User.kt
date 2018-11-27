package com.deguffroy.adrien.projetphoto.Models

import android.content.res.Resources
import com.deguffroy.adrien.projetphoto.R

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
data class User(var uid:String,
                var username:String? = null,
                var userPicture:String? = null) {

    constructor(): this("")
}