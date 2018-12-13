package com.deguffroy.adrien.projetphoto.Models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * Created by Adrien Deguffroy on 01/12/2018.
 */
class ModelTest(var titleItem:String,var latLng: LatLng) : ClusterItem{

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return latLng
    }
}