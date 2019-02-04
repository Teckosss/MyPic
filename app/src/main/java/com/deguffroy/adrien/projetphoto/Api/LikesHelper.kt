package com.deguffroy.adrien.projetphoto.Api

import com.deguffroy.adrien.projetphoto.Models.Like
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by Adrien Deguffroy on 10/01/2019.
 */
class LikesHelper {

    private val COLLECTION_NAME = "likes"

    // COLLECTION REFERENCE
    private fun getDatabaseReference() = FirebaseFirestore.getInstance()
    fun getLikesCollection() = getDatabaseReference().collection(COLLECTION_NAME)

    // --- CREATE ---

    fun createLike(pictureId:String, userId:String)
            = LikesHelper().getLikesCollection().add(Like(pictureId, userId))

    // --- GET ---

    fun checkIfUserAlreadyLikedThisPicture(pictureId: String, userId: String) = LikesHelper()
        .getLikesCollection()
        .whereEqualTo("pictureId", pictureId)
        .whereEqualTo("userId", userId)

    // --- DELETE ---

    fun deleteLikeForUser(likeId:String) = LikesHelper()
        .getLikesCollection()
        .document(likeId)
        .delete()


}