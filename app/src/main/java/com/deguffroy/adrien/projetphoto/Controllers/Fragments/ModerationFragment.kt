package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.deguffroy.adrien.projetphoto.Api.BottomNavHelper
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.MainActivity

import com.deguffroy.adrien.projetphoto.R
import kotlinx.android.synthetic.main.activity_main.*
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
        this.setListener()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun setListener(){
        fragment_moderation_swipe_refresh.setOnRefreshListener { this.checkItemNeedingModeration() }
    }

    // -------------------
    // ACTION
    // -------------------

    private fun checkItemNeedingModeration(){
        fragment_moderation_swipe_refresh.isRefreshing = true
        var noResult = true
        PicturesHelper().getAllPublicPictureNeedingVerification().get().addOnCompleteListener {
            if (it.isSuccessful){
                noResult = it.result?.isEmpty!!
                Log.e("ModerationFrag","Picture result = $noResult")
                this.setViewVisibility(fragment_moderation_picture_card,fragment_moderation_picture_number, it.result?.size()!!, noResult)
            }

            CommentsHelper().getCommentsReported().get().addOnCompleteListener {commentTask ->
                if (commentTask.isSuccessful){
                    noResult = commentTask.result?.isEmpty!!
                    Log.e("ModerationFrag","Comment result = $noResult")
                    this.setViewVisibility(fragment_moderation_comment_card,fragment_moderation_comment_number, commentTask.result?.size()!!, noResult)
                }
            }

            activity?.runOnUiThread{
                if (fragment_moderation_swipe_refresh != null) fragment_moderation_swipe_refresh.isRefreshing = false
            }

            if (noResult) {
                (activity as MainActivity).showSnackbarMessage(fragment_moderation_coordinator_layout, resources.getString(R.string.moderation_fragment_nothing_to_do_message))
                BottomNavHelper().removeBadge((activity as MainActivity).bottom_navigation_view, R.id.bnv_moderation)
            }else{
                BottomNavHelper().showBadge((activity as MainActivity), (activity as MainActivity).bottom_navigation_view, R.id.bnv_moderation)
            }
        }
    }

    // -------------------
    // UI
    // -------------------

    private fun setViewVisibility(containerVisibilityToChange:View, numberView:TextView, itemCount:Int, noResult:Boolean){
        if(noResult){
            containerVisibilityToChange.visibility = View.GONE
            numberView.text = "0"
        }else{
            containerVisibilityToChange.visibility = View.VISIBLE
            numberView.text = itemCount.toString()
        }

    }

}
