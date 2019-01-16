package com.deguffroy.adrien.projetphoto.Views

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.R
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState

/**
 * Created by Adrien Deguffroy on 08/01/2019.
 */
class DetailActivityAdapter(options: FirestorePagingOptions<Comment>, private val callback:Listener) : FirestorePagingAdapter<Comment, DetailActivityViewHolder>(options) {

    interface Listener{
        fun onOptionsClickButton(comment: Comment)
        fun startLoading()
        fun loaded()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailActivityViewHolder {
        return DetailActivityViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_detail_comment_item, parent, false))
    }

    override fun onBindViewHolder(p0: DetailActivityViewHolder, p1: Int, p2: Comment) {
        p0.updateWithData(p2,this.callback)
    }

    override fun onLoadingStateChanged(state: LoadingState) {
        super.onLoadingStateChanged(state)
        when (state) {
            LoadingState.LOADING_INITIAL -> {
                Log.e("DetailAdapter","LOADING_INITIAL")
                callback.startLoading()
            }
            LoadingState.LOADING_MORE -> {
                Log.e("DetailAdapter","LOADING_MORE")
            }
            LoadingState.LOADED -> {
                Log.e("DetailAdapter","LOADED")
                callback.loaded()}
            LoadingState.ERROR -> {
                Log.e("DetailAdapter","ERROR")}
            LoadingState.FINISHED -> {
                Log.e("DetailAdapter","FINISHED")
                callback.loaded()}
        }
    }

}