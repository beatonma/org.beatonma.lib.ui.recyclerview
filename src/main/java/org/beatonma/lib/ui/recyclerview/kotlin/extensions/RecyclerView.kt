package org.beatonma.lib.ui.recyclerview.kotlin.extensions

import android.content.ContentValues.TAG
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.beatonma.lib.ui.recyclerview.BaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.itemanimator.FadeItemAnimator
import java.lang.ref.WeakReference

fun RecyclerView.setup(adapter: BaseRecyclerViewAdapter,
                       layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context),
                       itemAnimator: RecyclerView.ItemAnimator = FadeItemAnimator(),
                       touchHelper: Boolean = false) {
    setAdapter(adapter)
    setLayoutManager(layoutManager)
    setItemAnimator(itemAnimator)
    if (touchHelper) adapter.setupWithTouchHelper(this)
}

/**
 * Automatically set up the correct span count of the grid based on the required span width
 * and the space available on the device
 */
fun RecyclerView.setupGrid(adapter: BaseRecyclerViewAdapter, columnWidth: Int) {
    setup(adapter,
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL))

    val weakRv = WeakReference<RecyclerView>(this)

    post {
        val recyclerView1 = weakRv.get() ?: return@post

        try {
            val columnCount = Math.max(
                    1,
                    Math.floor((recyclerView1.measuredWidth / columnWidth).toDouble()).toInt())
            val lm = recyclerView1.layoutManager as StaggeredGridLayoutManager
            lm.spanCount = columnCount
        } catch (e: Exception) {
            Log.e(TAG, "Error updating StaggeredGridLayoutManager column count: $e")
        }
    }
}
