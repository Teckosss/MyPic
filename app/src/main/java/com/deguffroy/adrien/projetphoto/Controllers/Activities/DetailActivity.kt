package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.LikesHelper
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Api.ViewsHelper
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.OptionsModalFragment
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.DividerItemDecoration
import com.deguffroy.adrien.projetphoto.Views.DetailActivityAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : BaseActivity(), DetailActivityAdapter.Listener, OptionsModalFragment.Listener {

    private lateinit var adapter:DetailActivityAdapter
    private var userLikeThisPicture:Boolean = false
    private var likeId:String? = null

    private var documentId:String? = null
    private var imageURL:String? = null

    companion object {
        private const val DOCUMENT_ID = "DOCUMENT_ID"
        fun newInstance(context: Context, document_id:String?) = Intent(context, DetailActivity::class.java).apply { putExtra(
            DOCUMENT_ID, document_id) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        this.documentId = intent.getStringExtra(DOCUMENT_ID)
        if (!(this.documentId.isNullOrEmpty())){
            updateUIWhenCreating()
        }else{
            BaseActivity().showSnackbarMessage(detail_activity_coordinator_layout,resources.getString(R.string.detail_activity_error_retrieving_document_uid))
        }

        this.getCurrentUserFromFirestore()
        this.configureRecyclerView()
        this.setOnClickListener()
        this.incrementView()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun configureRecyclerView(){
        if (this.documentId != null){

            this.adapter = DetailActivityAdapter(generateOptionsForAdapter(CommentsHelper().getCommentsForPicture(this.documentId!!)),this)
            detail_activity_comment_recycler_view.layoutManager = LinearLayoutManager(this)
            detail_activity_comment_recycler_view.adapter = this.adapter
            detail_activity_comment_recycler_view.addItemDecoration(DividerItemDecoration(this,0,0))
        }else{
            BaseActivity().showSnackbarMessage(detail_activity_coordinator_layout,resources.getString(R.string.detail_activity_error_retrieving_comments))
        }
    }

    private fun generateOptionsForAdapter(query: Query) = FirestorePagingOptions.Builder<Comment>()
        .setQuery(query, generateConfig() ,Comment::class.java)
        .setLifecycleOwner(this)
        .build()

    private fun generateConfig() = PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setPrefetchDistance(10)
        .setPageSize(20)
        .build()

    private fun setOnClickListener(){
        detail_activity_image.setOnClickListener {
            if (this.imageURL != null) {
                startActivity(FullscreenActivity.newInstance(this , this.imageURL!!))
            }
        }
        activity_detail_send_comment.setOnClickListener {
            if (!(activity_detail_comment_field.text.isNullOrBlank())){
                this.sendComment(activity_detail_comment_field.text.toString())
            }
        }
        activity_detail_fab.setOnClickListener { this.clickOnLike() }
    }

    private fun incrementView(){
        val userUID = getCurrentUser()?.uid!!
        ViewsHelper().checkIfUserAlreadyViewThisPicture(this.documentId!!,userUID).addOnCompleteListener {
            if(it.isSuccessful){
                if (it.result?.isEmpty!!){ // USER DIDN'T SEEN THIS PICTURE YET
                    Log.e("DetailFragment","User didn't seen this picture, incrementing counter!")
                    ViewsHelper().createNewView(this.documentId!!, userUID).addOnCompleteListener { createTask ->
                        if (createTask.isSuccessful){
                            val db = FirebaseFirestore.getInstance()
                            val docRef = PicturesHelper().getPicturesCollection().document(this.documentId!!)
                            db.runTransaction { transaction ->
                                val snapshot = transaction.get(docRef)
                                val newCount = (snapshot.get("views")!! as Long) + 1
                                transaction.update(docRef, "views", newCount)
                            }.addOnSuccessListener {
                                Log.e("DetailFragment","Success incrementing views! ")
                            }.addOnFailureListener {errorTask->
                                Log.e("DetailFragment","Error incrementing views : ${errorTask.localizedMessage}")
                            }
                        }
                    }

                }else{ // USER ALREADY SEEN THIS PICTURE
                    Log.e("DetailFragment","User already seen this picture")
                }
            }
        }
    }

    // -------------------
    // ACTION
    // -------------------

    private fun sendComment(commentText:String){
        if (this.documentId != null){
            CommentsHelper().createComment(commentText, this.documentId!!, this.modelCurrentUser).addOnSuccessListener {
                CommentsHelper().updateCommentDocumentID(it.id)
                activity_detail_comment_field.text = null
            }.addOnFailureListener {
                Log.e("DetailActivity","Error sending comment : ${it.localizedMessage}")
                this.showSnackbarMessage(detail_activity_coordinator_layout, resources.getString(R.string.detail_activity_error_sending_comment))
            }
        }else{
            this.showSnackbarMessage(detail_activity_coordinator_layout, resources.getString(R.string.detail_activity_error_sending_comment))
        }
    }

    override fun onOptionsClickButton(comment: Comment) {
        if (comment.documentId != null){
            OptionsModalFragment.newInstance(comment.documentId!!, modelCurrentUser.uid).show(supportFragmentManager, "MODAL")
        }else{
            Log.e("DetailActivity","Error! Unable to open bottom sheet fragment. CommentId = ${comment.documentId}")
        }
    }

    private fun clickOnLike(){
        if (userLikeThisPicture){
            if (this.likeId != null){
                LikesHelper().deleteLikeForUser(this.likeId!!).addOnSuccessListener {
                    this.runTransactionToUpdateLikeCount(false)
                }
            }

        }else{
            LikesHelper().createLike(this.documentId!!, this.getCurrentUser()?.uid!!).addOnSuccessListener {
                this.userLikeThisPicture = true
                this.likeId = it.id
                this.runTransactionToUpdateLikeCount(true)
            }
        }
    }

    private fun runTransactionToUpdateLikeCount(toIncrement:Boolean){
        val db = FirebaseFirestore.getInstance()
        val docRef = PicturesHelper().getPicturesCollection().document(this.documentId!!)

        db.runTransaction { transaction->
            val currentLikeCount = transaction.get(docRef)
            val newLikeCount:Long
            newLikeCount = if(toIncrement){
                (currentLikeCount.get("likes")!! as Long) + 1
            }else{
                (currentLikeCount.get("likes")!! as Long) - 1
            }
            Log.e("DetailActivity","Transaction like new cunt : $newLikeCount")
            transaction.update(docRef,"likes", if (newLikeCount >=0) newLikeCount else 0)
        }.addOnSuccessListener { success->
            Log.e("DetailActivity","Transaction success")
            this.userLikeThisPicture = toIncrement
            this.toggleLike(this.userLikeThisPicture)

        }.addOnFailureListener { failure->
            Log.e("DetailActivity","Transaction failed! | ${failure.localizedMessage}")
        }

    }

    private fun toggleLike(userLike:Boolean){
        if(userLike){
            activity_detail_fab.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_favorite_border_white_24dp))
            activity_detail_fab.backgroundTintList = (ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_favorite)))
        }else{
            activity_detail_fab.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.baseline_favorite_black_24))
            activity_detail_fab.backgroundTintList= (ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGrey)))
        }
        activity_detail_fab.show()
    }

    // -------------------
    // UI
    // -------------------

    private fun updateUIWhenCreating(){
        val glide = Glide.with(this)
        PicturesHelper().getPictureById(documentId!!).addOnSuccessListener {
            this.imageURL = (it.get("urlImage") as String)
            glide.load(this.imageURL).into(detail_activity_image)
            if(!(it.get("description") as String).isEmpty()){
                detail_activity_description_title.visibility = View.VISIBLE
                detail_activity_desc.text = it.get("description").toString()
            }
            social_view_views.text = (it.get("views") as Long).toInt().toString()
            social_view_likes.text = (it.get("likes") as Long).toInt().toString()

            LikesHelper().checkIfUserAlreadyLikedThisPicture(documentId!!, this.getCurrentUser()?.uid!!).get().addOnSuccessListener {likesTask ->
                this.userLikeThisPicture = !likesTask.isEmpty
                this.toggleLike(this.userLikeThisPicture)
                if (this.userLikeThisPicture){
                    for (like in likesTask){
                        this.likeId = like.id
                    }
                }
            }
        }
    }

    override fun displayMessage(message: String) {
        this.showSnackbarMessage(detail_activity_coordinator_layout,message, Snackbar.LENGTH_LONG)
    }
}
