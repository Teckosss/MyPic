package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.*
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.*
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity() {

    private lateinit var picturesList:ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.picturesList = ArrayList()

        this.initDb()
        this.getCurrentUserFromFirestore(true)
        this.configureBottomView()
        if (savedInstanceState != null){
            val tag = savedInstanceState.getString(FRAGMENT_TAG_KEY) ?: null
            if (tag != null){
                when(tag){
                    FRAGMENT_HOME -> showFragment(HomeFragment.newInstance())
                    FRAGMENT_MAP -> showFragment(MapFragment.newInstance())
                    FRAGMENT_MY_PIC -> showFragment(MyPicFragment.newInstance())
                    FRAGMENT_PROFILE -> showFragment(ProfileFragment.newInstance())
                    FRAGMENT_MODERATION -> showFragment(ModerationFragment.newInstance())
                }
            }
        }else{
            this.showFragment(HomeFragment.newInstance())
        }
        this.setOnClickListener()
        mViewModel.updateCurrentUserUID(this.getCurrentUser()?.uid!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.handleResponse(requestCode, resultCode)

    }

    private fun setOnClickListener(){
        main_activity_fab.setOnClickListener { this.onClickPictureFAB() }
    }

    private fun onClickPictureFAB()= runWithPermissions(PERM_CAMERA){this.takePhotoFromCamera()}

    // ---------------------
    // FILE MANAGEMENT
    // ---------------------

    // Create a imageFile when user take a photo
    private fun takePhotoFromCamera(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                }catch (ex: IOException){
                    Log.e("MainActivity","Error creatingFile! ${ex.localizedMessage}")
                    null
                }
                if (photoFile != null ){
                    Log.i("MainActivity","PhotoFile not null!")
                    mViewModel.updateCurrentPhotoFile(photoFile)
                    mViewModel.getCurrentPhotoFile().also {
                        this.photoURI = FileProvider.getUriForFile(this,"com.deguffroy.adrien.projetphoto.fileprovider", it!!)
                        mViewModel.updateCurrentPhotoURI(this.photoURI)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mViewModel.getCurrentPhotoURI() )
                        startActivityForResult(takePictureIntent, RC_TAKE_PHOTO)
                    }
                }
            }
        }
    }

    // Create temp image
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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
    private fun handleResponse(requestCode: Int, resultCode: Int) {
        if(requestCode == RC_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) { // Everything is Ok, starting AddActivity
                val intent = Intent(this,AddActivity::class.java)
                intent.putExtra(URI_EXTRA_NAME, mViewModel.getCurrentPhotoURI().toString())
                startActivity(intent)
            }

        }else {
            Log.e("TAG","No pic choose")
        }
    }

    fun showFab() = main_activity_fab.show()

    fun hideFab() = main_activity_fab.hide()
}
