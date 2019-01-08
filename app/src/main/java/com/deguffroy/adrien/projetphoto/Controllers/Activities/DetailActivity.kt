package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Api.ViewsHelper
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Views.DetailActivityAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_main.*

class DetailActivity : BaseActivity() {

    private lateinit var adapter:DetailActivityAdapter

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
            this.adapter = DetailActivityAdapter(generateOptionsForAdapter(CommentsHelper().getCommentsForPicture(this.documentId!!)))
            detail_activity_comment_recycler_view.layoutManager = LinearLayoutManager(this)
            detail_activity_comment_recycler_view.adapter = this.adapter
        }else{
            BaseActivity().showSnackbarMessage(detail_activity_coordinator_layout,resources.getString(R.string.detail_activity_error_retrieving_comments))
        }
    }

    private fun generateOptionsForAdapter(query: Query) = FirestoreRecyclerOptions.Builder<Comment>()
        .setQuery(query, Comment::class.java)
        .setLifecycleOwner(this)
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

    // -------------------
    // UI
    // -------------------

    private fun updateUIWhenCreating(){
        val glide = Glide.with(this)
        PicturesHelper().getPictureById(documentId!!).addOnSuccessListener {
            this.imageURL = (it.get("urlImage") as String)
            glide.load(this.imageURL).into(detail_activity_image)
            detail_activity_desc.text = it.get("description").toString()
            social_view_views.text = (it.get("views") as Long).toInt().toString()
        }
    }
}