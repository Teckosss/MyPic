package com.deguffroy.adrien.projetphoto.Views

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Models.Picture
import kotlinx.android.synthetic.main.fragment_my_pic_item.view.*

/**
 * Created by Adrien Deguffroy on 26/11/2018.
 */
class MyPicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun updateWithData(data: Picture){
        val glide = Glide.with(itemView)

        glide.load(data.urlImage).apply(RequestOptions().centerCrop()).into(itemView.fragment_my_pic_item_image)
    }
}