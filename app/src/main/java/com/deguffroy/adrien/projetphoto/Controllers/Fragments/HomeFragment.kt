package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.DetailActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.MainActivity
import com.deguffroy.adrien.projetphoto.Models.Picture

import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.ItemClickSupport
import com.deguffroy.adrien.projetphoto.Views.HomePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*


/**
 * A simple [Fragment] subclass.
 *
 */
class HomeFragment : BaseFragment() {

    private lateinit var adapter:HomePagingAdapter
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
        this.configureOnClickItemRecyclerView()
        this.configureRecyclerView()
        this.retrievePicture()
    }

    override fun onResume() {
        super.onResume()
        this.configureRecyclerView()
        this.retrievePicture()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun configureRecyclerView(){
        this.listPictures = ArrayList()
        this.adapter = HomePagingAdapter(generateOptionsForAdapter(PicturesHelper().getAllPublicAndVerifiedPictures()))
        fragment_home_recycler_view.adapter = adapter
        fragment_home_recycler_view.layoutManager = LinearLayoutManager(activity)
    }

    private fun generateOptionsForAdapter(query: Query) = FirestorePagingOptions.Builder<Picture>()
        .setQuery(query, generateConfig() ,Picture::class.java)
        .setLifecycleOwner(this)
        .build()

    private fun generateConfig() = PagedList.Config.Builder()
        .setPrefetchDistance(10)
        .setPageSize(20)
        .build()

    private fun configureRefreshListener(){
        fragment_home_swipe_container.setOnRefreshListener {
            fragment_home_swipe_container.isRefreshing = true
            this.retrievePicture()
        }
    }

    private fun configureOnClickItemRecyclerView(){
        ItemClickSupport.addTo(fragment_home_recycler_view, R.layout.fragment_home_item)
            .setOnItemClickListener { recyclerView, position, v ->
                startActivity(DetailActivity.newInstance(activity!!, adapter.getItemAtPosition(position)?.documentId))
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
            (activity as MainActivity).showSnackbarMessage(home_fragment_coordinator,resources.getString(R.string.home_fragment_error_retrieving_data), Snackbar.LENGTH_LONG, main_activity_fab)
        }
    }


    // -------------------
    // UI
    // -------------------

    private fun updateUI(listPic:ArrayList<Picture>){
        listPictures = ArrayList()
        activity?.runOnUiThread{
            if (fragment_home_swipe_container != null) fragment_home_swipe_container.isRefreshing = false
        }
        listPictures.addAll(listPic)
        adapter.notifyDataSetChanged()
    }
}
