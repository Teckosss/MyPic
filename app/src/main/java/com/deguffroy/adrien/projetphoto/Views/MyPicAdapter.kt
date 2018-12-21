package com.deguffroy.adrien.projetphoto.Views

import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Utils.MAX_NUMBER_IMAGE_DELETE
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.fragment_my_pic_item.view.*

/**
 * Created by Adrien Deguffroy on 26/11/2018.
 */
class MyPicAdapter(var callback:Listener, @NonNull options: FirestoreRecyclerOptions<Picture>, var selectedItems:SparseBooleanArray = SparseBooleanArray()) : FirestoreRecyclerAdapter<Picture, MyPicViewHolder>(options) {

    interface Listener{
        fun onDataChanged()
        fun onTooMuchItem()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPicViewHolder {
        return MyPicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_my_pic_item, parent,false))
    }

    override fun onBindViewHolder(p0: MyPicViewHolder, p1: Int, @NonNull p2: Picture) {
        p0.updateWithData(p2, isSelected(p1))
    }

    override fun onDataChanged() {
        super.onDataChanged()
        this.callback.onDataChanged()
    }

    private fun onTooMuchItem(){
       this.callback.onTooMuchItem()
    }

    fun isSelected(position:Int):Boolean = getSelectedItems().contains(position)

    fun toggleSelection(position: Int){
        when {
            selectedItems.get(position,false) -> selectedItems.delete(position)
            getSelectedItemCount() < MAX_NUMBER_IMAGE_DELETE -> selectedItems.put(position, true)
            else -> this.onTooMuchItem()
        }
        notifyItemChanged(position)
    }

    fun clearSelection(){
        val selection = getSelectedItems()
        selectedItems.clear()
        for (i:Int in selection){
            notifyItemChanged(i)
        }
    }

    fun setAllItemsSelected(){
        this.clearSelection()
        for (i in 0 until MAX_NUMBER_IMAGE_DELETE){
            if (!selectedItems.get(i,false)){selectedItems.put(i, true)}
        }
        notifyDataSetChanged()
    }

    fun getSelectedItemCount() = selectedItems.size()

    fun getSelectedItems() : List<Int> {
        val items = ArrayList<Int>(selectedItems.size())
        (0 until selectedItems.size()).forEach {
            items.add(selectedItems.keyAt(it))
        }
        return items
    }
}