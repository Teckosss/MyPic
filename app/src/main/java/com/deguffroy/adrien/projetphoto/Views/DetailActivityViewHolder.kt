package com.deguffroy.adrien.projetphoto.Views

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Controllers.Activities.DetailActivity
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.Utils.toLocaleStringDateMedium
import kotlinx.android.synthetic.main.activity_detail_comment_item.view.*
import java.lang.ref.WeakReference

/**
 * Created by Adrien Deguffroy on 08/01/2019.
 */
class DetailActivityViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {

    private lateinit var callbackWeakReference: WeakReference<DetailActivityAdapter.Listener>
    private lateinit var comment: Comment

    fun updateWithData(comment: Comment, callback:DetailActivityAdapter.Listener){
        val glide = Glide.with(itemView.context)
        this.comment = comment

        if (!(comment.userSender.userPicture.isNullOrEmpty())){
            glide.load(comment.userSender.userPicture).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into(itemView.detail_activity_item_user_picture)
        }else{
            // DISPLAY NO IMAGE ICON
        }

        itemView.detail_activity_item_user_name.text = comment.userSender.username

        try {
            itemView.detail_activity_item_date.text = comment.dateCreated?.toDate()!!.toLocaleStringDateMedium()
        }catch (e:Exception){
            Log.e("DetailViewHolder", "Error : ${e.localizedMessage}")
        }

        itemView.detail_activity_item_comment_text.text = comment.commentText

        // CALLBACK WITH WEAK REFERENCE ON 3DOTS ICON
        itemView.detail_activity_item_icon_options.setOnClickListener { this.onClick() }
        this.callbackWeakReference = WeakReference(callback)
    }

    fun onClick(){
        val callback = callbackWeakReference.get()
        callback!!.onOptionsClickButton(this.comment)
    }
}