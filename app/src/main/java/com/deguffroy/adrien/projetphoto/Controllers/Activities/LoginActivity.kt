package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.os.Bundle
import com.deguffroy.adrien.projetphoto.R
import android.content.Intent
import android.util.Log
import com.firebase.ui.auth.AuthUI
import java.util.*
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.deguffroy.adrien.projetphoto.Api.UserHelper
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {

    // FOR DATA
    private val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if(!this.isCurrentUserLogged()!!){
            this.startSignInActivity()
        }else{
            this.launchMainActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle SignIn Activity response on activity result
        if (data != null) {
            this.handleResponseAfterSignIn(requestCode, resultCode, data)
        }
    }

    private fun startSignInActivity() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    Arrays.asList(
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.AnonymousBuilder().build()
                    )
                )
                .setIsSmartLockEnabled(false, true)
                .build(),
            RC_SIGN_IN
        )
    }

    // --------------------
    // REST REQUEST
    // --------------------

    // Http request that create user in firestore
    private fun createUserInFirestore() {

        if (this.getCurrentUser() != null) {
            Log.e("LOGIN_ACTIVITY", "createUserInFirestore: LOGGED")
            val urlPicture =
                if (this.getCurrentUser()!!.photoUrl != null) this.getCurrentUser()!!.photoUrl!!.toString() else null
            val username =
                if (this.getCurrentUser()!!.displayName != null) this.getCurrentUser()!!.displayName else resources.getString(R.string.user_default_name)
            val uid = this.getCurrentUser()!!.uid
            this.mViewModel.updateCurrentUserUID(uid)
            UserHelper().createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener())
        } else {
            Log.e("LOGIN_ACTIVITY", "createUserInFirestore: NOT LOGGED")
        }
    }

    // --------------------
    // UTILS
    // --------------------

    private fun handleResponseAfterSignIn(requestCode: Int, resultCode: Int, data: Intent) {

        val response = IdpResponse.fromResultIntent(data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore()
                launchMainActivity()
            } else { // ERRORS
                when {
                    response == null -> {
                        Toast.makeText(this, getString(R.string.error_authentication_canceled), Toast.LENGTH_SHORT).show()
                        startSignInActivity()
                    }
                    response.error!!.errorCode == ErrorCodes.NO_NETWORK -> showSnackbarMessage(coordinator_layout_login_activity,getString(R.string.error_no_internet))
                    response.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR -> showSnackbarMessage(coordinator_layout_login_activity,getString(R.string.error_unknown_error))
                }
            }
        }
    }

    // --------------------
    // ACTION
    // --------------------

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
