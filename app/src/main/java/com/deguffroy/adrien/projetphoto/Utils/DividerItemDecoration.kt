package com.deguffroy.adrien.projetphoto.Utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.deguffroy.adrien.projetphoto.R

/**
 * Created by Adrien Deguffroy on 09/01/2019.
 */

// Add a horizontal bar to separate each items of a RecyclerView
class DividerItemDecoration(var context: Context, var paddingLeft:Int, var paddingRight:Int) : RecyclerView.ItemDecoration() {

    private var mDivider: Drawable? = null

    init {
        mDivider = ContextCompat.getDrawable(context, R.drawable.divider)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        if (parent.getChildAdapterPosition(view) == 0) {
            return
        }

        outRect.top = mDivider!!.intrinsicHeight
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft + convertDpToPx(paddingLeft)
        val dividerRight = parent.width - parent.paddingRight + convertDpToPx(paddingRight)

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + mDivider!!.intrinsicHeight

            mDivider!!.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            mDivider!!.draw(c)
        }
    }

    private fun convertDpToPx(px: Int): Int {
        return (px * Resources.getSystem().displayMetrics.density).toInt()
    }
}