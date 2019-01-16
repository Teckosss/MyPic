package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.DetailActivity
import com.deguffroy.adrien.projetphoto.Models.Picture

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.ItemClickSupport
import com.deguffroy.adrien.projetphoto.Views.HomeAdapter
import kotlinx.android.synthetic.main.fragment_home.*


/**
 * A simple [Fragment] subclass.
 *
 */
class HomeFragment : BaseFragment() {

    private lateinit var adapter:HomeAdapter
    private lateinit var listPictures:ArrayList<Picture>

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_home_swipe_container.isRefreshing = true
        this.configureRefreshListener()
        this.configureRecyclerView()
        this.configureOnClickItemRecyclerView()
        this.retrievePicture()
    }

    override fun onResume() {
        super.onResume()
        this.retrievePicture()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun configureRecyclerView(){
        this.listPictures = ArrayList()
        this.adapter = HomeAdapter(this.listPictures)
        fragment_home_recycler_view.adapter = adapter
        fragment_home_recycler_view.layoutManager = LinearLayoutManager(activity)
    }

    private fun configureRefreshListener(){
        fragment_home_swipe_container.setOnRefreshListener {
            fragment_home_swipe_container.isRefreshing = true
            this.retrievePicture()
        }
    }

    private fun configureOnClickItemRecyclerView(){
        ItemClickSupport.addTo(fragment_home_recycler_view, R.layout.fragment_home_item)
            .setOnItemClickListener { recyclerView, position, v ->
                startActivity(DetailActivity.newInstance(activity!!, adapter.getItemAtPosition(position).documentId))
            }
    }

    private fun retrievePicture(){
        Log.e("HomeFragment","Enter RetrievePicture!")
        PicturesHelper().getAllPublicAndVerifiedPictures().get().addOnCompleteListener {
            if (it.isSuccessful){
                val listToAdd = arrayListOf<Picture>()
                for (document in it.result!!){
                    val picture = document.toObject(Picture::class.java)
                    listToAdd.add(picture)
                }
                this.updateUI(listToAdd)
            }else{
                Log.e("HomeFragment","No result!")
            }
        }.addOnFailureListener {
            Log.e("HomeFragment", it.localizedMessage)
        }
    }

    // -------------------
    // UI
    // -------------------

    private fun updateUI(listPic:ArrayList<Picture>){
        activity?.runOnUiThread{
            if (fragment_home_swipe_container != null) fragment_home_swipe_container.isRefreshing = false
        }
        listPictures.clear()
        listPictures.addAll(listPic)
        adapter.notifyDataSetChanged()
    }

}
