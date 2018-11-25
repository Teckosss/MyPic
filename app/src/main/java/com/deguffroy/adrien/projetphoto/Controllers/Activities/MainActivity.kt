package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Api.UserHelper
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.HomeFragment
import com.deguffroy.adrien.projetphoto.Models.User
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.Constants
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener
import java.io.File
import java.io.IOException
import java.lang.ClassCastException
import java.lang.Exception
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity(), GeoQueryDataEventListener {

    private val geoPointCenter = GeoPoint(50.3663336,3.5577161999999998)
    private val geoRadius: Double = 100.0 // Kilometers Radius

    private lateinit var photoURI: Uri
    private lateinit var photoFilePath: String

    private val picturesList = arrayListOf<String>()

    private lateinit var geoFirestore:GeoFirestore
    private lateinit var geoQuery: GeoQuery

    private lateinit var modelCurrentUser:User

    private var mUserCurrentLocation:LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mViewModel.currentUserPosition.observe(this, androidx.lifecycle.Observer {
            Log.e("MainActivity", " Location changed : $it")
            mUserCurrentLocation = it
        })

        this.configureBottomView()
        this.showFragment(HomeFragment.newInstance())
        this.retrieveData()
        this.getCurrentUserFromFirestore()
        //this.populateDB()
        this.setOnClickListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.handleResponse(requestCode,resultCode,data)

    }

    private fun setOnClickListener(){
        main_activity_fab.setOnClickListener { this.onClickPictureFAB() }
    }

    private fun onClickPictureFAB()= runWithPermissions(Constants.PERM_CAMERA){this.takePhotoFromCamera()}

    // ---------------------
    // FILE MANAGEMENT
    // ---------------------

    private fun takePhotoFromCamera(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                }catch (ex: IOException){
                    null
                }
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(this,"com.deguffroy.adrien.projetphoto.fileprovider",it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI )
                    startActivityForResult(takePictureIntent, Constants.RC_TAKE_PHOTO)
                }

            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            photoFilePath = absolutePath
        }
    }

    //  Handle activity response (after user has chosen or not a picture)
    private fun handleResponse(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == Constants.RC_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                val uuid:String = UUID.randomUUID().toString()
                val mImageRef =  FirebaseStorage.getInstance().getReference(uuid)
                val uploadTask = mImageRef.putFile(photoURI)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }

                    // Continue with the task to get the download URL
                    mImageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        PicturesHelper().createPicture(modelCurrentUser,downloadUri.toString()).addOnSuccessListener{
                            if ( mUserCurrentLocation != null){
                                this.geoFirestore.setLocation(it.id,GeoPoint(mUserCurrentLocation!!.latitude, mUserCurrentLocation!!.longitude))
                            }
                        }

                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }

        }else {
            Log.e("TAG","No pic choose")
        }
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    private fun getCurrentUserFromFirestore() {
        UserHelper().getUser(getCurrentUser()?.uid!!)
            .addOnSuccessListener {
                modelCurrentUser = it.toObject<User>(User::class.java)!!
            }
    }

    private fun populateDB(){
        val db = FirebaseFirestore.getInstance()
        val picturesCollection = db.collection("pictures")

        this.geoFirestore = GeoFirestore(picturesCollection)
        geoFirestore.setLocation("aQ81kFMkJxVeuYObtsPG", GeoPoint(50.3659507, 3.5568468000000166))
        geoFirestore.setLocation("5PEE40TEgCWnAHGaWXb6", GeoPoint(40.7127753, -74.0059728))
        geoFirestore.setLocation("Dum9Z60Ce1MWdOfqV6fv", GeoPoint(45.764043, 4.835658999999964))
        geoFirestore.setLocation("DzCziWiEtWKLc9JkgY6W", GeoPoint(43.296482, 5.369779999999992))
        geoFirestore.setLocation("Fv7zY3Ra09t8uI05nhzI", GeoPoint(50.3102428, 3.5797181000000364))
        geoFirestore.setLocation("mRr6oF85L29qte8zLiQv", GeoPoint(48.856614, 2.3522219000000177))
        geoFirestore.setLocation("rK45U2j9vo1R5BX0b3Qe", GeoPoint(49.9849752, 3.4451979000000392))
        geoFirestore.setLocation("vBHlS0TcSkRArVZuj0YA", GeoPoint(50.3519847, 3.519403000000011))
    }

    private fun retrieveData(){
        Log.e("retrieveData","Enter")
        val db = FirebaseFirestore.getInstance()
        val picturesCollection = db.collection("pictures")

        this.geoFirestore = GeoFirestore(picturesCollection)



        this.geoQuery = geoFirestore.queryAtLocation(geoPointCenter,geoRadius)
        this.geoQuery.removeAllListeners()
        this.geoQuery.addGeoQueryDataEventListener(this)
    }

    override fun onGeoQueryReady() {
        Log.e("onGeoQueryReady","Enter, list size : ${picturesList.size}")
        this.geoQuery.removeGeoQueryEventListener(this)

        (0 until picturesList.size).forEach{
            Log.e("onGeoQueryReady","Data : ${picturesList[it]}")
        }
    }

    override fun onDocumentExited(p0: DocumentSnapshot?) {
        Log.e("onDocumentExited","Enter")
    }

    override fun onDocumentChanged(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentChanged","Enter")
    }

    override fun onDocumentEntered(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentEntered","Enter, data : ${p0?.data}")
     try {
         val data:Map<String,Any>? = p0?.data
         val description:String = data?.get("desc") as String
         if (description != null) picturesList.add(description)
     }catch (e:NullPointerException){
         Log.e("DocumentEntered", " Error : ${e.localizedMessage}")
     }catch (e:ClassCastException){
         Log.e("DocumentEntered", " Error : ${e.localizedMessage}")
     }
    }

    override fun onDocumentMoved(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentMoved","Enter")
    }

    override fun onGeoQueryError(p0: Exception?) {
     Log.e("GEO_QUERY","Error : ${p0?.localizedMessage}")
    }
}
