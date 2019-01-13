package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Models.Picture

import com.deguffroy.adrien.projetphoto.R


class PicturesPageFragment : ModerationBaseFragment() {

    private var position: Int = 0
    private var documentId = ""
    private var itemCount:Int? = null
    private lateinit var rootView:ConstraintLayout

    companion object {
        private const val POSITION = "POSITION"
        private const val ITEM_COUNT = "ITEM_COUNT"
        private const val DOCUMENT_ID = "DOCUMENT_ID"

        fun newInstance(position:Int, itemCount:Int, documentId:String):PicturesPageFragment{
           val fragment = PicturesPageFragment()
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
        return inflater.inflate(R.layout.picture_page_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("PicturePageFrag","View Created!")

        this.position = arguments?.getInt(POSITION)!!
        this.documentId = arguments?.getString(DOCUMENT_ID)!!
        this.itemCount = arguments?.getInt(ITEM_COUNT)
        this.rootView = view.findViewById(R.id.picture_page_container)

        this.setOnClickListener()
        this.updateUI()
    }

    // -------------------
    // ACTION
    // -------------------

    override fun onClickAcceptButton() {

    }

    override fun onClickDenyButton() {

    }

    // -------------------
    // UI
    // -------------------

    private fun updateUI(){
        val glide = Glide.with(activity!!)

        activity?.title = resources.getString(R.string.moderation_fragment_title_pictures)

        Log.e("PageFragment","Position : $position")
        Log.e("PageFragment","DocumentId : $documentId")

        PicturesHelper().getPictureById(documentId).addOnSuccessListener {
            val picture = it.toObject(Picture::class.java)
            glide.load(picture?.urlImage).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(rootView.findViewById(R.id.picture_page_image_view))

            rootView.findViewById<TextView>(R.id.picture_page_text_view).text = "Numéro de page $position"
        }

        rootView.findViewById<TextView>(R.id.moderation_top_text_view).text = resources.getString(R.string.moderation_fragment_item_count, position + 1, itemCount)
    }

}
