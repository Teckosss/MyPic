package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.deguffroy.adrien.projetphoto.Controllers.Activities.MainActivity
import com.deguffroy.adrien.projetphoto.Utils.GEO_FIRESTORE_COLLECTION_NAME
import com.deguffroy.adrien.projetphoto.Utils.RC_PERM_LOCATION
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import org.imperiumlabs.geofirestore.GeoFirestore

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
open class BaseFragment : Fragment(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    lateinit var mViewModel: CommunicationViewModel

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mLocationRequest : LocationRequest
    private lateinit var mLocationCallback : LocationCallback

    lateinit var geoFirestore: GeoFirestore

    var mGoogleApiClient: GoogleApiClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProviders.of(activity!!).get(CommunicationViewModel::class.java)

        if (this is ModerationFragment || this is ProfileFragment){
            (activity as MainActivity).hideFab()
        }else{
            (activity as MainActivity).showFab()
        }

        this.initDb(GEO_FIRESTORE_COLLECTION_NAME)

        if (locationPermissionsGranted()){
            // Permission is granted
            this.configureLocationFunctions(true)
        }else{
            // Permission is not granted
            this.requestLocationPermissions()
        }
    }

    override fun onStart() {
        super.onStart()
        if(mGoogleApiClient != null){
            if(!mGoogleApiClient?.isConnected!! && !mGoogleApiClient?.isConnecting!!){
                mGoogleApiClient?.connect()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient != null) mGoogleApiClient?.connect()
    }

    override fun onPause() {
        super.onPause()
        this.stopLocationUpdateAndDisconnectGoogleApiClient()
    }

    override fun onStop() {
        super.onStop()
        this.stopLocationUpdateAndDisconnectGoogleApiClient()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.stopLocationUpdateAndDisconnectGoogleApiClient()
    }

    // -------------------
    // LOCATION
    // -------------------

    open fun configureLocationFunctions(hasPermissions:Boolean){
        Log.i("BaseFragment","ENTER configureLocationFunctions, permissions value = $hasPermissions")
        if (hasPermissions){
            this.configureLocationRequest()
            this.configureLocationCallback()
            this.configureGoogleApiClient()
        }
    }

    private fun stopLocationUpdateAndDisconnectGoogleApiClient(){
        if (mGoogleApiClient != null){
            this.stopLocationUpdate()
            mGoogleApiClient?.stopAutoManage(activity!!)
            mGoogleApiClient?.disconnect()
        }
    }

    // Request device position every 10s
    private fun configureLocationRequest(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(100 * 1000)
            .setFastestInterval(1000)
    }

    private fun configureGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient
            .Builder(this.context!!)
            .addConnectionCallbacks(this)
            .addApi(LocationServices.API)
            .enableAutoManage(activity!!,this)
            .build()
    }

    // If we find location we trigger HandleNewLocation with this new location
    private fun configureLocationCallback(){
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    handleNewLocation(location)
                }
            }
        }
    }

    // Force request device location
    @SuppressLint("MissingPermission")
    fun forceRequestLocation(){
        mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,mLocationCallback,null)
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        if (mFusedLocationClient != null){
            mFusedLocationClient!!.lastLocation.addOnSuccessListener {
                if (it != null){
                    mViewModel.updateCurrentUserPosition(LatLng(it.latitude,it.longitude))
                }else{
                    mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,mLocationCallback,null)
                }
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {}

    // Stop requesting device position
    fun stopLocationUpdate(){
        if (mFusedLocationClient != null){
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) { Log.e("MAP_FRAGMENT","Connection result : $p0") }

    override fun onLocationChanged(p0: Location?) { this.handleNewLocation(p0!!) }

    open fun handleNewLocation(location: Location){
        mViewModel.updateCurrentUserPosition(LatLng(location.latitude,location.longitude))
    }

    // -------------------
    // PERMISSIONS
    // -------------------

    // Check if location permissions are granted
    private fun locationPermissionsGranted(): Boolean = (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)

    // Request location permissions
    private fun requestLocationPermissions(){
        Log.i("BaseFragment","Request location permissions")
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
            RC_PERM_LOCATION)
    }

    // Handle permissions request's result
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RC_PERM_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    this.configureLocationFunctions(true)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    this.configureLocationFunctions(false)
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    // --------------------
    // GEO FIRESTORE
    // --------------------

    private fun initDb(collection:String){
        val db = FirebaseFirestore.getInstance()
        val picturesCollection = db.collection(collection)
        this.geoFirestore = GeoFirestore(picturesCollection)
    }
}