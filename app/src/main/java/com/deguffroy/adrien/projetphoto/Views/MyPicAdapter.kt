package com.deguffroy.adrien.projetphoto.Views

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

/**
 * Created by Adrien Deguffroy on 26/11/2018.
 */
class MyPicAdapter(var callback:Listener, @NonNull options: FirestoreRecyclerOptions<Picture>) : FirestoreRecyclerAdapter<Picture, MyPicViewHolder>(options) {

    interface Listener{
        fun onDataChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPicViewHolder {
        return MyPicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_my_pic_item, parent,false))
    }

    override fun onBindViewHolder(p0: MyPicViewHolder, p1: Int, @NonNull p2: Picture) {
        p0.updateWithData(p2)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        this.callback.onDataChanged()
    }
}