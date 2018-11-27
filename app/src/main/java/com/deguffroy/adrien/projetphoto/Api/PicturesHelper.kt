package com.deguffroy.adrien.projetphoto.Api

import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
open class PicturesHelper {

    private val COLLECTION_NAME = "pictures"

    // COLLECTION REFERENCE
    private fun getPicturesCollection() = FirebaseFirestore.getInstance().collection(COLLECTION_NAME)

    // --- CREATE ---

    fun createPicture(userSender:User, urlImage:String, isPublic:Boolean, description:String?): Task<DocumentReference>{
        val picture = Picture(userSender,isPublic,false, urlImage,null, description)
        return PicturesHelper().getPicturesCollection().add(picture)
    }

    // --- GET ---

    fun getAllPicturesFromUser(uid:String): Query = PicturesHelper()
        .getPicturesCollection()
        .whereEqualTo("userSender.uid", uid)
        .orderBy("dateCreated",Query.Direction.DESCENDING)



    // --- UPDATE ---

    // --- DELETE ---
}