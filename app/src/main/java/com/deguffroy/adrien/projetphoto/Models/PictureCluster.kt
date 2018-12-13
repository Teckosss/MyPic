package com.deguffroy.adrien.projetphoto.Models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * Created by Adrien Deguffroy on 01/12/2018.
 */
data class PictureCluster(var picture:Picture, var latLng: LatLng) : ClusterItem {

    override fun getSnippet(): String {
        return "Snippet"
    }

    override fun getTitle(): String {
        return "Title"
    }

    override fun getPosition() = latLng
}