package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.deguffroy.adrien.projetphoto.R

class ModerationFragment : Fragment() {

    companion object {
        fun newInstance():ModerationFragment = ModerationFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moderation, container, false)
    }


}
