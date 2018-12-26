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
    private val mDimension: Int = 200
    private lateinit var picturesList:ArrayList<Drawable>

    init {
        val multiProfile = LayoutInflater.from(context).inflate(R.layout.multi_profile, null)
        mClusterIconGenerator.setContentView(multiProfile)
        mClusterImageView = (multiProfile.findViewById(R.id.multi_profile_image) as ImageView)
        mImageView = ImageView(context)
        mImageView.layoutParams = ViewGroup.LayoutParams(mDimension,mDimension)
        mImageView.setPadding(2,2,2,2)
        mIconGenerator.setContentView(mImageView)
    }

    override fun onBeforeClusterItemRendered(item: PictureCluster?, markerOptions: MarkerOptions?) {
        super.onBeforeClusterItemRendered(item, markerOptions)
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

    override fun onBeforeClusterRendered(cluster: Cluster<PictureCluster>?, markerOptions: MarkerOptions?) {
        super.onBeforeClusterRendered(cluster, markerOptions)

    }

    override fun onClusterRendered(cluster: Cluster<PictureCluster>?, marker: Marker?) {
        super.onClusterRendered(cluster, marker)
        picturesList = ArrayList(Math.min(4,cluster!!.size))
        var dummyBitmap : Bitmap? = null

        for (picture in cluster.items) {
            if (picturesList.size == 4) break
            try {
                Glide.with(context).load(picture.picture.urlImage).apply(RequestOptions().diskCacheStrategy(
                    DiskCacheStrategy.ALL).centerCrop())
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            resource.setBounds(0,0,mDimension,mDimension)
                            picturesList.add(resource)
                            val multiDrawable = MultiDrawable(picturesList)
                            multiDrawable.setBounds(0, 0, mDimension, mDimension)

                            mClusterImageView.setImageDrawable(multiDrawable)
                            val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
                            marker!!.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
                        }

                    })
            } catch (e: Exception) {
                Log.e("PictureRenderer", " Error : ${e.localizedMessage}")
            }
        }
        val icon = mClusterIconGenerator.makeIcon()
        marker!!.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    private fun addDrawableWhenRetrieved(bitmap: Bitmap, cluster: Cluster<PictureCluster>?, markerOptions: MarkerOptions?){
        val drawable = BitmapDrawable(context.resources,bitmap)
        drawable.setBounds(0,0,mDimension,mDimension)
        picturesList.add(drawable)
        if (picturesList.size == 4 || picturesList.size == cluster!!.size){
            drawMultiDrawable(cluster,markerOptions)
        }
    }

    private fun drawMultiDrawable(cluster: Cluster<PictureCluster>?, markerOptions: MarkerOptions?){
        Log.e("PictureRenderer","PicturesList : $picturesList")
        val multiDrawable = MultiDrawable(picturesList)
        multiDrawable.setBounds(0,0,mDimension,mDimension)

        mClusterImageView.setImageDrawable(multiDrawable)

        val icon = mClusterIconGenerator.makeIcon(cluster!!.size.toString())
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun setOnClusterItemClickListener(listener: ClusterManager.OnClusterItemClickListener<PictureCluster>?) {
        super.setOnClusterItemClickListener(listener)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<PictureCluster>?): Boolean {
        return cluster!!.size > 1
    }
}