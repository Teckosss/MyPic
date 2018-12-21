package com.deguffroy.adrien.projetphoto.Views

import android.content.res.ColorStateList
import android.graphics.LightingColorFilter
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import kotlinx.android.synthetic.main.fragment_my_pic_item.view.*

/**
 * Created by Adrien Deguffroy on 26/11/2018.
 */
class MyPicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun updateWithData(data: Picture, isSelected:Boolean){
        val glide = Glide.with(itemView)

        val colorSelected = ContextCompat.getColor(itemView.context,R.color.colorPrimaryDark)
        val icon = itemView.resources.getDrawable(R.drawable.baseline_check_circle_outline_white_24)
        val filter = LightingColorFilter(colorSelected,colorSelected)
        glide.load(data.urlImage).apply(RequestOptions().centerCrop()).into(itemView.fragment_my_pic_item_image)

        if (isSelected){
            itemView.fragment_my_pic_background.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorBlackBackground))
            itemView.fragment_my_pic_constraint_layout.visibility = View.VISIBLE
            icon.colorFilter = filter
            itemView.fragment_my_pic_item_selected_icon.background = icon
        }else{
            itemView.fragment_my_pic_constraint_layout.visibility = View.GONE
            itemView.fragment_my_pic_background.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorTransparentBackground))
        }
    }
}