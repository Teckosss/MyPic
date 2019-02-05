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
import com.deguffroy.adrien.projetphoto.Controllers.Activities.ModerationActivity
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.Models.Picture

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.MODERATION_COMMENTS
import com.deguffroy.adrien.projetphoto.Utils.MODERATION_PICTURES
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_moderation.*

class ModerationFragment : BaseFragment() {

    private lateinit var listPicture:ArrayList<String>
    private lateinit var listComments:ArrayList<String>

    companion object {
        fun newInstance():ModerationFragment = ModerationFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moderation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.setListener()
    }

    override fun onResume() {
        super.onResume()
        this.checkItemNeedingModeration()
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

    // Check if any picture or comment need moderation, if yes we display a badge to moderation icon
    private fun checkItemNeedingModeration(){
        fragment_moderation_swipe_refresh.isRefreshing = true
        var noResult: Boolean
        var needToShowBadge = false
        PicturesHelper().getAllPublicPictureNeedingVerification().get().addOnCompleteListener {
            if (it.isSuccessful){
                noResult = it.result?.isEmpty!!
                if (!(noResult)){
                    needToShowBadge = true
                    this.listPicture = arrayListOf()
                    for (document in it.result!!){
                        val picture = document.toObject(Picture::class.java)
                        this.listPicture.add(picture.documentId!!)
                    }
                }
                Log.i("ModerationFrag","Picture no result = $noResult")
                if (fragment_moderation_picture_card != null){
                    this.setViewVisibility(fragment_moderation_picture_card,fragment_moderation_picture_number, it.result?.size()!!, noResult, MODERATION_PICTURES)
                }

            }

            CommentsHelper().getCommentsReported().get().addOnCompleteListener {commentTask ->
                if (commentTask.isSuccessful){
                    activity?.runOnUiThread{
                        if (fragment_moderation_swipe_refresh != null) fragment_moderation_swipe_refresh.isRefreshing = false
                    }
                    noResult = commentTask.result?.isEmpty!!
                    if (!(noResult)){
                        needToShowBadge = true
                        this.listComments = arrayListOf()
                        for (document in commentTask.result!!){
                            val comment = document.toObject(Comment::class.java)
                            this.listComments.add(comment.documentId!!)
                        }
                    }
                    Log.i("ModerationFrag","Comment no result = $noResult")
                    if (fragment_moderation_comment_card != null){
                        this.setViewVisibility(fragment_moderation_comment_card,fragment_moderation_comment_number, commentTask.result?.size()!!, noResult, MODERATION_COMMENTS)
                    }
                }
                if (!needToShowBadge) {
                    if (fragment_moderation_coordinator_layout != null){
                        (activity as MainActivity).showSnackbarMessage(fragment_moderation_coordinator_layout, resources.getString(R.string.moderation_fragment_nothing_to_do_message))
                    }
                    BottomNavHelper().removeBadge((activity as MainActivity).bottom_navigation_view)
                }else{
                    BottomNavHelper().showBadge((activity as MainActivity), (activity as MainActivity).bottom_navigation_view, R.id.bnv_moderation)
                }

            }
        }
    }

    // -------------------
    // UI
    // -------------------

    // Display or not picture or comment CardView if there is something to moderate
    private fun setViewVisibility(containerVisibilityToChange:View, numberView:TextView, itemCount:Int, noResult:Boolean, documentType:String){
        if(noResult){
            containerVisibilityToChange.visibility = View.GONE
            numberView.text = "0"
        }else{
            containerVisibilityToChange.visibility = View.VISIBLE
            numberView.text = itemCount.toString()
            containerVisibilityToChange.setOnClickListener { this.launchActivity(documentType) }
        }

    }

    private fun launchActivity(documentType: String){
        when(documentType){
            MODERATION_PICTURES -> startActivity(ModerationActivity.newInstance(activity!!, this.listPicture, documentType))
            MODERATION_COMMENTS -> startActivity(ModerationActivity.newInstance(activity!!, this.listComments, documentType))
        }
    }

}
