package com.deguffroy.adrien.projetphoto.Api

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.IdRes
import com.deguffroy.adrien.projetphoto.R
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Created by Adrien Deguffroy on 04/01/2019.
 */
class BottomNavHelper {
    fun showBadge(context: Context,bottomNavigationView: BottomNavigationView, @IdRes itemId:Int){
        val itemView = bottomNavigationView.findViewById<BottomNavigationItemView>(itemId)
        val badge = LayoutInflater.from(context).inflate(R.layout.layout_bnv_badge, bottomNavigationView, false)
        itemView.addView(badge)
    }

    fun removeBadge(bottomNavigationView: BottomNavigationView, @IdRes itemId: Int){
        val itemView = bottomNavigationView.findViewById<BottomNavigationItemView>(itemId)
        if (itemView.childCount == 3){
            itemView.removeViewAt(2)
        }
    }
}