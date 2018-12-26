package com.deguffroy.adrien.projetphoto.Views

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Utils.toLocaleStringDate
import kotlinx.android.synthetic.main.fragment_home_item.view.*

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun updateWithData(picture:Picture){
        val glide = Glide.with(itemView.context)
        if (!(picture.userSender.userPicture.isNullOrBlank())){
            glide.load(picture.userSender.userPicture)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop())
                .into(itemView.home_user_profile_picture)
        }else{
            itemView.home_user_profile_picture.visibility = View.GONE
        }

        glide.load(picture.urlImage).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()).into(itemView.home_item_image_view)

        itemView.home_item_author.text = picture.userSender.username
        itemView.home_item_date.text = picture.dateCreated!!.toDate().toLocaleStringDate()
    }
}