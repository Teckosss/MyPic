package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Api.UserHelper
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.HomeFragment
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.MapFragment
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.MyPicFragment
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.ProfileFragment
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.User
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.*
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
import kotlin.collections.ArrayList

class MainActivity : BaseActivity() {

    private val geoPointCenter = GeoPoint(50.3663336,3.5577161999999998)
    private val geoRadius: Double = 20.0 // Kilometers Radius

    private lateinit var picturesList:ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.picturesList = ArrayList()

        this.initDb()
        this.configureBottomView()
        if (savedInstanceState != null){
            val tag = savedInstanceState.getString(FRAGMENT_TAG_KEY) ?: null
            if (tag != null){
                when(tag){
                    FRAGMENT_HOME -> showFragment(HomeFragment.newInstance())
                    FRAGMENT_MAP -> showFragment(MapFragment.newInstance())
                    FRAGMENT_MY_PIC -> showFragment(MyPicFragment.newInstance())
                    FRAGMENT_PROFILE -> showFragment(ProfileFragment.newInstance())
                }
            }
        }else{
            this.showFragment(HomeFragment.newInstance())
        }

        this.getCurrentUserFromFirestore()
        //this.retrieveData()
        //this.populateDB()
        this.setOnClickListener()
        mViewModel.updateCurrentUserUID(this.getCurrentUser()?.uid!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.handleResponse(requestCode,resultCode,data)

    }

    private fun setOnClickListener(){
        main_activity_fab.setOnClickListener { this.onClickPictureFAB() }
    }

    private fun onClickPictureFAB()= runWithPermissions(PERM_CAMERA){this.takePhotoFromCamera()}

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
                    this.photoURI = FileProvider.getUriForFile(this,"com.deguffroy.adrien.projetphoto.fileprovider",it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.photoURI )
                    startActivityForResult(takePictureIntent, RC_TAKE_PHOTO)
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
        if(requestCode == RC_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                val intent = Intent(this,AddActivity::class.java)
                intent.putExtra(URI_EXTRA_NAME, this.photoURI.toString())
                startActivity(intent)
            }

        }else {
            Log.e("TAG","No pic choose")
        }
    }

    // --------------------
    // REST REQUESTS
    // --------------------

   /* private fun populateDB(){
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
    }*/

    fun showFab() = main_activity_fab.show()

    fun hideFab() = main_activity_fab.hide()
}
