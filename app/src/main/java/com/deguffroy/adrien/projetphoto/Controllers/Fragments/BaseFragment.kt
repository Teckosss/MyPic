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
import com.deguffroy.adrien.projetphoto.Utils.RC_PERM_LOCATION
import com.deguffroy.adrien.projetphoto.ViewModels.CommunicationViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener
import java.lang.ClassCastException
import java.lang.Exception
import java.lang.NullPointerException

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */
open class BaseFragment : Fragment(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, GeoQueryDataEventListener {

    lateinit var mViewModel: CommunicationViewModel

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mLocationRequest : LocationRequest
    private lateinit var mLocationCallback : LocationCallback

    private val picturesList = arrayListOf<String>()

    private lateinit var geoFirestore: GeoFirestore
    private lateinit var geoQuery: GeoQuery

    var mGoogleApiClient: GoogleApiClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProviders.of(activity!!).get(CommunicationViewModel::class.java)

        if (this is DetailFragment){
            (activity as MainActivity).hideFab()
        }else{
            (activity as MainActivity).showFab()
        }

        this.initDb("pictures")

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
        Log.e("BaseFragment","ENTER configureLocationFunctions, permissions value = $hasPermissions")
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

    fun locationPermissionsGranted(): Boolean = (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)

    fun requestLocationPermissions(){
        Log.e("BaseFragment","Request location permissions")
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
            RC_PERM_LOCATION)
    }

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

    fun getGeoFirestoreData(center:GeoPoint, radius:Double){
        this.geoQuery = geoFirestore.queryAtLocation(center,radius)
        this.geoQuery.removeAllListeners()
        this.geoQuery.addGeoQueryDataEventListener(this)
    }

    override fun onGeoQueryReady() {
        Log.e("onGeoQueryReady","Enter, list size : ${picturesList.size}")
        this.geoQuery.removeGeoQueryEventListener(this)

        (0 until picturesList.size).forEach{
            Log.e("onGeoQueryReady","Data : ${picturesList[it]}")
        }
    }

    override fun onDocumentExited(p0: DocumentSnapshot?) {
        Log.e("onDocumentExited","Enter")
    }

    override fun onDocumentChanged(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentChanged","Enter")
    }

    override fun onDocumentEntered(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentEntered","Enter, data : ${p0?.data}")
        try {
            val data:Map<String,Any>? = p0?.data
            val description:String = data?.get("desc") as String
            if (description != null) picturesList.add(description)
        }catch (e: NullPointerException){
            Log.e("DocumentEntered", " Error : ${e.localizedMessage}")
        }catch (e: ClassCastException){
            Log.e("DocumentEntered", " Error : ${e.localizedMessage}")
        }
    }

    override fun onDocumentMoved(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentMoved","Enter")
    }

    override fun onGeoQueryError(p0: Exception?) {
        Log.e("GEO_QUERY","Error : ${p0?.localizedMessage}")
    }
}