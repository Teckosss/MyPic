package com.deguffroy.adrien.projetphoto.Api

import com.deguffroy.adrien.projetphoto.Models.PictureView
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by Adrien Deguffroy on 04/01/2019.
 */
open class ViewsHelper {

    private val COLLECTION_NAME = "views"

    // COLLECTION REFERENCE
    private fun getDatabaseReference() = FirebaseFirestore.getInstance()
    fun getViewsCollection() = getDatabaseReference().collection(COLLECTION_NAME)

    // --- CREATE ---

    fun createNewView(documentId:String,userId:String) = ViewsHelper().getViewsCollection().add(PictureView(documentId, userId))

    // --- GET ---

    fun checkIfUserAlreadyViewThisPicture(documentId:String,userId:String) = ViewsHelper()
        .getViewsCollection()
        .whereEqualTo("documentId",documentId)
        .whereEqualTo("userId",userId)
        .get()
}