package org.beatonma.lib.ui.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

const val VIEW_TYPE_DEFAULT = 0

abstract class BaseRecyclerViewAdapter : RecyclerView.Adapter<BaseViewHolder>(),
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    override fun onItemDismiss(position: Int) = Unit

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean = false

    private var inflater: LayoutInflater? = null

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.bind(position)

    private fun getLayoutInflater(view: View): LayoutInflater {
        if (inflater == null) {
            inflater = LayoutInflater.from(view.context)
        }
        return inflater ?: throw Exception("LayoutInflater is not available")
    }

    protected fun inflate(parent: ViewGroup, layoutID: Int, attachToRoot: Boolean = false) =
            getLayoutInflater(parent).inflate(layoutID, parent, attachToRoot)

    open fun setupTouchHelper(recyclerView: RecyclerView?, callback: SimpleItemTouchHelperCallback?) {
        callback?.let {
            ItemTouchHelper(it).attachToRecyclerView(recyclerView)
        }
    }

    fun diff(callback: DiffUtil.Callback, detectMoves: Boolean = false) {
        DiffUtil.calculateDiff(callback, detectMoves).dispatchUpdatesTo(this)
    }
}

inline fun <T> simpleDiffCallback(
        oldList: List<T>?, newList: List<T>?,
        crossinline sameItem: (T?, T?) -> Boolean = { old, new -> old?.equals(new) ?: false },
        crossinline sameContent: (T?, T?) -> Boolean = { old, new -> old?.equals(new) ?: false }
): DiffUtil.Callback {
    return object : DiffUtil.Callback() {
        override fun getOldListSize() = oldList?.size ?: 1
        override fun getNewListSize() = newList?.size ?: 1

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                sameItem(oldList?.get(oldItemPosition), newList?.get(newItemPosition))

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                sameContent(oldList?.get(oldItemPosition), newList?.get(newItemPosition))
    }
}
