package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.deguffroy.adrien.projetphoto.R


/**
 * A simple [Fragment] subclass.
 *
 */
class DetailFragment : BaseFragment() {

    companion object {
        fun newInstance():DetailFragment{
            return DetailFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}
