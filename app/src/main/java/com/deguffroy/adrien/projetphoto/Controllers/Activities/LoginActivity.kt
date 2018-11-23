package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.os.Bundle
import com.deguffroy.adrien.projetphoto.R
import android.content.Intent
import com.firebase.ui.auth.AuthUI
import java.util.*
import android.widget.Toast
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse

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
                        AuthUI.IdpConfig.AnonymousBuilder().build()
                    )
                )
                .setIsSmartLockEnabled(false, true)
                .build(),
            RC_SIGN_IN
        )
    }

    // --------------------
    // UTILS
    // --------------------

    private fun handleResponseAfterSignIn(requestCode: Int, resultCode: Int, data: Intent) {

        val response = IdpResponse.fromResultIntent(data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                //this.createUserInFirestore()
                launchMainActivity()
            } else { // ERRORS
                when {
                    response == null -> {
                        Toast.makeText(this, getString(R.string.error_authentication_canceled), Toast.LENGTH_SHORT).show()
                        startSignInActivity()
                    }
                    response.error!!.errorCode == ErrorCodes.NO_NETWORK -> Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show()
                    response.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR -> Toast.makeText(this, getString(R.string.error_unknown_error), Toast.LENGTH_SHORT).show()
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
