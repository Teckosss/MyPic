package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.PictureCluster

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.Constants
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager

/**
 * A simple [Fragment] subclass.
 *
 */
class MapFragment : BaseFragment() {

    private var mMap: GoogleMap? = null

    private lateinit var mapFragment:SupportMapFragment

    private lateinit var mClusterManager:ClusterManager<PictureCluster>

    companion object {
        fun newInstance():MapFragment{
            return MapFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun configureLocationFunctions(hasPermissions: Boolean) {
        super.configureLocationFunctions(hasPermissions)
        if (mMap == null) this.configureMap(hasPermissions)
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    private fun configureMap(hasPermissions: Boolean) {
        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync{
            this.loadMap(it, hasPermissions) // WHEN MAP IS READY
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadMap(googleMap: GoogleMap?, hasPermissions: Boolean){
        if (googleMap != null){
            Log.e("MapFragment","loadMap!")
            mMap = googleMap
            mMap!!.isIndoorEnabled = false
            mMap!!.uiSettings.isCompassEnabled = true
            mMap!!.uiSettings.isMyLocationButtonEnabled = true
            mMap!!.uiSettings.isRotateGesturesEnabled = true

            if (hasPermissions){ // IF USER GRANT PERMISSION TO KNOW HIS LOCATION
                mMap!!.isMyLocationEnabled = true
                if(mViewModel.getCurrentUserPosition() != null){
                    this.moveCameraOnMap(mViewModel.getCurrentUserPosition()!!)
                }else{
                    this.forceRequestLocation()
                }
            }
            this.setUpCluster()
        }
    }

    private fun setUpCluster(){
        Log.e("MapFragment","SetUp Cluster!")
        mClusterManager = ClusterManager(activity!!, mMap)
        mClusterManager.clearItems()
        mMap!!.setOnCameraIdleListener(mClusterManager)
        mMap!!.setOnMarkerClickListener(mClusterManager)
        this.addItemsToCluster()

    }

    private fun addItemsToCluster(){
        PicturesHelper().getAllPictures().addOnCompleteListener {
            if (it.isSuccessful){
                val listPicture = arrayListOf<PictureCluster>()
                for (document in it.result!!){ // ADDING EVERY PIC FROM FIRESTORE TO LIST
                    val picture = document.toObject(Picture::class.java)
                    val pictureCluster = PictureCluster(picture,LatLng(picture.l[0], picture.l[1]))
                    listPicture.add(pictureCluster)
                }
                this.updateUI(listPicture)
            }
        }
    }

    override fun handleNewLocation(location: Location){
        Log.e("Handle_New_Location","New location : ${LatLng(location.latitude,location.longitude)}")
        this.moveCameraOnMap(LatLng(location.latitude,location.longitude))
        this.stopLocationUpdate()
    }

    // -----------------
    // UI
    // -----------------

    private fun updateUI(pictureToAdd:ArrayList<PictureCluster>){
        mClusterManager.addItems(pictureToAdd)
        mClusterManager.cluster()
    }

    private fun moveCameraOnMap(latLng: LatLng){
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.MAP_FRAGMENT_DEFAULT_ZOOM))
    }
}
