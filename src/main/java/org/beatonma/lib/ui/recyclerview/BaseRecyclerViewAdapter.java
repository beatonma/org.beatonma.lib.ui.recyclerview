package org.beatonma.lib.ui.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Michael on 14/07/2016.
 */
public abstract class BaseRecyclerViewAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<BaseViewHolder>
        implements SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    protected final static String TAG = "BaseRvAdapter";
    protected final static int VIEW_TYPE_DEFAULT = 0;

    private LayoutInflater mInflater;

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        holder.bind(position);
    }

    protected LayoutInflater getLayoutInflater(final View v) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(v.getContext());
        }

        return mInflater;
    }

    protected View inflate(final ViewGroup parent, final int layoudId) {
        return getLayoutInflater(parent).inflate(layoudId, parent, false);
    }

    public void notifyItemAdded() {
        notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public boolean onItemMove(final int positionFrom, final int positionTo) {
        return false;
    }

    @Override
    public void onItemDismiss(final int position) {

    }

    @Deprecated
    /**
     * @deprecated Use {@link #diff(DiffUtil.Callback)}
     */
    public void setDiffCallback(final DiffUtil.Callback callback) {

    }

    public static DiffUtil.Callback getDefaultDiffCallback(final List<?> oldList, final List<?> newList) {
        return new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList == null ? 0 : oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList == null ? 0 : newList.size();
            }

            @Override
            public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
                if (oldList == null || newList == null) {
                    return false;
                } else if (oldItemPosition > oldList.size() ||
                        newItemPosition > newList.size()) {
                    return false;
                }

                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }
        };
    }

    /**
     * {@link #getItemTouchCallback(View)} must be overriden for this to have any effect
     * @param recyclerView
     */
    public void setupWithTouchHelper(final RecyclerView recyclerView) {
        final SimpleItemTouchHelperCallback callback = getItemTouchCallback(recyclerView);
        if (callback == null) {
            return;
        }

        final ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

    protected SimpleItemTouchHelperCallback getItemTouchCallback(final View v) {
        return null;
    }

    public void diff(final List<?> oldList, final List<?> newList) {
        getDiff(oldList, newList).dispatchUpdatesTo(this);
    }

    public void diff(final List<?> oldList, final List<?> newList, final boolean detectMoves) {
        getDiff(oldList, newList, detectMoves).dispatchUpdatesTo(this);
    }

    public void diff(final DiffUtil.Callback callback) {
        getDiff(callback).dispatchUpdatesTo(this);
    }

    public void diff(final DiffUtil.Callback callback, final boolean detectMoves) {
        getDiff(callback, detectMoves).dispatchUpdatesTo(this);
    }

    private static DiffUtil.DiffResult getDiff(final List<?> oldList, final List<?> newList) {
        return DiffUtil.calculateDiff(getDefaultDiffCallback(oldList, newList));
    }

    private static DiffUtil.DiffResult getDiff(final List<?> oldList, final List<?> newList, final boolean detectMoves) {
        return DiffUtil.calculateDiff(getDefaultDiffCallback(oldList, newList), detectMoves);
    }

    private static DiffUtil.DiffResult getDiff(final DiffUtil.Callback callback) {
        return DiffUtil.calculateDiff(callback);
    }

    private static DiffUtil.DiffResult getDiff(final DiffUtil.Callback callback, final boolean detectMoves) {
        return DiffUtil.calculateDiff(callback, detectMoves);
    }
}