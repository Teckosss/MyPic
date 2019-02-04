package com.deguffroy.adrien.projetphoto.Api

import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
open class PicturesHelper {

    private val COLLECTION_NAME = "pictures"

    // COLLECTION REFERENCE
    private fun getDatabaseReference() = FirebaseFirestore.getInstance()
    fun getPicturesCollection() = getDatabaseReference().collection(COLLECTION_NAME)

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

    fun getAllPicturesWithGeoLocAndPublicVerified() = PicturesHelper().getPicturesCollection()
        .whereGreaterThan("g","")
        .whereEqualTo("public",true)
        .whereEqualTo("verificationDone",true)
        .get()

    fun getAllPublicAndVerifiedPictures() = PicturesHelper()
        .getPicturesCollection()
        .whereEqualTo("public", true)
        .whereEqualTo("verificationDone", true)
        .orderBy("dateCreated",Query.Direction.DESCENDING)

    fun getAllPublicPictureNeedingVerification() = PicturesHelper()
        .getPicturesCollection()
        .whereEqualTo("public", true)
        .whereEqualTo("verificationDone", false)
        .orderBy("dateCreated",Query.Direction.ASCENDING)

    fun getPictureById(uid:String) = PicturesHelper().getPicturesCollection().document(uid).get()

    // --- UPDATE ---

    fun updatePictureDocumentID(uid:String)=
        PicturesHelper().getPicturesCollection().document(uid).update("documentId",uid)

    fun updatePictureDocumentUsername(pictureId: String, username:String) =
        PicturesHelper().getPicturesCollection().document(pictureId).update("userSender.username", username)

    fun toggleVisibilityScope(uid: String, setToPublic:Boolean) =
        PicturesHelper().getPicturesCollection().document(uid).update("public", setToPublic)

}