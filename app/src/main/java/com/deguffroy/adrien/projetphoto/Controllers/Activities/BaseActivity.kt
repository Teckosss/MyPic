package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.os.Bundle
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.MapFragment
import com.deguffroy.adrien.projetphoto.R
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.lifecycle.ViewModelProviders
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.HomeFragment
import com.deguffroy.adrien.projetphoto.Utils.Constants
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.snackbar.Snackbar
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions


/**
 * Created by Adrien Deguffroy on 21/11/2018.
 */
open class BaseActivity : AppCompatActivity(){

    lateinit var mViewModel: CommunicationViewModel

    private lateinit var snackbar:Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel::class.java)
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    fun configureBottomView(){
        bottom_navigation_view.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.bnv_home -> showFragment(HomeFragment.newInstance())
                R.id.bnv_map -> showFragment(MapFragment.newInstance())
                //R.id.bnv_my_pic -> showFragment()
                //R.id.bnv_profile -> showFragment()
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    // -------------------
    // UI
    // -------------------

    fun showFragment(newFragment:Fragment){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_view, newFragment)
            transaction.commit()
    }

    fun showSnackbarMessage(messageToShow:String){
        snackbar = Snackbar.make(main_activity_layout,messageToShow,Snackbar.LENGTH_SHORT)
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

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected fun onFailureListener(): OnFailureListener {
        return OnFailureListener {
            this.showSnackbarMessage(getString(R.string.error_unknown_error))
        }
    }
}