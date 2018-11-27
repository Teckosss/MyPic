package com.deguffroy.adrien.projetphoto.ViewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deguffroy.adrien.projetphoto.Models.User
import com.google.android.gms.maps.model.LatLng
import java.net.URI

/**
 * Created by Adrien Deguffroy on 25/11/2018.
 */
class CommunicationViewModel : ViewModel() {

    var currentUserPosition:MutableLiveData<LatLng> = MutableLiveData()
    var currentUserUID:MutableLiveData<String> = MutableLiveData()
    var currentPhotoURI: MutableLiveData<Uri> = MutableLiveData()
    var currentModelUser: MutableLiveData<User> = MutableLiveData()

    fun updateCurrentUserPosition(latLng: LatLng){
        this.currentUserPosition.value = latLng
    }

    fun getCurrentUserPosition() = currentUserPosition.value

    fun updateCurrentUserUID(uid:String){
        this.currentUserUID.value = uid
    }

    fun getCurrentUserUID() = currentUserUID.value

    fun updateCurrentPhotoURI(uri:Uri){
        this.currentPhotoURI.value = uri
        Log.e("ViewModel","¨PhotoURI : ${this.currentPhotoURI.value}")
    }

    fun getCurrentPhotoURI() = this.currentPhotoURI.value

    fun updateCurrentModelUser(user: User){
        this.currentModelUser.value = user
    }

    fun getCurrentModelUser() = this.currentModelUser.value
}