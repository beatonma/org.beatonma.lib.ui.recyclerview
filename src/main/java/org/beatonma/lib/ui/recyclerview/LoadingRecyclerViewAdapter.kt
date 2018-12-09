package org.beatonma.lib.ui.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import org.beatonma.lib.recyclerview.R

/**
 * Created by Michael on 29/07/2017.
 * A RecyclerView Adapter that provides an interface to handle null/empty datasets in a simple way.
 */
private const val VIEW_TYPE_LOADING = 123
private const val VIEW_TYPE_EMPTY = 321

abstract class LoadingRecyclerViewAdapter(
        val nullLayoutID: Int = R.layout.vh_loading_custom,
        val emptyLayoutID: Int = R.layout.vh_invisible
): BaseRecyclerViewAdapter() {
    abstract val items: List<*>?

    /**
     * Return a ViewHolder to use when items == null
     * This usually means data is still being loaded
     */
    open fun getNullViewHolder(view: View): BaseViewHolder {
        return LoadingViewHolder(view)
    }

    /**
     * Return a ViewHolder to use when items,isEmpty()
     * This usually means data loading has finished but has no contents
     */
    open fun getEmptyViewHolder(view: View): BaseViewHolder {
        return InvisibleViewHolder(view)
    }

    @CallSuper
    override fun getItemCount(): Int {
        return when {
            items == null -> if (nullLayoutID == 0) 0 else 1
            items?.isEmpty() == true -> if (emptyLayoutID == 0) 0 else 1
            else -> items?.size ?: 0
        }
    }

    @CallSuper
    override fun getItemViewType(position: Int): Int {
        return when {
            items == null -> VIEW_TYPE_LOADING
            items?.isEmpty() == true -> VIEW_TYPE_EMPTY
            else -> VIEW_TYPE_DEFAULT
        }
    }

    @CallSuper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOADING -> getNullViewHolder(inflate(parent, nullLayoutID))
            VIEW_TYPE_EMPTY -> getEmptyViewHolder(inflate(parent, emptyLayoutID))
            else -> InvisibleViewHolder(inflate(parent, R.layout.vh_invisible))
        }
    }

    abstract class DiffAdapter<T>(val oldList: List<T>?,
                                  val newList: List<T>?) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList?.size ?: 0
        }

        override fun getNewListSize(): Int {
            return newList?.size ?: 0
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList?.get(oldItemPosition) == newList?.get(newItemPosition) ?: false
        }
    }

    private inner class LoadingViewHolder internal constructor(v: View) : BaseViewHolder(v) {
        override fun bind(position: Int) {

        }
    }

    private class InvisibleViewHolder internal constructor(v: View) : BaseViewHolder(v) {
        override fun bind(position: Int) {

        }
    }
}
