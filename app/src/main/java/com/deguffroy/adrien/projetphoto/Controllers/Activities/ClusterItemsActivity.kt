package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.ItemClickSupport
import com.deguffroy.adrien.projetphoto.Views.ClusterItemsAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_cluster.*
import kotlin.collections.ArrayList

/**
 * Created by Adrien Deguffroy on 22/01/2019.
 */
class ClusterItemsActivity : BaseActivity() {

    private lateinit var listPictures:ArrayList<Picture>
    private lateinit var adapter:ClusterItemsAdapter

    companion object {
        private const val PICTURES_OBJECT = "PICTURES_OBJECT"
        fun newInstance(context: Context, listPictures:String) = Intent(context, ClusterItemsActivity::class.java).apply {
            putExtra(PICTURES_OBJECT, listPictures)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cluster)

        this.retrieveDataFromJson()
        this.configureRecyclerView()
        this.configureOnClickRecyclerView()
    }

    private fun retrieveDataFromJson(){
        val gson = Gson()
        val arrayAsString = intent.getStringExtra(PICTURES_OBJECT)
        val listType = object : TypeToken<List<Picture>>() {}.type
        this.listPictures = gson.fromJson(arrayAsString, listType)
    }

    private fun configureRecyclerView(){
        this.adapter = ClusterItemsAdapter(listPictures)
        activity_cluster_recycler.layoutManager = GridLayoutManager(this,3)
        activity_cluster_recycler.adapter = this.adapter
    }

    private fun configureOnClickRecyclerView(){
        ItemClickSupport
            .addTo(activity_cluster_recycler, R.layout.activity_cluster_item)
            .setOnItemClickListener { _, position, _ ->
                startActivity(DetailActivity.newInstance(this , adapter.getItemAtPosition(position).documentId))
        }
    }
}