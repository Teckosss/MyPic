package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.MainActivity

import com.deguffroy.adrien.projetphoto.R
import kotlinx.android.synthetic.main.fragment_moderation.*

class ModerationFragment : BaseFragment() {

    companion object {
        fun newInstance():ModerationFragment = ModerationFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moderation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.checkItemNeedingModeration()
    }

    // -------------------
    // CONFIGURATION
    // -------------------



    // -------------------
    // ACTION
    // -------------------

    private fun checkItemNeedingModeration(){
        var noResult = true
        PicturesHelper().getAllPublicPictureNeedingVerification().get().addOnCompleteListener {
            if (it.isSuccessful){
                noResult = if (!(it.result?.isEmpty!!)){
                    this.setViewVisible(fragment_moderation_picture_card,fragment_moderation_picture_number, it.result?.size()!!)
                    false
                }else{
                    true
                }
            }
        }

        CommentsHelper().getCommentsReported().get().addOnCompleteListener {
            if (it.isSuccessful){
                noResult = if (!(it.result?.isEmpty!!)){
                    this.setViewVisible(fragment_moderation_comment_card,fragment_moderation_comment_number, it.result?.size()!!)
                    false
                }else{
                    true
                }
            }
        }

        if (noResult) (activity as MainActivity).showSnackbarMessage(fragment_moderation_coordinator_layout, resources.getString(R.string.moderation_fragment_nothing_to_do_message))

    }

    // -------------------
    // UI
    // -------------------

    private fun setViewVisible(containerToShow:View, numberView:TextView, itemCount:Int){
        containerToShow.visibility = View.VISIBLE
        numberView.text = itemCount.toString()
    }

}
