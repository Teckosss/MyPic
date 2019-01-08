package com.deguffroy.adrien.projetphoto.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Created by Adrien Deguffroy on 08/01/2019.
 */
data class Comment(var commentText:String,
              var pictureId:String,
              var userSender:User,
              @ServerTimestamp var dateCreated:Timestamp? = null,
              var documentId:String? = null,
              var reportCount:Int = 0){

    constructor() : this("","",User())
}