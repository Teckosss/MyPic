package com.deguffroy.adrien.projetphoto.Views

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.CommentsPageFragment
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.PicturesPageFragment
import com.deguffroy.adrien.projetphoto.Utils.MODERATION_PICTURES

/**
 * Created by Adrien Deguffroy on 11/01/2019.
 */
class ModerationSlideAdapter(fm:FragmentManager, var documentId:ArrayList<String>, private var documentType:String) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        Log.e("ModerationAdapter","Document type = $documentType")
        return when(documentType){
            MODERATION_PICTURES -> PicturesPageFragment.newInstance(position, this.count, this.documentId[position])
            else -> CommentsPageFragment.newInstance(position, this.count, this.documentId[position])
        }
    }

    override fun getCount(): Int = documentId.size

    // THIS FUNCTION IS TRIGGER WHEN NOTIFY_DATA_SET_CHANGED IS CALLED
    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

    fun deletePage(position: Int){
        this.documentId.removeAt(position)
        notifyDataSetChanged()
    }
}