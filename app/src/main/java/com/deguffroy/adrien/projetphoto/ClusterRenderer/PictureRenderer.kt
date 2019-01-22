package com.deguffroy.adrien.projetphoto.ClusterRenderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.Models.PictureCluster
import com.deguffroy.adrien.projetphoto.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ui.IconGenerator
import java.lang.Exception


/**
 * Created by Adrien Deguffroy on 21/12/2018.
 */
class PictureRenderer(private val context: Context, googleMap: GoogleMap, clusterManager: ClusterManager<PictureCluster>) : DefaultClusterRenderer<PictureCluster>(context, googleMap, clusterManager){

    private val mIconGenerator = IconGenerator(context)
    private val mClusterIconGenerator = IconGenerator(context)
    private var mImageView: ImageView
    private var mClusterImageView: ImageView
    private val mDimension: Int = (context.resources.getDimension(R.dimen.map_image_size)).toInt()
    private lateinit var picturesList:ArrayList<Drawable>

    init {
        val multiProfile = LayoutInflater.from(context).inflate(R.layout.multi_profile, null)
        mClusterIconGenerator.setContentView(multiProfile)
        mClusterImageView = (multiProfile.findViewById(R.id.multi_profile_image) as ImageView)
        mImageView = ImageView(context)
        mImageView.layoutParams = ViewGroup.LayoutParams(mDimension,mDimension)
        //mImageView.setPadding(2,2,2,2)
        mImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        mIconGenerator.setContentView(mImageView)
    }

    override fun onBeforeClusterItemRendered(item: PictureCluster?, markerOptions: MarkerOptions?) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        mImageView.setImageResource(0)
        mImageView.setImageDrawable(null)
        val icon = mIconGenerator.makeIcon()
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item!!.picture.description)
    }

    override fun onClusterItemRendered(clusterItem: PictureCluster?, marker: Marker?) {
        super.onClusterItemRendered(clusterItem, marker)
        Glide.with(context).load(clusterItem!!.picture.urlImage).apply(RequestOptions().diskCacheStrategy(
            DiskCacheStrategy.ALL).centerCrop())
            .into(object : SimpleTarget<Drawable>(){
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    mImageView.setImageDrawable(resource)
                    val icon = mIconGenerator.makeIcon()
                    marker!!.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
                }
            })
    }



    override fun shouldRenderAsCluster(cluster: Cluster<PictureCluster>?): Boolean {
        return cluster!!.size > 1
    }
}