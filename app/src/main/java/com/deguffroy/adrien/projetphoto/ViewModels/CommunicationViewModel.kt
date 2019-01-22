package com.deguffroy.adrien.projetphoto.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.google.android.gms.maps.model.LatLng

/**
 * Created by Adrien Deguffroy on 25/11/2018.
 */
class CommunicationViewModel : ViewModel() {

    var currentPicture:MutableLiveData<Picture> = MutableLiveData()

    var currentUserPosition:MutableLiveData<LatLng> = MutableLiveData()
    var currentUserUID:MutableLiveData<String> = MutableLiveData()

    var currentListImagesToDelete = arrayListOf<Picture>()
    var currentListCommentToDelete = arrayListOf<Comment>()
    var myPicSelectingMode = false

    var ListImageToCluster = arrayListOf<Picture>()

    fun updateCurrentUserPosition(latLng: LatLng){
        this.currentUserPosition.value = latLng
    }

    fun getCurrentUserPosition() = currentUserPosition.value

    fun updateCurrentUserUID(uid:String){
        this.currentUserUID.value = uid
    }

    fun getCurrentUserUID() = currentUserUID.value

    fun getPicture(pictureId:String):LiveData<Picture>{
        if (currentPicture.value == null){
            PicturesHelper().getPicturesCollection().document(pictureId).addSnapshotListener { p0, p1 ->
                if (p0?.exists()!!){
                    val picture = p0.toObject(Picture::class.java)
                    currentPicture.postValue(picture)
                }else{
                    Log.e("ViewModel","SnapshotListener failed! ${p1?.localizedMessage}")
                }
            }
        }
        return currentPicture
    }
}