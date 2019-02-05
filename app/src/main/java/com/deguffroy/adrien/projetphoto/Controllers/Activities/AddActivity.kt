package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.URI_EXTRA_NAME
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class AddActivity : BaseActivity() {

    private lateinit var retrievedURI: Uri
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        this.retrievedURI = Uri.parse(intent.getStringExtra(URI_EXTRA_NAME))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        this.initDb()
        this.getCurrentUserFromFirestore()
        this.configureOnClickListener()
        this.updateUIWhenCreating()

    }

    // --------------------
    // CONFIGURATION
    // --------------------

    private fun configureOnClickListener() {
        add_activity_save_fab.setOnClickListener { this.savePicture() }
        add_activity_cancel_button.setOnClickListener { finish() }
    }

    // --------------------
    // UI
    // --------------------

    private fun updateUIWhenCreating() {
        val glide = Glide.with(this)
        glide.load(this.retrievedURI).apply(RequestOptions().centerCrop()).into(add_activity_image)
    }

    // --------------------
    // ACTION
    // --------------------

    // Compress file, upload it to Storage, create picture in Firestore and set its location
    @SuppressLint("MissingPermission")
    private fun savePicture(needToCompress:Boolean = true) {
        add_activity_upload_layout.visibility = View.VISIBLE

        val uuid: String = UUID.randomUUID().toString() // Generate a random name for picture file to save in Firebase Storage
        val mImageRef = FirebaseStorage.getInstance().reference.child("images/${this.modelCurrentUser.uid}/$uuid.jpg")
        lateinit var uploadTask:UploadTask

        if (needToCompress){ // Compress picture to save space in Firebase Storage
            var bmp:Bitmap? = null
            try {
                bmp = MediaStore.Images.Media.getBitmap(contentResolver,this.retrievedURI)
            }catch (e:IOException){
                Log.e("AddActivity","Unable to create bitmap from URI : ${e.localizedMessage}")
            }
            val baos = ByteArrayOutputStream()
            bmp?.compress(Bitmap.CompressFormat.JPEG, 25, baos)
            val fileBytes = baos.toByteArray()
            uploadTask = mImageRef.putBytes(fileBytes)
        }else{
            uploadTask = mImageRef.putFile(this.retrievedURI)
        }

        // With ProgressListener we can show the current upload percent to user
        uploadTask.addOnProgressListener {
            val progress = 100.0 * it.bytesTransferred / it.totalByteCount
            Log.i("AddActivity","Upload progress : $progress")
            add_activity_progressBar.progress = progress.toInt()
            add_activity_upload_text.text = resources.getString(R.string.add_activity_upload_progress,progress.toInt())
            this.disableUI()
        }.addOnCanceledListener {
            Log.i("AddActivity","Canceled!")
        }.addOnPausedListener {
            Log.i("AddActivity","Paused : $it")
        }.addOnFailureListener {
            Log.e("AddActivity","Failure : ${it.localizedMessage}")
        }

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }

            // Continue with the task to get the download URL
            mImageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                PicturesHelper().createPicture(
                    this.modelCurrentUser,
                    downloadUri.toString(),
                    add_activity_public_checkbox.isChecked,
                    add_activity_desc.text.toString()
                ).addOnSuccessListener {
                    if (this.locationPermissionsGranted()){
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location : Location? ->
                                // Got last known location. In some rare situations this can be null.
                                if (location != null){
                                    this.geoFirestore.setLocation(it.id, GeoPoint(location.latitude,location.longitude))
                                }
                            }
                    }

                    PicturesHelper().updatePictureDocumentID(it.id)
                    finish()
                }

            }
        }.addOnFailureListener {
            Log.e("AddActivity","Failure ! ${it.localizedMessage}")
        }
    }

    // When user click save button, disable all buttons and editText
    private fun disableUI(){
        add_activity_save_fab.isEnabled = false
        add_activity_cancel_button.isEnabled = false
        add_activity_desc.isEnabled = false
        add_activity_public_checkbox.isEnabled = false
        add_activity_save_fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.button_disable))
    }

    // -------------------
    // PERMISSIONS
    // -------------------

    // Check if needed location permissions are granted
    private fun locationPermissionsGranted() : Boolean = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)
}
