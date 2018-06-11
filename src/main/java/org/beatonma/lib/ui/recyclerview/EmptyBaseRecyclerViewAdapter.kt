package org.beatonma.lib.ui.recyclerview

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import org.beatonma.lib.recyclerview.R

/**
 * Created by Michael on 29/07/2017.
 * A RecyclerView Adapter that provides an interface to handle null/empty datasets in a simple way.
 */

abstract class EmptyBaseRecyclerViewAdapter : BaseRecyclerViewAdapter {

    protected lateinit var mEmpty: EmptyViews

    abstract val items: List<*>?

    constructor() {
        setEmptyViews(object : EmptyViewsAdapter() {
            override val dataset: Collection<*>?
                get() = items
        })
    }

    /**
     * @param emptyViews An implementation of the EmptyViews interface
     */
    constructor(@NonNull emptyViews: EmptyViews) {
        mEmpty = emptyViews
    }

    /**
     * @param emptyViews An implementation of the EmptyViews interface
     */
    fun setEmptyViews(@NonNull emptyViews: EmptyViews) {
        mEmpty = emptyViews
    }

    @CallSuper
    override fun getItemCount(): Int {
        val dataset = mEmpty.dataset
        return if (dataset == null) {
            if (mEmpty.nullLayoutID == 0) 0 else 1
        } else if (dataset.isEmpty()) {
            if (mEmpty.emptyLayoutID == 0) 0 else 1
        } else {
            dataset.size
        }
//        Log.w(TAG, "Using EmptyBaseRecyclerViewAdapter without EmptyViews interface - you should use BaseRecyclerViewAdapter if you do not require empty/null dataset handling")
    }

    @CallSuper
    override fun getItemViewType(position: Int): Int {
//        if (mEmpty == null) {
//            Log.w(TAG, "Using EmptyBaseRecyclerViewAdapter without EmptyViews interface - you should use BaseRecyclerViewAdapter if you do not require empty/null dataset handling")
//            return super.getItemViewType(position)
//        }
        val dataset = mEmpty.dataset
        return if (dataset == null) {
            VIEW_TYPE_LOADING
        } else if (dataset.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            BaseRecyclerViewAdapter.VIEW_TYPE_DEFAULT
        }
    }

    @CallSuper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
//        if (mEmpty != null) {
            val layoutID: Int
            when (viewType) {
                VIEW_TYPE_EMPTY -> {
                    layoutID = mEmpty.emptyLayoutID
                    return if (layoutID == 0) {
                        InvisibleViewHolder(inflate(parent, R.layout.vh_invisible))
                    } else {
                        mEmpty.getEmptyViewHolder(inflate(parent, mEmpty.emptyLayoutID))
                    }
                }
                VIEW_TYPE_LOADING -> {
                    layoutID = mEmpty.nullLayoutID
                    return if (layoutID == 0) {
                        InvisibleViewHolder(inflate(parent, R.layout.vh_invisible))
                    } else {
                        mEmpty.getNullViewHolder(inflate(parent, mEmpty.nullLayoutID))
                    }
                }
            }
//        }
        Log.w(TAG, "Using EmptyBaseRecyclerViewAdapter without EmptyViews interface - you should use BaseRecyclerViewAdapter if you do not require empty/null dataset handling")
        return InvisibleViewHolder(inflate(parent, R.layout.vh_invisible))
    }

    @NonNull
    fun getDiffCallback(@Nullable oldList: List<*>?,
                        @Nullable newList: List<*>?): DiffUtil.Callback {
        return object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return if (oldList == null) {
                    if (mEmpty.nullLayoutID == 0) 0 else 1
                } else {
                    Math.max(if (mEmpty.emptyLayoutID == 0) 0 else 1, oldList.size)
                }
            }

            override fun getNewListSize(): Int {
                return if (newList == null) {
                    if (mEmpty.nullLayoutID == 0) 0 else 1
                } else {
                    Math.max(if (mEmpty.emptyLayoutID == 0) 0 else 1, newList.size)
                }
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                if (oldList == null || newList == null) {
                    return false
                }
                return if (oldItemPosition >= oldList.size || newItemPosition >= newList.size) {
                    false
                } else oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList!![oldItemPosition] == newList!![newItemPosition]
            }
        }
    }

    override fun diff(@Nullable oldList: List<*>?, @Nullable newList: List<*>?) {
        DiffUtil.calculateDiff(getDiffCallback(oldList, newList), true).dispatchUpdatesTo(this)
    }

    override fun diff(@NonNull callback: DiffUtil.Callback) {
        getDiff(callback).dispatchUpdatesTo(this)
    }

    override fun diff(@NonNull callback: DiffUtil.Callback, detectMoves: Boolean) {
        getDiff(callback, detectMoves).dispatchUpdatesTo(this)
    }

    abstract class DiffAdapter<T>(
                                  val oldList: List<T>?,
                                  val newList: List<T>?) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList?.size ?: 0
        }

        override fun getNewListSize(): Int {
            return newList?.size ?: 0
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            try {
                return oldList!![oldItemPosition] == newList!![newItemPosition]
            } catch (e: Exception) {
                return false
            }

        }
    }

    /**
     * Handler for null or empty datasets, showing loading views or empty messages
     * Intended use:
     * - If dataset is null then show a loading view
     * - If dataset is has no items then show a view that explains this to the user
     */
    interface EmptyViews {

        val nullLayoutID: Int
        val emptyLayoutID: Int

        /**
         * Return the dataset for this adapter
         */
        val dataset: Collection<*>?

        /**
         * Return a ViewHolder to use when the dataset=null
         * This usually means that data loading is in progress
         */
        @NonNull
        fun getNullViewHolder(view: View): BaseViewHolder

        /**
         * Return a ViewHolder to use when getDataset().isEmpty()
         * This usually means data loading has finished but has no contents
         */
        @NonNull
        fun getEmptyViewHolder(view: View): BaseViewHolder
    }

    abstract class EmptyViewsAdapter : EmptyViews {

        override val nullLayoutID: Int = R.layout.vh_loading
        override val emptyLayoutID: Int = R.layout.vh_empty

        @NonNull
        override fun getNullViewHolder(view: View): BaseViewHolder {
            return LoadingViewHolder(view)
        }

        @NonNull
        override fun getEmptyViewHolder(view: View): BaseViewHolder {
            return EmptyViewHolder(view)
        }

        private inner class EmptyViewHolder internal constructor(v: View) : BaseViewHolder(v) {
            override fun bind(position: Int) {

            }
        }

        private inner class LoadingViewHolder internal constructor(v: View) : BaseViewHolder(v) {
            override fun bind(position: Int) {

            }
        }
    }

    private class InvisibleViewHolder internal constructor(v: View) : BaseViewHolder(v) {

        override fun bind(position: Int) {

        }
    }

    companion object {
        protected const val TAG = "EmptyBaseRvAdapter"

        private const val VIEW_TYPE_LOADING = 123
        private const val VIEW_TYPE_EMPTY = 321

        @NonNull
        fun getDefaultDiffCallback(@Nullable oldList: List<*>?,
                                            @Nullable newList: List<*>?): DiffUtil.Callback {
            return object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return Math.max(1, oldList?.size ?: 0)
                }

                override fun getNewListSize(): Int {
                    return Math.max(1, newList?.size ?: 0)
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    if (oldList == null || newList == null) {
                        return false
                    }
                    return if (oldItemPosition >= oldList.size || newItemPosition >= newList.size) {
                        false
                    } else oldList[oldItemPosition] == newList[newItemPosition]
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldList!![oldItemPosition] == newList!![newItemPosition]
                }
            }
        }

        @NonNull
        private fun getDiff(callback: DiffUtil.Callback): DiffUtil.DiffResult {
            return DiffUtil.calculateDiff(callback)
        }

        @NonNull
        private fun getDiff(callback: DiffUtil.Callback, detectMoves: Boolean): DiffUtil.DiffResult {
            return DiffUtil.calculateDiff(callback, detectMoves)
        }
    }
}
