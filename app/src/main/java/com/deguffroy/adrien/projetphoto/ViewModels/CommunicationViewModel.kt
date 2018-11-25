package com.deguffroy.adrien.projetphoto.ViewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

/**
 * Created by Adrien Deguffroy on 25/11/2018.
 */
class CommunicationViewModel : ViewModel() {

    var currentUserPosition:MutableLiveData<LatLng> = MutableLiveData()

    fun updateCurrentUserPosition(latLng: LatLng){
        this.currentUserPosition.value = latLng
    }

    fun getCurrentUserPosition() = currentUserPosition.value
}