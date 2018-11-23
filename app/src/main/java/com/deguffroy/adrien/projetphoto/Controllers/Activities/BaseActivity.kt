package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser



/**
 * Created by Adrien Deguffroy on 21/11/2018.
 */
open class BaseActivity : AppCompatActivity() {

    // -------------------
    // CONFIGURATION
    // -------------------

    fun configureBottomView(){
        bottom_navigation_view.setOnNavigationItemSelectedListener{showFragment(it.itemId)}
    }

    // -------------------
    // UI
    // -------------------

    private fun showFragment(fragmentId:Int):Boolean{
        Log.e("BASE_ACTIVITY", "Click on : $fragmentId")
        return true
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
}