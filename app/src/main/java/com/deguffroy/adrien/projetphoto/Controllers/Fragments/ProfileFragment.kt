package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.LoginActivity

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.Constants.SIGN_OUT_TASK
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.fragment_profile.*
import com.google.android.gms.tasks.OnSuccessListener



/**
 * A simple [Fragment] subclass.
 *
 */
class ProfileFragment : BaseFragment() {

    companion object {
        fun newInstance():ProfileFragment{
            return ProfileFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.configureOnClickListener()
    }

    private fun configureOnClickListener(){
        profile_sign_out_button.setOnClickListener {this.signOutUserFromFirebase() }
    }

    private fun signOutUserFromFirebase(){
        AuthUI.getInstance().signOut(context!!).addOnCompleteListener {
            val intent = Intent(context,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
