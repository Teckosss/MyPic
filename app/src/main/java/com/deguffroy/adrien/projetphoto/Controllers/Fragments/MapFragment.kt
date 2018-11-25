package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.Constants
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_map.*


/**
 * A simple [Fragment] subclass.
 *
 */
class MapFragment : BaseFragment() {

    private var mMap: GoogleMap? = null

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

        if (this.locationPermissionsGranted()) {
            // Permission is granted
            this.configureMap()
        }else{
            // Permission is not granted
            Log.e("MapFragment","Permissions not granted!")
            this.requestLocationPermissions()
        }
    }

    override fun configureLocationFunctions() {
        super.configureLocationFunctions()
        this.configureMap()
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

    @SuppressLint("MissingPermission")
    private fun configureMap() {

        try {
            MapsInitializer.initialize(activity!!.baseContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        map_fragment.getMapAsync {
            mMap = it
            it.isIndoorEnabled = false
            it.isMyLocationEnabled = true
            it.uiSettings.isCompassEnabled = true
            it.uiSettings.isMyLocationButtonEnabled = true
            it.uiSettings.isRotateGesturesEnabled = true
            it.setOnMarkerClickListener { Log.e("MAP_FRAGMENT","Click on $it");true }

            this.moveCameraOnMap(mViewModel.getCurrentUserPosition()!!)
        }
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
