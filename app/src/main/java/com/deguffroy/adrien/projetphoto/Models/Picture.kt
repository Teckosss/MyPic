package com.deguffroy.adrien.projetphoto.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
data class Picture(var userSender:User,
                   var isPublic:Boolean,
                   var isVerificationDone:Boolean,
                   var urlImage:String,
                   @ServerTimestamp var dateCreated: Timestamp? = null,
                   var description:String? = null,
                   var views:Int? = 0,
                   var likes:Int? = 0,
                   var comments:Int? = 0) {

}