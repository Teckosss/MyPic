package com.deguffroy.adrien.projetphoto.Controllers.Activities


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.deguffroy.adrien.projetphoto.R
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.lifecycle.ViewModelProviders
import com.deguffroy.adrien.projetphoto.Api.BottomNavHelper
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Api.UserHelper
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.*
import com.deguffroy.adrien.projetphoto.Models.User
import com.deguffroy.adrien.projetphoto.Utils.*
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import org.imperiumlabs.geofirestore.GeoFirestore


/**
 * Created by Adrien Deguffroy on 21/11/2018.
 */
open class BaseActivity : AppCompatActivity(){

    lateinit var mViewModel: CommunicationViewModel
    lateinit var modelCurrentUser:User
    lateinit var geoFirestore:GeoFirestore

    lateinit var photoURI: Uri
    lateinit var photoFilePath: String

    private lateinit var snackbar:Snackbar

    lateinit var mFragmentTag:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel::class.java)
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount != 1){
            super.onBackPressed()
            //bottom_navigation_view.menu.getItem(supportFragmentManager.backStackEntryCount).isChecked = true
            when(supportFragmentManager.findFragmentById(R.id.fragment_view)){
                is HomeFragment -> bottom_navigation_view.menu.getItem(0).isChecked = true
                is MapFragment -> bottom_navigation_view.menu.getItem(1).isChecked = true
                is MyPicFragment -> bottom_navigation_view.menu.getItem(2).isChecked = true
                is ProfileFragment -> bottom_navigation_view.menu.getItem(3).isChecked = true
                is ModerationFragment -> bottom_navigation_view.menu.getItem(4).isChecked = true
            }
        }else{
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.bnv_moderation)?.setVisible(true)
       /* UserHelper().getUser(getCurrentUser()?.uid!!).addOnCompleteListener {
            Log.e("BaseActivity","ENTER onCreateOptionsMenu : ${it.result}")
            if (it.isSuccessful){
                val moderation = menu?.findItem(R.id.bnv_moderation)
                moderation?.isVisible = (it.result?.get("admin") as Boolean)
                Log.e("BaseActivity","isAdmin : ${(it.result?.get("admin") as Boolean)}")
            }else{
                Log.e("BaseActivity","Error onCreateOptionsMenu")
            }
        }*/
        return super.onCreateOptionsMenu(menu)
    }

    fun configureBottomView(){
        bottom_navigation_view.setOnNavigationItemSelectedListener {
            val previousFragment = bottom_navigation_view.selectedItemId
            val nextFragment = it.itemId
            if (previousFragment != nextFragment) {
                when (it.itemId) {
                    R.id.bnv_home -> showFragment(HomeFragment.newInstance())
                    R.id.bnv_map -> showFragment(MapFragment.newInstance())
                    R.id.bnv_my_pic -> showFragment(MyPicFragment.newInstance())
                    R.id.bnv_profile -> showFragment(ProfileFragment.newInstance())
                    R.id.bnv_moderation -> showFragment(ModerationFragment.newInstance())
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    fun initDb(){
        val db = FirebaseFirestore.getInstance()
        val picturesCollection = db.collection("pictures")
        this.geoFirestore = GeoFirestore(picturesCollection)
    }

    // -------------------
    // UI
    // -------------------

    fun showFragment(newFragment:Fragment){
        when(newFragment){
            is HomeFragment -> this.mFragmentTag = FRAGMENT_HOME
            is MapFragment -> this.mFragmentTag = FRAGMENT_MAP
            is MyPicFragment -> this.mFragmentTag = FRAGMENT_MY_PIC
            is ProfileFragment -> this.mFragmentTag = FRAGMENT_PROFILE
            is ModerationFragment -> this.mFragmentTag = FRAGMENT_MODERATION
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_view, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showSnackbarMessage(coordinatorLayout: CoordinatorLayout,messageToShow:String, duration:Int = Snackbar.LENGTH_SHORT, dismissFAB:FloatingActionButton? = null){
        snackbar = Snackbar.make(coordinatorLayout,messageToShow, duration)
        snackbar.addCallback(object : Snackbar.Callback(){
            override fun onShown(sb: Snackbar?) {
                super.onShown(sb)
                Log.e("BaseActivity","onShown")
                dismissFAB?.hide()
            }
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                Log.e("BaseActivity","onDismissed")
                dismissFAB?.show()
            }
        })
        snackbar.show()
        snackbar.removeCallback(Snackbar.Callback())
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    protected fun isCurrentUserLogged(): Boolean? {
        return this.getCurrentUser() != null
    }

    fun getCurrentUserFromFirestore(changeBottomNav:Boolean = false) {
        UserHelper().getUser(getCurrentUser()?.uid!!)
            .addOnSuccessListener {
                this.modelCurrentUser = it.toObject<User>(User::class.java)!!
                if (changeBottomNav){
                    Log.e("BaseActivity","Need to change BottomNav! Does user need to see moderation : ${modelCurrentUser.admin || modelCurrentUser.moderator}")
                    bottom_navigation_view.menu.findItem(R.id.bnv_moderation).isVisible = modelCurrentUser.admin || modelCurrentUser.moderator
                    if (modelCurrentUser.admin || modelCurrentUser.moderator){
                        PicturesHelper().getAllPublicPictureNeedingVerification().get().addOnCompleteListener { taskResult ->
                            var needToShowBadge = false
                            Log.e("BaseActivity","Picture needing verification : ${taskResult.result?.count()}")
                            if (taskResult.isSuccessful){
                                if (taskResult.result?.count()!! > 0) needToShowBadge = true
                            }else{
                                Log.e("BaseActivity","Picture needing verification : ERROR")
                            }

                            CommentsHelper().getCommentsReported().get().addOnCompleteListener {commentTask->
                                Log.e("BaseActivity","Comment needing verification : ${commentTask.result?.count()}")
                                if(commentTask.isSuccessful){
                                    if (commentTask.result?.count()!! > 0) needToShowBadge = true
                                }

                                if (needToShowBadge) BottomNavHelper().showBadge(this,bottom_navigation_view,R.id.bnv_moderation)
                            }.addOnFailureListener {errorComment ->
                                Log.e("BaseActivity","Comment needing verification : ${errorComment.localizedMessage}")
                            }

                        }.addOnFailureListener {error ->
                            Log.e("BaseActivity","Picture needing verification : ${error.localizedMessage}")
                        }
                    }
                }
            }.addOnFailureListener {
                Log.e("BaseActivity","getCurrentUserFromFirestore FAILURE LISTENER : ${it.localizedMessage}")
            }
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected fun onFailureListener(): OnFailureListener {
        return OnFailureListener {
            this.showSnackbarMessage(main_activity_layout, getString(R.string.error_unknown_error))
        }
    }
}