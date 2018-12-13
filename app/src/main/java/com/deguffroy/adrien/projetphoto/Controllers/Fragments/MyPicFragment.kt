package com.deguffroy.adrien.projetphoto.Controllers.Fragments


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.ItemClickSupport
import com.deguffroy.adrien.projetphoto.Views.MyPicAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
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
                    val transaction = activity!!.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_view, DetailFragment.newInstance(adapter.getItem(position).documentId))
                    transaction.addToBackStack(null)
                    transaction.commit()
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

    private fun changeBottomInfo(listSize:Int){
        if (listSize > 0){
            mViewModel.myPicSelectingMode = true
            my_pic_fragment_bottom_info_container.visibility = View.VISIBLE
            my_pic_fragment_delete_number.text = listSize.toString()
        }else{
            adapter.clearSelection()
            my_pic_fragment_bottom_info_container.visibility = View.GONE
            mViewModel.myPicSelectingMode = false
        }
    }

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

    // -------------------
    // ACTION
    // -------------------

    private fun manageListToDelete(image:Picture, position:Int){
        adapter.toggleSelection(position)
        val count = adapter.getSelectedItemCount()
        if (count == 0){
            actionMode?.finish()
            mViewModel.myPicSelectingMode = false
        }else{
            mViewModel.myPicSelectingMode = true
            actionMode?.title = resources.getString(R.string.my_pic_fragment_action_bar_text, count)
            actionMode?.invalidate()
        }
        if (mViewModel.currentListImagesToDelete.isEmpty()){
            mViewModel.myPicSelectingMode = true
            mViewModel.currentListImagesToDelete.add(image)

        }else{
            (0 until mViewModel.currentListImagesToDelete.size).forEach {
                if (image == mViewModel.currentListImagesToDelete[it]){
                    mViewModel.currentListImagesToDelete.remove(image)

                    //adapter.triggerAction(mViewModel.currentListImagesToDelete.size, position)
                    return
                }
            }
            mViewModel.currentListImagesToDelete.add(image)

        }
        //adapter.triggerAction(mViewModel.currentListImagesToDelete.size, position)
    }

    private fun manageListFromMenu(action:Int){
        if (action == R.id.selected_menu_check_all){
            mViewModel.currentListImagesToDelete.clear()
            val listToAdd = arrayListOf<Picture>()
            (0 until adapter.itemCount).forEach {
                val image = adapter.getItem(it)
                listToAdd.add(image)
            }
            mViewModel.currentListImagesToDelete.addAll(listToAdd)
        }else{
            mViewModel.currentListImagesToDelete.clear()
        }
        actionMode?.title = resources.getString(R.string.my_pic_fragment_action_bar_text, adapter.getSelectedItemCount())
    }

    private fun deleteSelectedImageFromFirebase(){
        (0 until mViewModel.currentListImagesToDelete.size).forEach {
            val currentImage = mViewModel.currentListImagesToDelete[it]
            PicturesHelper().deletePictureByID(currentImage.documentId!!).addOnSuccessListener {
                Log.e("MyPicFragment","Delete from Firestore : OK")
                val storageRef = FirebaseStorage.getInstance().reference.storage.getReferenceFromUrl(currentImage.urlImage)
                storageRef.delete().addOnSuccessListener {
                    Log.e("MyPicFragment","Delete from Storage : OK")
                    mViewModel.currentListImagesToDelete.remove(currentImage)
                    //this.changeBottomInfo(mViewModel.currentListImagesToDelete.size)
                }.addOnFailureListener {
                    Log.e("MyPicFragment","Failed to delete from Storage!")
                }
            }.addOnFailureListener {
                Log.e("MyPicFragment","Failed to delete from Firestore!")
            }
        }
    }
}
