package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.MyPicFragment
import com.deguffroy.adrien.projetphoto.R
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add.*
import java.util.*

class AddActivity : BaseActivity() {

    private lateinit var photoURL:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        this.photoURL = mViewModel.getCurrentPhotoURI()!!


        this.configureOnClickListener()

        this.updateUIWhenCreating()

    }

    // --------------------
    // CONFIGURATION
    // --------------------

    private fun configureOnClickListener() {
        add_activity_save_fab.setOnClickListener { this.savePicture() }
    }

    // --------------------
    // UI
    // --------------------

    private fun updateUIWhenCreating() {
        val glide = Glide.with(this)
        Log.e("AddActivity","PhotoURI : ${mViewModel.getCurrentPhotoURI()}")
        glide.load(mViewModel.getCurrentPhotoURI()).apply(RequestOptions().centerCrop()).into(add_activity_image)
    }

    // --------------------
    // ACTION
    // --------------------

    private fun savePicture() {
        val uuid: String = UUID.randomUUID().toString()
        val mImageRef = FirebaseStorage.getInstance().getReference(uuid)
        val uploadTask = mImageRef.putFile(mViewModel.getCurrentPhotoURI()!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }

            // Continue with the task to get the download URL
            mImageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.e("AddActivity","ViewModel User : ${mViewModel.currentModelUser}")
                PicturesHelper().createPicture(
                    mViewModel.getCurrentModelUser()!!,
                    downloadUri.toString(),
                    add_activity_public_checkbox.isChecked,
                    add_activity_desc.text.toString()
                ).addOnSuccessListener {
                    if (mViewModel.getCurrentUserPosition() != null) {
                        this.geoFirestore.setLocation(it.id, GeoPoint(mViewModel.getCurrentUserPosition()!!.latitude, mViewModel.getCurrentUserPosition()!!.longitude))
                    }
                    finish()
                }

            } else {
                // Handle failures
                // ...
            }
        }
    }

}
