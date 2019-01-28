package com.deguffroy.adrien.projetphoto.Views

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions

/**
 * Created by Adrien Deguffroy on 21/01/2019.
 */
class HomePagingAdapter(var options: FirestorePagingOptions<Picture>) : FirestorePagingAdapter<Picture, HomeViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_home_item, parent, false))
    }

    fun getItemAtPosition(position: Int) = (options.data.value)?.get(position)?.toObject(Picture::class.java)

    override fun onBindViewHolder(p0: HomeViewHolder, p1: Int, p2: Picture) {
        p0.updateWithData(p2)
    }
}