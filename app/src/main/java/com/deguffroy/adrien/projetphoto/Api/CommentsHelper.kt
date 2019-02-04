package com.deguffroy.adrien.projetphoto.Api

import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.Models.User
import com.deguffroy.adrien.projetphoto.Utils.COMMENTS_MIN_NUMBER_TO_NEED_VERIFICATION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Created by Adrien Deguffroy on 08/01/2019.
 */
open class CommentsHelper {

    private val COLLECTION_NAME = "comments"

    // COLLECTION REFERENCE
    private fun getDatabaseReference() = FirebaseFirestore.getInstance()
    fun getCommentsCollection() = getDatabaseReference().collection(COLLECTION_NAME)

    // --- CREATE ---

    fun createComment(commentText:String, pictureId:String, userSender:User)
            = CommentsHelper().getCommentsCollection().add(Comment(commentText, pictureId, userSender))

    // --- GET ---

    fun getCommentsForPicture(pictureId: String) = CommentsHelper()
        .getCommentsCollection()
        .whereEqualTo("pictureId", pictureId)
        .orderBy("dateCreated", Query.Direction.DESCENDING)

    fun getAllCommentsForUser(userId:String) =
            CommentsHelper().getCommentsCollection().whereEqualTo("userSender.uid", userId)

    fun getCommentById(commentId:String) = CommentsHelper().getCommentsCollection().document(commentId).get()

    fun getCommentsReported() = CommentsHelper()
        .getCommentsCollection()
        .whereGreaterThan("reportCount", COMMENTS_MIN_NUMBER_TO_NEED_VERIFICATION)
        .orderBy("reportCount", Query.Direction.DESCENDING)

    // --- UPDATE ---

    fun updateCommentDocumentId(documentId:String)=
        CommentsHelper().getCommentsCollection().document(documentId).update("documentId",documentId)

    fun updateCommentDocumentUsername(commentId: String, username:String) =
            CommentsHelper().getCommentsCollection().document(commentId).update("userSender.username", username)

    fun updateCommentTextById(documentId: String, replacementText:String) =
        CommentsHelper().getCommentsCollection().document(documentId).update("commentText", replacementText )
}