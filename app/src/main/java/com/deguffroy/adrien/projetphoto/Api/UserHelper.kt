package com.deguffroy.adrien.projetphoto.Api

import com.deguffroy.adrien.projetphoto.Models.User
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
open class UserHelper {

    private val COLLECTION_NAME = "users"

    private fun getUsersCollection() = FirebaseFirestore.getInstance().collection(COLLECTION_NAME)

    // --- CREATE ---

    fun createUser(uid:String,username:String?,userPicture:String?) =
        UserHelper().getUsersCollection().document(uid).set(User(uid,username,userPicture))

    // --- GET ---

    fun getUser(uid:String) = UserHelper().getUsersCollection().document(uid).get()

    // --- UPDATE ---

    fun updateUsername(uid: String, username: String) = UserHelper().getUsersCollection().document(uid).update("username", username)

}