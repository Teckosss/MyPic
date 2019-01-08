package com.deguffroy.adrien.projetphoto.Views

import android.view.LayoutInflater
import android.view.ViewGroup
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.util.zip.Inflater

/**
 * Created by Adrien Deguffroy on 08/01/2019.
 */
class DetailActivityAdapter(options: FirestoreRecyclerOptions<Comment>) :
    FirestoreRecyclerAdapter<Comment, DetailActivityViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailActivityViewHolder {
        return DetailActivityViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_detail_comment_item, parent, false))
    }

    override fun onBindViewHolder(p0: DetailActivityViewHolder, p1: Int, p2: Comment) {
        p0.updateWithData(p2)
    }
}