package com.deguffroy.adrien.projetphoto.Views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R

/**
 * Created by Adrien Deguffroy on 22/01/2019.
 */
class ClusterItemsAdapter(private var listPictures:ArrayList<Picture>) : RecyclerView.Adapter<ClusterItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClusterItemViewHolder =
        ClusterItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_cluster_item, parent, false))

    override fun getItemCount(): Int = listPictures.size

    fun getItemAtPosition(position: Int) = listPictures[position]

    override fun onBindViewHolder(holder: ClusterItemViewHolder, position: Int) {
        holder.updateWithData(listPictures[position])
    }
}