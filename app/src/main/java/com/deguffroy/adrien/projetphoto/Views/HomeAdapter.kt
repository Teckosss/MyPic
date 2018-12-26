package com.deguffroy.adrien.projetphoto.Views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
class HomeAdapter(private val listPictures:ArrayList<Picture>) : RecyclerView.Adapter<HomeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
       return HomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_home_item,parent,false))
    }

    override fun getItemCount(): Int = listPictures.size

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.updateWithData(listPictures[position])
    }
}