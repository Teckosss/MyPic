package com.deguffroy.adrien.projetphoto.Views

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Models.Picture
import kotlinx.android.synthetic.main.activity_cluster_item.view.*

/**
 * Created by Adrien Deguffroy on 22/01/2019.
 */
class ClusterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun updateWithData(picture:Picture){
        val glide = Glide.with(itemView.context)
        glide.load(picture.urlImage).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()).into(itemView.activity_cluster_item_image)
    }
}