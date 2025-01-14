package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Api.ReportsHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.ModerationActivity
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Created by Adrien Deguffroy on 11/01/2019.
 */
class CommentsPageFragment : ModerationBaseFragment() {

    private var position: Int = 0
    private var commentId = ""
    private var itemCount:Int? = null

    private lateinit var rootView:ConstraintLayout
    private lateinit var pictureId:String
    private lateinit var listCommentIdToDelete:ArrayList<String>
    private lateinit var previousText:String

    companion object {
        private const val POSITION = "POSITION"
        private const val ITEM_COUNT = "ITEM_COUNT"
        private const val DOCUMENT_ID = "DOCUMENT_ID"

        fun newInstance(position:Int, itemCount:Int, documentId:String):CommentsPageFragment{
            val fragment = CommentsPageFragment()
            val bundle = Bundle()
            bundle.putInt(POSITION, position)
            bundle.putInt(ITEM_COUNT, itemCount)
            bundle.putString(DOCUMENT_ID, documentId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.comment_page_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("CommentPageFrag","View Created!")

        this.position = arguments?.getInt(CommentsPageFragment.POSITION)!!
        this.commentId = arguments?.getString(CommentsPageFragment.DOCUMENT_ID)!!
        this.itemCount = arguments?.getInt(ITEM_COUNT)
        this.rootView = view.findViewById(R.id.comment_page_container)

        this.setOnClickListener()
        this.updateUI()

    }

    // -------------------
    // ACTION
    // -------------------

    // When click "Accept" nothing change for comment
    override fun onClickAcceptButton() {
        if (rootView.findViewById<EditText>(R.id.comment_page_text_view).text.toString() != this.previousText){
            CommentsHelper().updateCommentTextById(this.commentId, rootView.findViewById<EditText>(R.id.comment_page_text_view).text.toString()).addOnSuccessListener {
                this.moveToNextAndReset(false)
            }
        }
        this.moveToNextAndReset(false)
    }

    // When click on "Deny", Picture's comment decrement
    override fun onClickDenyButton() {
        val db = FirebaseFirestore.getInstance()
        val docRef = PicturesHelper().getPicturesCollection().document(this.pictureId)
        val docToDelete = CommentsHelper().getCommentsCollection().document(this.commentId)

        db.runTransaction { transaction ->
            val currentCommentCount = transaction.get(docRef)
            var newCommentCount = (currentCommentCount.get("comments") as Long) - 1
            if (newCommentCount < 0) newCommentCount = 0
            transaction.update(docRef, "comments", newCommentCount)
            transaction.delete(docToDelete)
        }.addOnSuccessListener {
            Log.i("PageFragment","Success update comment count")
            this.moveToNextAndReset(true)
        }.addOnFailureListener {failure->
            Log.e("PageFragment","Fail update comment count! ${failure.localizedMessage}")
        }
    }

    // Depending on boolean comment is delete or not. Reset report count for this comment
    private fun resetReportCountAndDeleteReports(deleteComment:Boolean){
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        ReportsHelper().getAllReportForAComment(commentId).get().addOnSuccessListener {
            if (!(it.isEmpty)){
                this.listCommentIdToDelete = arrayListOf()
                for (document in it){
                    this.listCommentIdToDelete.add(document.id) // Adding each document to list for the batch
                }
                if (!deleteComment) batch.update(CommentsHelper().getCommentsCollection().document(commentId),"reportCount",0)
                listCommentIdToDelete.forEach{commentIdToDelete ->
                    batch.delete(ReportsHelper().getReportsCollection().document(commentIdToDelete)) // Creating a batch to delete each element in list
                }
                batch.commit().addOnCompleteListener {
                    Log.i("CommentPage","Batch success")
                }.addOnFailureListener {batchFailure ->
                    Log.e("CommentPage","Batch Failure | ${batchFailure.localizedMessage}")
                }
            }
        }
    }

    private fun moveToNextAndReset(deleteComment:Boolean){
        (activity as ModerationActivity).moveToNext(this.position)
        this.resetReportCountAndDeleteReports(deleteComment)
    }

    // -------------------
    // UI
    // -------------------

    private fun updateUI(){
        val glide = Glide.with(activity!!)
        val itemCount =  arguments?.getInt(ITEM_COUNT)

        activity?.title = resources.getString(R.string.moderation_fragment_title_comments)

        CommentsHelper().getCommentById(commentId).addOnSuccessListener {
            val comment = it.toObject(Comment::class.java)
            rootView.findViewById<TextView>(R.id.comment_page_text_view).text = comment?.commentText
            this.previousText = comment?.commentText!!

            PicturesHelper().getPictureById(comment.pictureId).addOnSuccessListener { pictureTask->
                val picture = pictureTask.toObject(Picture::class.java)
                this.pictureId = picture?.documentId!!
                glide.load(picture.urlImage).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(rootView.findViewById(R.id.comment_page_image_view))
            }
        }

        rootView.findViewById<TextView>(R.id.moderation_top_text_view).text = resources.getString(R.string.moderation_fragment_item_count, position + 1, itemCount)
    }
}