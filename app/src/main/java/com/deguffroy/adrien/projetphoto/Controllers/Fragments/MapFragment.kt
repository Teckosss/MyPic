package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.ClusterRenderer.PictureRenderer
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.ClusterItemsActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.DetailActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.MainActivity
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.PictureCluster

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.MAP_FRAGMENT_DEFAULT_ZOOM
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * A simple [Fragment] subclass.
 *
 */
class MapFragment : BaseFragment() , ClusterManager.OnClusterItemClickListener<PictureCluster>, ClusterManager.OnClusterClickListener<PictureCluster>{

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
        mClusterManager.renderer = PictureRenderer(activity!!, mMap!!, mClusterManager)
        mMap!!.setOnCameraIdleListener(mClusterManager)
        mMap!!.setOnMarkerClickListener(mClusterManager)
        mClusterManager.setOnClusterClickListener(this)
        mClusterManager.setOnClusterItemClickListener(this)
        this.addItemsToCluster()

    }

    private fun addItemsToCluster(){
        PicturesHelper().getAllPicturesWithGeoLocAndPublicVerified().addOnCompleteListener {
            Log.e("MapFragment","Result's number : ${it.result?.size()}")
            if (it.isSuccessful){
                val listPicture = arrayListOf<PictureCluster>()
                for (document in it.result!!){ // ADDING EVERY PIC FROM FIRESTORE TO LIST
                    val picture = document.toObject(Picture::class.java)
                    val pictureCluster = PictureCluster(picture,LatLng(picture.l[0], picture.l[1]))
                    listPicture.add(pictureCluster)
                }
                this.updateUI(listPicture)
            }else{
               BaseActivity().showSnackbarMessage(coordinator_layout_map_fragment,
                   resources.getString(R.string.map_fragment_no_result_message),
                   Snackbar.LENGTH_LONG,
                   (activity!! as MainActivity).main_activity_fab)
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
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_FRAGMENT_DEFAULT_ZOOM))
    }

    // -----------------
    // ACTION
    // -----------------

    override fun onClusterItemClick(p0: PictureCluster?): Boolean {
        Log.e("MapFragment","Click on item : ${p0?.picture}")
        startActivity(DetailActivity.newInstance(activity!!, p0?.picture?.documentId))
        return true
    }

    override fun onClusterClick(p0: Cluster<PictureCluster>?): Boolean {
        Log.e("MapFragment","Click on cluster : ${p0?.items}")
        val listPicture = ArrayList<Picture>()
        for (item in p0?.items!!){
            val picture = item.picture
            listPicture.add(picture)
        }
        val gson = Gson()
        val listClusterAsString = gson.toJson(listPicture)
        startActivity(ClusterItemsActivity.newInstance(activity!!, listClusterAsString))
        return true
    }
}
