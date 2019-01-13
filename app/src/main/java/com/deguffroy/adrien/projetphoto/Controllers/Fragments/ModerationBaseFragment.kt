package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.moderation_buttons_view.*

/**
 * Created by Adrien Deguffroy on 13/01/2019.
 */
abstract class ModerationBaseFragment : Fragment() {

    fun setOnClickListener(){
        moderation_buttons_accept.setOnClickListener { this.onClickAcceptButton() }

        moderation_buttons_deny.setOnClickListener { this.onClickDenyButton() }
    }

    // -------------------
    // ABSTRACT METHOD
    // -------------------

    abstract fun onClickAcceptButton()

    abstract fun onClickDenyButton()
}