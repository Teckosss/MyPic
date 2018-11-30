package com.deguffroy.adrien.projetphoto.ViewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.User
import com.google.android.gms.maps.model.LatLng
import java.net.URI

/**
 * Created by Adrien Deguffroy on 25/11/2018.
 */
class CommunicationViewModel : ViewModel() {

    var currentUserPosition:MutableLiveData<LatLng> = MutableLiveData()
    var currentUserUID:MutableLiveData<String> = MutableLiveData()

    var currentListImagesToDelete = arrayListOf<Picture>()
    var myPicSelectingMode = false

    fun updateCurrentUserPosition(latLng: LatLng){
        this.currentUserPosition.value = latLng
    }

    fun getCurrentUserPosition() = currentUserPosition.value

    fun updateCurrentUserUID(uid:String){
        this.currentUserUID.value = uid
    }

    fun getCurrentUserUID() = currentUserUID.value
}