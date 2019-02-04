package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.ColorFilter
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Api.*
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.OptionsModalFragment
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.User
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.DividerItemDecoration
import com.deguffroy.adrien.projetphoto.Views.DetailActivityAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : BaseActivity(), DetailActivityAdapter.Listener, OptionsModalFragment.Listener {

    private lateinit var adapter:DetailActivityAdapter
    private var userIsOwner:Boolean? = null
    private var isPublicPicture:Boolean? = null
    private var isPictureDenyByModeration:Boolean? = null
    private var previousCommentCount:Long = 0

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

        mViewModel.getPicture(this.documentId!!).observe(this, Observer {
            Log.e("DetailActivity","Observe change!")
            this.updateOnObserve(it)
        })

        this.getCurrentUserFromFirestore()
        this.configureRecyclerView()
        this.setOnClickListener()
        this.incrementView()
        //this.createImage()
    }

    //ONLY FOR TESTING
    private fun createImage(){
        UserHelper().getUser(FirebaseAuth.getInstance().currentUser?.uid!!).addOnCompleteListener {
            if (it.isSuccessful){
                val user = it.result?.toObject(User::class.java)!!
                (0 until 2).forEach {
                    PicturesHelper().createPicture(user,"",true,"").addOnSuccessListener {pictureAdd->
                        Log.e("DetailActivity","Image successfully created!")
                        PicturesHelper().updatePictureDocumentID(pictureAdd.id)
                    }
                }
            }
        }
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

        detail_activity_chip_public.setOnClickListener { this.toggleVisibilityScope((!this.isPublicPicture!!),false, this.isPictureDenyByModeration!!) }
        detail_activity_chip_private.setOnClickListener { this.toggleVisibilityScope((!this.isPublicPicture!!),false, this.isPictureDenyByModeration!!) }
    }

    private fun incrementView(){
        val userUID = getCurrentUser()?.uid!!
        ViewsHelper().checkIfUserAlreadyViewThisPicture(this.documentId!!,userUID).addOnCompleteListener {
            if(it.isSuccessful){
                if (it.result?.isEmpty!!){ // USER DIDN'T SEEN THIS PICTURE YET
                    Log.e("DetailActivity","User didn't seen this picture, incrementing counter!")
                    ViewsHelper().createNewView(this.documentId!!, userUID).addOnCompleteListener { createTask ->
                        if (createTask.isSuccessful){
                            val db = FirebaseFirestore.getInstance()
                            val docRef = PicturesHelper().getPicturesCollection().document(this.documentId!!)
                            db.runTransaction { transaction ->
                                val snapshot = transaction.get(docRef)
                                val newCount = (snapshot.get("views")!! as Long) + 1
                                transaction.update(docRef, "views", newCount)
                            }.addOnSuccessListener {
                                Log.e("DetailActivity","Success incrementing views! ")
                            }.addOnFailureListener {errorTask->
                                Log.e("DetailActivity","Error incrementing views : ${errorTask.localizedMessage}")
                            }
                        }
                    }

                }else{ // USER ALREADY SEEN THIS PICTURE
                    Log.e("DetailActivity","User already seen this picture")
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
                CommentsHelper().updateCommentDocumentId(it.id)
                activity_detail_comment_field.text = null

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

                val db = FirebaseFirestore.getInstance()
                val docRef = PicturesHelper().getPicturesCollection().document(this.documentId!!)

                db.runTransaction {transaction ->
                    val currentCommentCount = transaction.get(docRef)
                    val newCommentCount = (currentCommentCount.get("comments") as Long) + 1
                    transaction.update(docRef, "comments", newCommentCount)
                }.addOnSuccessListener {
                    Log.e("DetailActivity","Success incrementing comments count! ")
                }.addOnFailureListener { failure ->
                    Log.e("DetailActivity","Error incrementing comments count : ${failure.localizedMessage}")
                }

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
            displayMessage(resources.getString(R.string.detail_activity_error_reporting_comment))
        }
    }

    override fun startLoading() {
        detail_activity_comment_recycler_view.visibility = View.GONE
        detail_activity_recycler_loading.visibility = View.VISIBLE
    }

    override fun loaded() {
        detail_activity_comment_recycler_view.visibility = View.VISIBLE
        detail_activity_recycler_loading.visibility = View.GONE
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

    private fun displayChipsContainer(mustDisplayContainer:Boolean, isPublicPicture:Boolean){
        Log.e("DetailActivity","isPublicPicture = $isPublicPicture")
        this.userIsOwner = mustDisplayContainer
        this.isPublicPicture = isPublicPicture
        if(mustDisplayContainer){
            detail_activity_chips_container.visibility = View.VISIBLE
            this.toggleVisibilityScope(isPublicPicture, true, false)
        }else{
            detail_activity_chips_container.visibility = View.GONE
        }
    }

    private fun toggleVisibilityScope(setToPublic:Boolean, whenUpdatingUI:Boolean = false, isDenyByModeration:Boolean){
        if (!whenUpdatingUI){
            if (isDenyByModeration){
                this.displayMessage(resources.getString(R.string.detail_activity_error_already_deny_by_moderation))
                this.toggleVisibilityOnUI(false)
                return
            }else{
                PicturesHelper().toggleVisibilityScope(this.documentId!!, setToPublic).addOnSuccessListener {
                    this.displayMessage(resources.getString(R.string.detail_activity_success_toggle_visibility))
                    this.toggleVisibilityOnUI(setToPublic)
                    return@addOnSuccessListener
                }.addOnFailureListener { failureTask->
                    Log.e("DetailActivity","Fail to toggle visibility scope! ${failureTask.localizedMessage}")
                    this.displayMessage(resources.getString(R.string.detail_activity_error_toggle_visibility))
                    return@addOnFailureListener
                }
            }
        }
        this.toggleVisibilityOnUI(setToPublic)
    }

    // -------------------
    // UI
    // -------------------

    private fun toggleVisibilityOnUI(setToPublic: Boolean){
        Log.e("DetailActivity","toggleVisibilityOnUI = $setToPublic")
        if (setToPublic){
            detail_activity_chip_public.isChecked = true
            detail_activity_chip_private.isChecked = false
        }else{
            detail_activity_chip_public.isChecked = false
            detail_activity_chip_private.isChecked = true
        }
        this.isPublicPicture = setToPublic
    }

    private fun updateUIWhenCreating(){
        val glide = Glide.with(this)
        PicturesHelper().getPictureById(documentId!!).addOnSuccessListener {
            val denyReason = (it.get("denyReason") as String?)
            if (denyReason != null) {
                Log.e("DetailActivity", "isPictureDenyByModeration = ${!denyReason.isBlank()}")
                this.isPictureDenyByModeration = !denyReason.isBlank()
            }else{
                this.isPictureDenyByModeration = false
            }

            this.previousCommentCount = (it.get("comments") as Long)

            this.displayChipsContainer(
                (it.get("userSender.uid") as String) == getCurrentUser()?.uid!!,
                (it.get("public") as Boolean))

            this.imageURL = (it.get("urlImage") as String)
            glide.load(this.imageURL)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(this.getCircularPlaceHolder(5F,30F)))
                .into(detail_activity_image)

            if(!(it.get("description") as String).isEmpty()){
                detail_activity_description_title.text = resources.getString(R.string.detail_activity_description)
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

    private fun updateOnObserve(picture: Picture){
        social_view_views.text = picture.views.toString()
        social_view_likes.text = picture.likes.toString()

        if (!picture.description.isNullOrEmpty()){
            detail_activity_description_title.visibility = View.VISIBLE
            detail_activity_desc.setText(picture.description.toString(), TextView.BufferType.EDITABLE)
        }

        if (picture.comments?.toLong() != previousCommentCount) this.configureRecyclerView()

    }

    override fun displayMessage(message: String) {
        this.showSnackbarMessage(detail_activity_coordinator_layout,message, Snackbar.LENGTH_LONG)
    }

    private fun getCircularPlaceHolder(strokeWidth:Float, centerRadius:Float):CircularProgressDrawable{
        val circularPlaceHolder = CircularProgressDrawable(this)
        circularPlaceHolder.strokeWidth = strokeWidth
        circularPlaceHolder.centerRadius = centerRadius
        circularPlaceHolder.start()
        return circularPlaceHolder
    }
}
