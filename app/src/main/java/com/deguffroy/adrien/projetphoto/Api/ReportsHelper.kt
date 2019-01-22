package com.deguffroy.adrien.projetphoto.Api

import com.deguffroy.adrien.projetphoto.Models.Report
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by Adrien Deguffroy on 09/01/2019.
 */
class ReportsHelper {

    private val COLLECTION_NAME = "reports"

    // COLLECTION REFERENCE
    private fun getDatabaseReference() = FirebaseFirestore.getInstance()
    fun getReportsCollection() = getDatabaseReference().collection(COLLECTION_NAME)

    // --- CREATE ---

    fun createReport(commentId:String, userId: String)
            = ReportsHelper().getReportsCollection().add(Report(commentId, userId))

    // --- GET ---

    fun checkIfUserAlreadyReportThisComment(userId: String, commentId: String) = ReportsHelper()
        .getReportsCollection()
        .whereEqualTo("userId", userId)
        .whereEqualTo("commentId", commentId)

    fun getAllReportForAComment(commentId:String) = ReportsHelper()
        .getReportsCollection()
        .whereEqualTo("commentId", commentId)

    // --- DELETE ---

    fun deleteReportById(reportId:String) = ReportsHelper().getReportsCollection().document(reportId).delete()

}