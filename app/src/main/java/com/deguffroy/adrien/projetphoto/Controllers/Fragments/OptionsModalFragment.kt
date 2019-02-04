package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.ReportsHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.MainActivity
import com.deguffroy.adrien.projetphoto.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.modal_fragment_options.*

/**
 * Created by Adrien Deguffroy on 09/01/2019.
 */
class OptionsModalFragment : BottomSheetDialogFragment() {

    companion object {
        private const val DOCUMENT_ID = "DOCUMENT_ID"
        private const val USER_ID = "USER_ID"
        fun newInstance(documentId:String, userId:String):OptionsModalFragment{
            val bottomSheetFragment = OptionsModalFragment()
            val bundle = Bundle()
            bundle.putString(DOCUMENT_ID,documentId)
            bundle.putString(USER_ID, userId)
            bottomSheetFragment.arguments = bundle
            return bottomSheetFragment
        }
    }

    interface Listener{
        fun displayMessage(message:String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.modal_fragment_options,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.setOnClickListener()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun setOnClickListener(){
        modal_fragment_report_text.setOnClickListener{ this.sendReport() }
    }

    // -------------------
    // ACTION
    // -------------------

    private fun sendReport(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val documentId = arguments?.getString(DOCUMENT_ID)
        Log.e("ModalOptions","Comment id : $documentId")
        if (userId != null && documentId != null){
            ReportsHelper().checkIfUserAlreadyReportThisComment(userId, documentId).get().addOnCompleteListener {
                if (it.result?.isEmpty!!) {
                    ReportsHelper().createReport(documentId, userId).addOnCompleteListener { createTask ->
                        if (createTask.isSuccessful){
                            val db = FirebaseFirestore.getInstance()
                            val docRef = CommentsHelper().getCommentsCollection().document(documentId)
                            db.runTransaction { transaction ->
                                val currentReportCount = transaction.get(docRef)
                                val newReportCount = (currentReportCount.get("reportCount") as Long) + 1
                                Log.e("ModalOptions","New value : $newReportCount")
                                transaction.update(docRef,"reportCount", newReportCount)
                            }.addOnSuccessListener {
                                this.dismissAndShowMessage(resources.getString(R.string.modal_fragment_user_report_success))
                            }.addOnFailureListener { failure -> Log.e("ModalOptions","Transaction failed : ${failure.localizedMessage}") }
                        }
                    }
                }else{ // USER ALREADY REPORT THIS COMMENT
                    this.dismissAndShowMessage(resources.getString(R.string.modal_fragment_user_already_report))
                }
            }
        }else{
            // SHOW ERROR
            Log.e("ModalOptions","Error ! userId = $userId | documentId = $documentId")
            this.dismissAndShowMessage(resources.getString(R.string.detail_activity_error_reporting_comment))
        }
    }

    private fun dismissAndShowMessage(message: String){
        dismiss()
        (activity as Listener).displayMessage(message)
    }
}