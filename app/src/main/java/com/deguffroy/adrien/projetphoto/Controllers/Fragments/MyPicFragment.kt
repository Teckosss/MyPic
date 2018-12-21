package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.ItemClickSupport
import com.deguffroy.adrien.projetphoto.Utils.MAX_NUMBER_IMAGE_DELETE
import com.deguffroy.adrien.projetphoto.Views.MyPicAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_my_pic.*
import java.lang.IllegalStateException


/**
 * A simple [Fragment] subclass.
 *
 */
class MyPicFragment : BaseFragment(), MyPicAdapter.Listener, ActionMode.Callback{

    private lateinit var adapter:MyPicAdapter
    private var actionMode: ActionMode? = null

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

        this.clearListAndActionMode()
        this.configureRecyclerView()
        this.configureOnClickItemRecyclerView()
        this.setOnClickListener()

        //this.changeBottomInfo(mViewModel.currentListImagesToDelete.size)
        //this.retrieveUserData()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.clearListAndActionMode()
    }

    override fun onPause() {
        super.onPause()
        this.clearListAndActionMode()
    }

    // -------------------
    // ACTION MODE
    // -------------------

    override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
        when(p1?.itemId){
            R.id.selected_menu_delete -> {
                this.deleteSelectedImageFromFirebase()
                p0?.finish() }
            R.id.selected_menu_check_all -> {
                adapter.setAllItemsSelected()
                this.manageListFromMenu(p1.itemId)
            }
            R.id.selected_menu_clear_all -> {
                adapter.clearSelection()
                this.manageListFromMenu(p1.itemId)
            }
        }
        return true
    }

    override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
        p0?.menuInflater?.inflate(R.menu.action_bar_selected_menu, p1)
        return true
    }

    override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(p0: ActionMode?) {
        adapter.clearSelection()
        actionMode = null
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private fun clearListAndActionMode(){
        mViewModel.currentListImagesToDelete.clear()
        mViewModel.myPicSelectingMode = false
        actionMode?.finish()
    }

    private fun setOnClickListener(){
        my_pic_fragment_delete_icon.setOnClickListener { this.deleteSelectedImageFromFirebase() }
    }

    private fun configureRecyclerView(){
        this.adapter = MyPicAdapter(this, generateOptionsForAdapter(PicturesHelper().getAllPicturesFromUser(mViewModel.getCurrentUserUID()!!)))
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
            override fun onChanged() {
                super.onChanged()

            }
        })
        this.my_pic_recycler_view.adapter = this.adapter
        this.my_pic_recycler_view.layoutManager = GridLayoutManager(activity!!,3)
    }

    private fun configureOnClickItemRecyclerView(){
        ItemClickSupport.addTo(my_pic_recycler_view,R.layout.fragment_my_pic_item)
            .setOnItemClickListener{recyclerView, position, v ->
                if (mViewModel.myPicSelectingMode && actionMode != null){
                    val image = adapter.getItem(position)
                    this.manageListToDelete(image, position)

                }else{
                    (activity as BaseActivity).showFragment(DetailFragment.newInstance(adapter.getItem(position).documentId))
                }
            }
            .setOnItemLongClickListener { recyclerView, position, v ->
                Log.e("MyPicFragment","LongClick on item : $position")
                val image = adapter.getItem(position)
                if (actionMode == null) actionMode = activity!!.startActionMode(this)
                this.manageListToDelete(image, position)

                return@setOnItemLongClickListener true
            }

    }

    private fun generateOptionsForAdapter(query: Query) : FirestoreRecyclerOptions<Picture> =
        FirestoreRecyclerOptions.Builder<Picture>()
        .setQuery(query, Picture::class.java)
        .setLifecycleOwner(activity)
        .build()

    // -------------------
    // UI
    // -------------------

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
            Log.e("MyPicFragment","OnDataChanged : ${e.localizedMessage}")
        }
    }

    override fun onTooMuchItem() {
        this.displayMessage(getString(R.string.my_pic_fragment_too_much_image_to_delete, MAX_NUMBER_IMAGE_DELETE))
    }

    // -------------------
    // ACTION
    // -------------------

    private fun manageListToDelete(image:Picture, position:Int){
        adapter.toggleSelection(position)

        if (adapter.getSelectedItemCount() == 0 && mViewModel.myPicSelectingMode){
            actionMode?.finish()
            mViewModel.myPicSelectingMode = false
            Log.e("MyPicFragment","List to delete size AFTER CLEAR : ${mViewModel.currentListImagesToDelete.size}")

        }else{
            mViewModel.myPicSelectingMode = true
            actionMode?.title = resources.getString(R.string.my_pic_fragment_action_bar_text, adapter.getSelectedItemCount(), MAX_NUMBER_IMAGE_DELETE)
            actionMode?.invalidate()
        }

        when {
            mViewModel.currentListImagesToDelete.isEmpty() -> {
                mViewModel.myPicSelectingMode = true
                mViewModel.currentListImagesToDelete.add(image)
                Log.e("MyPicFragment","List is empty, ADDING ITEM, size : ${mViewModel.currentListImagesToDelete.size}")
            }
            else -> {
                mViewModel.currentListImagesToDelete.forEach {
                    if (image == it){
                        mViewModel.currentListImagesToDelete.remove(image)
                        Log.e("MyPicFragment","List not empty, Image already in list, REMOVING ITEM, size : ${mViewModel.currentListImagesToDelete.size}")
                        return
                    }
                }
                if (mViewModel.currentListImagesToDelete.size == adapter.getSelectedItemCount()){
                    Log.e("MyPicFragment","List FULL, DOING NOTHING, size : ${mViewModel.currentListImagesToDelete.size}")
                }else{
                    mViewModel.currentListImagesToDelete.add(image)
                    Log.e("MyPicFragment","List not empty, Image not in list, ADDING ITEM, size : ${mViewModel.currentListImagesToDelete.size}")
                }

            }
        }
        Log.e("MyPicFragment","FINAL List to delete size : ${mViewModel.currentListImagesToDelete.size}")
    }

    private fun manageListFromMenu(action:Int){
        if (action == R.id.selected_menu_check_all){
            mViewModel.currentListImagesToDelete.clear()
            val listToAdd = arrayListOf<Picture>()
            (0 until adapter.getSelectedItemCount()).forEach {
                val image = adapter.getItem(it)
                listToAdd.add(image)
            }
            mViewModel.currentListImagesToDelete.addAll(listToAdd)
            Log.e("MyPicFragment","List to delete size : ${mViewModel.currentListImagesToDelete.size}")
        }else{
            mViewModel.currentListImagesToDelete.clear()
        }
        actionMode?.title = resources.getString(R.string.my_pic_fragment_action_bar_text, adapter.getSelectedItemCount(), MAX_NUMBER_IMAGE_DELETE)
    }

    private fun deleteSelectedImageFromFirebase(){
        if(mViewModel.currentListImagesToDelete.size <= MAX_NUMBER_IMAGE_DELETE){
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()

            mViewModel.currentListImagesToDelete.forEach {
                val currentImage = it
                batch.delete(PicturesHelper().getPicturesCollection().document(currentImage.documentId!!))
            }
            batch.commit().addOnCompleteListener {
                if (it.isSuccessful){
                    val storageRef = FirebaseStorage.getInstance().reference.storage
                    val listToDelete = mViewModel.currentListImagesToDelete.toList()
                    listToDelete.forEach { pic ->
                        val urlReference = storageRef.getReferenceFromUrl(pic.urlImage)
                        urlReference.delete().addOnSuccessListener {
                            Log.e("MyPicFragment","Delete from Storage : OK")
                            mViewModel.currentListImagesToDelete.remove(pic)
                        }.addOnFailureListener {e ->
                            Log.e("MyPicFragment","Delete from Storage error : ${e.localizedMessage}")
                        }
                    }
                }
            }.addOnFailureListener {
                Log.e("MyPicFragment","Batch delete error : ${it.localizedMessage}")
            }

        }else{ // SHOW ERROR
           this.displayMessage(getString(R.string.my_pic_fragment_too_much_image_to_delete, MAX_NUMBER_IMAGE_DELETE))
        }
    }

    private fun displayMessage(message:String){
        BaseActivity().showSnackbarMessage(my_pic_fragment_coordinator,message)
    }
}
