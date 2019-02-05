package com.deguffroy.adrien.projetphoto.Api

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.IdRes
import com.deguffroy.adrien.projetphoto.R
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
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

    fun removeBadge(bottomNavigationView: BottomNavigationView){
        val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val itemView = menuView.getChildAt(4) as BottomNavigationItemView
        var retryCount = 0

        while (itemView.getChildAt(2) != null && retryCount <= 10){
            try {
                itemView.removeView(itemView.getChildAt(2))
            }catch (e:Exception){
                retryCount++
                Log.e("BottomNavHelper","Error : ${e.localizedMessage}")
            }
        }
    }
}