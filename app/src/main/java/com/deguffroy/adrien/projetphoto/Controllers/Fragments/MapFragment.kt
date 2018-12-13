package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity
import com.deguffroy.adrien.projetphoto.Models.ModelTest
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.PictureCluster
import com.deguffroy.adrien.projetphoto.Models.User

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.Constants
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_map.*


/**
 * A simple [Fragment] subclass.
 *
 */
class MapFragment : BaseFragment() {

    private var mMap: GoogleMap? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map_fragment.onCreate(savedInstanceState)
        map_fragment.onResume()

        /*if (this.locationPermissionsGranted()) {
            // Permission is granted
            if (mMap == null) this.configureMap()
        }else{
            // Permission is not granted
            Log.e("MapFragment","Permissions not granted!")
            this.requestLocationPermissions()
        }*/
    }

    override fun configureLocationFunctions(hasPermissions: Boolean) {
        super.configureLocationFunctions(hasPermissions)
        if (mMap == null) this.configureMap(hasPermissions)
    }

    override fun onResume() {
        super.onResume()
        map_fragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_fragment.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_fragment.onLowMemory()
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    private fun configureMap(hasPermissions: Boolean) {
        try {
            MapsInitializer.initialize(activity!!.baseContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        map_fragment.getMapAsync {
            this.loadMap(it, hasPermissions)
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
            //it.setOnMarkerClickListener { Log.e("MAP_FRAGMENT","Click on $it");true }

            if (hasPermissions){
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
        //mClusterManager.clearItems()
        mClusterManager.onCameraIdle()
        mMap!!.setOnMarkerClickListener(mClusterManager)
        this.addItemsToCluster()

    }

    private fun addItemsToCluster(){
        PicturesHelper().getAllPictures().addOnCompleteListener {
            if (it.isSuccessful){
                val listPicture = arrayListOf<PictureCluster>()
                for (document in it.result!!){
                    val picture = document.toObject(Picture::class.java)
                    //Log.e("MapFragment","Picture : $picture")
                    val pictureCluster = PictureCluster(picture,LatLng(picture.l[0], picture.l[1]))
                    Log.e("AddItemsToCluster","Picture to cluster : $pictureCluster")
                    listPicture.add(pictureCluster)
                }
                updateUI(listPicture)
            }
        }
    }

    private fun updateUI(pictureToAdd:ArrayList<PictureCluster>){
        //Log.e("ProcessData","List size : ${list.size}")
        mClusterManager.addItems(pictureToAdd)
        mClusterManager.cluster()
    }

    override fun handleNewLocation(location: Location){
        Log.e("Handle_New_Location","New location : ${LatLng(location.latitude,location.longitude)}")
        this.moveCameraOnMap(LatLng(location.latitude,location.longitude))
        this.stopLocationUpdate()
    }

    private fun moveCameraOnMap(latLng: LatLng){
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.MAP_FRAGMENT_DEFAULT_ZOOM))
    }
}
