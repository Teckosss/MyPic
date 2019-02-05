package com.deguffroy.adrien.projetphoto.Controllers.Fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.Api.CommentsHelper
import com.deguffroy.adrien.projetphoto.Api.PicturesHelper
import com.deguffroy.adrien.projetphoto.Controllers.Activities.BaseActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.DetailActivity
import com.deguffroy.adrien.projetphoto.Controllers.Activities.MainActivity
import com.deguffroy.adrien.projetphoto.Models.Comment
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.ItemClickSupport
import com.deguffroy.adrien.projetphoto.Utils.MAX_NUMBER_IMAGE_DELETE
import com.deguffroy.adrien.projetphoto.Utils.MAX_NUMBER_LIST_SIZE_DELETE
import com.deguffroy.adrien.projetphoto.Views.MyPicAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.Lists
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_my_pic.*
import java.lang.IllegalStateException

class MyPicFragment : BaseFragment(), MyPicAdapter.Listener, ActionMode.Callback{

    private lateinit var adapter:MyPicAdapter
    private var actionMode: ActionMode? = null

    companion object {
        fun newInstance():MyPicFragment = MyPicFragment()
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

    // Handle click on items in action bar
    override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
        when(p1?.itemId){
            R.id.selected_menu_delete -> {
                this.deleteSelectedImageFromFirebase()
                p0?.finish() }
            R.id.selected_menu_check_all -> {
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

    // If user is not in deleting mode, clear the selection
    override fun onDestroyActionMode(p0: ActionMode?) {
        adapter.clearSelection()
        actionMode = null
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    // Reset selecting mode, clear selection and hide action bar
    private fun clearListAndActionMode(){
        mViewModel.currentListImagesToDelete.clear()
        mViewModel.myPicSelectingMode = false
        actionMode?.finish()
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
            .setOnItemClickListener{ _, position, _ ->
                if (mViewModel.myPicSelectingMode && actionMode != null){ // If user is selecting picture, we add it into the list
                    val image = adapter.getItem(position)
                    this.manageListToDelete(image, position)

                }else{ // User only want to go to detail
                    startActivity(DetailActivity.newInstance(activity!! , adapter.getItem(position).documentId))
                }
            }
                // On long click, start selecting mode or just add picture to list
            .setOnItemLongClickListener { _, position, _ ->
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

    // Manage if there is picture to display
    override fun onDataChanged() {
        try{
            if (adapter.itemCount == 0){ // Hiding recyclerView and show textView
                my_pic_recycler_view.visibility = View.GONE
                my_pic_no_image_yet_layout.visibility = View.VISIBLE
            } else { // Hiding TextView and show recyclerView
                my_pic_recycler_view.visibility = View.VISIBLE
                my_pic_no_image_yet_layout.visibility = View.GONE
            }
        }catch (e:IllegalStateException){
            Log.e("MyPicFragment","OnDataChanged : ${e.localizedMessage}")
        }
    }

    // Callback from adapter, list is full, display message to user
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
            Log.i("MyPicFragment","List to delete size AFTER CLEAR : ${mViewModel.currentListImagesToDelete.size}")

        }else{
            mViewModel.myPicSelectingMode = true
            actionMode?.title = resources.getString(R.string.my_pic_fragment_action_bar_text, adapter.getSelectedItemCount(), MAX_NUMBER_IMAGE_DELETE)
            actionMode?.invalidate()
        }

        when {
            // List is empty we add this item
            mViewModel.currentListImagesToDelete.isEmpty() -> {
                mViewModel.myPicSelectingMode = true
                mViewModel.currentListImagesToDelete.add(image)
                Log.i("MyPicFragment","List is empty, ADDING ITEM, size : ${mViewModel.currentListImagesToDelete.size}")
            }
            else -> {
                mViewModel.currentListImagesToDelete.forEach {
                    // Checking if this item is already in list
                    if (image == it){
                        // Remove this item
                        mViewModel.currentListImagesToDelete.remove(image)
                        Log.i("MyPicFragment","List not empty, Image already in list, REMOVING ITEM, size : ${mViewModel.currentListImagesToDelete.size}")
                        return
                    }
                }
                if (mViewModel.currentListImagesToDelete.size == adapter.getSelectedItemCount()){
                    // List is full, we can't add this new item
                    Log.i("MyPicFragment","List FULL, DOING NOTHING, size : ${mViewModel.currentListImagesToDelete.size}")
                }else{
                    // We add this item to list
                    mViewModel.currentListImagesToDelete.add(image)
                    Log.i("MyPicFragment","List not empty, Image not in list, ADDING ITEM, size : ${mViewModel.currentListImagesToDelete.size}")
                }

            }
        }
        Log.i("MyPicFragment","FINAL List to delete size : ${mViewModel.currentListImagesToDelete.size}")
    }

    // Handle click on icon from action bar
    private fun manageListFromMenu(action:Int){
        if (action == R.id.selected_menu_check_all){ // Check all picture to fill the list size
            mViewModel.currentListImagesToDelete.clear()
            adapter.clearSelection()

            val listToAdd = arrayListOf<Picture>()
            Log.i("MyPicFragment","Item number in adapter : ${adapter.getSelectedItemCount()}")

            (0 until adapter.itemCount).forEach {// Adding each picture to list
                if (it < adapter.itemCount && it < MAX_NUMBER_IMAGE_DELETE){
                    adapter.toggleSelection(it)
                    Log.i("MyPicFragment","listToAdd size : ${listToAdd.size} \n $listToAdd")
                    val image = adapter.getItem(it)
                    listToAdd.add(image)
                }
            }

            mViewModel.currentListImagesToDelete.addAll(listToAdd)
            Log.i("MyPicFragment","List to delete size : ${mViewModel.currentListImagesToDelete.size}")
        }else{
            mViewModel.currentListImagesToDelete.clear()
        }
        actionMode?.title = resources.getString(R.string.my_pic_fragment_action_bar_text, adapter.getSelectedItemCount(), MAX_NUMBER_IMAGE_DELETE)
    }

    // Deleting selected pictures from firebase
    private fun deleteSelectedImageFromFirebase(){
        if(mViewModel.currentListImagesToDelete.size <= MAX_NUMBER_IMAGE_DELETE){ // Check if list size is not higher than maximum item for a single batch
            val db = FirebaseFirestore.getInstance()

            mViewModel.currentListImagesToDelete.forEach {
                val currentImage = it
                val batchImage = db.batch()
                batchImage.delete(PicturesHelper().getPicturesCollection().document(currentImage.documentId!!))
                CommentsHelper().getCommentsForPicture(currentImage.documentId!!).get().addOnCompleteListener {commentTask ->
                    // Retrieve comments for picture we are trying to delete
                    if(commentTask.isSuccessful){
                        Log.i("MyPicFragment","Comment's number : ${commentTask.result!!.size()}")
                        if (commentTask.result!!.isEmpty){
                            Log.i("MyPicFragment","This picture doesn't have any comment")
                        }else{ // If picture has comment we add each of them into a list to delete with a batch
                            for (document in commentTask.result!!){
                                val comment = document.toObject(Comment::class.java)
                                Log.i("MyPicFragment","comment : $comment")
                                mViewModel.currentListCommentToDelete.add(comment)
                            }
                            Log.i("MyPicFragment","currentListCommentToDelete size : ${mViewModel.currentListCommentToDelete.size}")
                        }
                    }
                }

                batchImage.commit().addOnCompleteListener {batch ->
                    if (batch.isSuccessful){
                        val storageRef = FirebaseStorage.getInstance().reference.storage
                        val listToDelete = mViewModel.currentListImagesToDelete.toList()
                        listToDelete.forEach { pic ->
                            val urlReference = storageRef.getReferenceFromUrl(pic.urlImage)
                            urlReference.delete().addOnSuccessListener {
                                // Deleting each picture from Firebase Storage
                                Log.i("MyPicFragment","Delete from Storage : OK")
                                mViewModel.currentListImagesToDelete.remove(pic)
                            }.addOnFailureListener {e ->
                                Log.e("MyPicFragment","Delete from Storage error : ${e.localizedMessage}")
                            }
                        }



                        if (mViewModel.currentListCommentToDelete.size < MAX_NUMBER_IMAGE_DELETE){
                            // If comment's number is lower than batch's size, we add each comment to a list
                            val batchCommentSimple = db.batch()
                            mViewModel.currentListCommentToDelete.forEach { commentIndex ->
                                batchCommentSimple.delete(CommentsHelper().getCommentsCollection().document(commentIndex.documentId!!))
                            }
                            batchCommentSimple.commit().addOnSuccessListener {
                                Log.i("MyPicFragment","Batch comment success!")
                            }.addOnFailureListener {commentFailure->
                                Log.e("MyPicFragment","Batch comment failure! ${commentFailure.localizedMessage}")
                            }
                        }else{
                            // If comment's number is higher batch's size we need to create several batch
                            val listPartitions =  Lists.partition(mViewModel.currentListCommentToDelete, MAX_NUMBER_LIST_SIZE_DELETE)
                            listPartitions.forEach{partitionList->
                                // Creating a batch for each sub-list
                                val batchCommentMultiple = db.batch()
                                partitionList.forEach{partitionComment ->
                                    batchCommentMultiple.delete(CommentsHelper().getCommentsCollection().document(partitionComment.documentId!!))
                                }
                                batchCommentMultiple.commit().addOnSuccessListener {
                                    Log.i("MyPicFragment","Batch partitionComment success!")
                                }.addOnFailureListener { partitionFail ->
                                    Log.e("MyPicFragment","Batch comment failure! ${partitionFail.localizedMessage}")
                                }
                            }
                        }

                    }
                }.addOnFailureListener {
                    Log.e("MyPicFragment","Batch delete error : ${it.localizedMessage}")
                }

            }


        }else{ // SHOW ERROR
           this.displayMessage(getString(R.string.my_pic_fragment_too_much_image_to_delete, MAX_NUMBER_IMAGE_DELETE))
        }
    }

    private fun displayMessage(message:String){
        BaseActivity().showSnackbarMessage(my_pic_fragment_coordinator,message,Snackbar.LENGTH_LONG, (activity as MainActivity).main_activity_fab)
    }
}
