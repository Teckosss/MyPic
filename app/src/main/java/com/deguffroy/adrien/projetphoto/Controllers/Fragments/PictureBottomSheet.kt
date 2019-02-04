package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.picture_bottom_sheet.*

/**
 * Created by Adrien Deguffroy on 14/01/2019.
 */
@SuppressLint("ValidFragment")
class PictureBottomSheet @SuppressLint("ValidFragment") constructor(private var callback: BottomSheetInterface) : BottomSheetDialogFragment() {

    companion object {
        private const val DOCUMENT_ID = "DOCUMENT_ID"
        fun newInstance(documentId:String, callback: BottomSheetInterface):PictureBottomSheet{
            val bottomSheetFragment = PictureBottomSheet(callback)
            val bundle = Bundle()
            bundle.putString(DOCUMENT_ID,documentId)
            bottomSheetFragment.arguments = bundle
            return bottomSheetFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.picture_bottom_sheet,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.setOnClickListener()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun setOnClickListener(){
        picture_bottom_sheet_image_nudity.setOnClickListener{ this.updatePictureAndDismiss(MODERATION_IMAGE_NUDITY) }
        picture_bottom_sheet_image_violence.setOnClickListener{ this.updatePictureAndDismiss(MODERATION_IMAGE_VIOLENCE) }
        picture_bottom_sheet_text_racism.setOnClickListener{ this.updatePictureAndDismiss(MODERATION_TEXT_RACISM) }
        picture_bottom_sheet_text_vulgar.setOnClickListener{ this.updatePictureAndDismiss(MODERATION_TEXT_VULGAR) }
    }

    // -------------------
    // ACTION
    // -------------------

    // Update picture with deny reason
    private fun updatePictureAndDismiss(reason:String){
        val pictureId = arguments?.getString(DOCUMENT_ID)
        if (pictureId != null){
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            batch.update(PicturesHelper().getPicturesCollection().document(pictureId), "public" , false) // Set picture public field to false
            batch.update(PicturesHelper().getPicturesCollection().document(pictureId), "denyReason" , reason) // Adding deny reason
            batch.commit().addOnSuccessListener {
                Log.i("PictureSheet","Batch success!")
                dismiss()
                callback.returnToPicturePageFragment()
            }.addOnFailureListener {failureTask->
                Log.e("PictureSheet","Batch failure! ${failureTask.localizedMessage}")
            }
        }
    }
}