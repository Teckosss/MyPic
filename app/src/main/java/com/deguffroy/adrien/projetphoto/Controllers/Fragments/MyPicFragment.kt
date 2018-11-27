package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Views.MyPicAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_my_pic.*
import java.lang.IllegalStateException


/**
 * A simple [Fragment] subclass.
 *
 */
class MyPicFragment : BaseFragment(), MyPicAdapter.Listener{

    private lateinit var listImages:ArrayList<String>
    private lateinit var adapter:MyPicAdapter

    companion object {
        fun newInstance():MyPicFragment{
            return MyPicFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_pic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.configureRecyclerView()
        //this.retrieveUserData()
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun configureRecyclerView(){
        this.listImages = ArrayList()
        this.adapter = MyPicAdapter(this, generateOptionsForAdapter(PicturesHelper().getAllPicturesFromUser(mViewModel.getCurrentUserUID()!!)))
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
            override fun onChanged() {
                super.onChanged()

            }
        })
        this.my_pic_recycler_view.adapter = this.adapter
        this.my_pic_recycler_view.layoutManager = GridLayoutManager(activity!!,3)
    }

    private fun generateOptionsForAdapter(query: Query) : FirestoreRecyclerOptions<Picture> = FirestoreRecyclerOptions.Builder<Picture>()
        .setQuery(query, Picture::class.java)
        .setLifecycleOwner(activity)
        .build()

    override fun onDataChanged() {
        try{
            if (adapter.itemCount == 0){
                my_pic_recycler_view.visibility = View.GONE
                my_pic_no_image_yet_layout.visibility = View.VISIBLE
            } else {
                my_pic_recycler_view.visibility = View.VISIBLE
                my_pic_no_image_yet_layout.visibility = View.GONE
            }
        }catch (e:IllegalStateException){
            Log.e("MycPicFragment","OnDataChanged : ${e.localizedMessage}")
        }
    }
}
