package org.beatonma.lib.ui.recyclerview;

import android.view.View;
import android.view.ViewGroup;

import org.beatonma.lib.log.Log;
import org.beatonma.lib.recyclerview.R;

import java.util.Collection;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

/**
 * Created by Michael on 29/07/2017.
 * A RecyclerView Adapter that provides an interface to handle null/empty datasets in a simple way.
 */

public abstract class EmptyBaseRecyclerViewAdapter extends BaseRecyclerViewAdapter {
    protected final static String TAG = "EmptyBaseRvAdapter";

    protected final static int VIEW_TYPE_LOADING = 123;
    protected final static int VIEW_TYPE_EMPTY = 321;

    protected EmptyViews mEmpty;

    @Nullable
    public abstract List getItems();

    public EmptyBaseRecyclerViewAdapter() {
        setEmptyViews(new EmptyViewsAdapter() {
            @Nullable
            @Override
            public Collection<?> getDataset() {
                return getItems();
            }
        });
    }

    /**
     * @param emptyViews An implementation of the EmptyViews interface
     */
    public EmptyBaseRecyclerViewAdapter(final EmptyViews emptyViews) {
        mEmpty = emptyViews;
    }

    /**
     * @param emptyViews An implementation of the EmptyViews interface
     */
    public void setEmptyViews(final EmptyViews emptyViews) {
        mEmpty = emptyViews;
    }

    @CallSuper
    @Override
    public int getItemCount() {
        if (mEmpty != null) {
            Collection dataset = mEmpty.getDataset();
            if (dataset == null) {
                return mEmpty.getNullLayoutID() == 0 ? 0 : 1;
            }
            else if (dataset.isEmpty()) {
                return mEmpty.getEmptyLayoutID() == 0 ? 0 : 1;
            }
            else {
                return dataset.size();
            }
        }
        Log.w(TAG, "Using EmptyBaseRecyclerViewAdapter without EmptyViews interface - you should use BaseRecyclerViewAdapter if you do not require empty/null dataset handling");
        return 0;
    }

    @CallSuper
    @Override
    public int getItemViewType(final int position) {
        if (mEmpty == null) {
            Log.w(TAG, "Using EmptyBaseRecyclerViewAdapter without EmptyViews interface - you should use BaseRecyclerViewAdapter if you do not require empty/null dataset handling");
            return super.getItemViewType(position);
        }
        final Collection<?> dataset = mEmpty.getDataset();
        if (dataset == null) {
            return VIEW_TYPE_LOADING;
        }
        else if (dataset.isEmpty()) {
            return VIEW_TYPE_EMPTY;
        }
        else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @NonNull
    @CallSuper
    @Override
    public BaseViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (mEmpty != null) {
            final int layoutID;
            switch (viewType) {
                case VIEW_TYPE_EMPTY:
                    layoutID = mEmpty.getEmptyLayoutID();
                    if (layoutID == 0) {
                        return new InvisibleViewHolder(inflate(parent, R.layout.vh_invisible));
                    }
                    else {
                        return mEmpty.getEmptyViewHolder(inflate(parent, mEmpty.getEmptyLayoutID()));
                    }
                case VIEW_TYPE_LOADING:
                    layoutID = mEmpty.getNullLayoutID();
                    if (layoutID == 0) {
                        return new InvisibleViewHolder(inflate(parent, R.layout.vh_invisible));
                    }
                    else {
                        return mEmpty.getNullViewHolder(inflate(parent, mEmpty.getNullLayoutID()));
                    }
            }
        }
        Log.w(TAG, "Using EmptyBaseRecyclerViewAdapter without EmptyViews interface - you should use BaseRecyclerViewAdapter if you do not require empty/null dataset handling");

        return null;
    }

    public DiffUtil.Callback getDiffCallback(final List<?> oldList, final List<?> newList) {
        return new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                if (oldList == null) {
                    return mEmpty.getNullLayoutID() == 0 ? 0 : 1;
                }
                else {
                    return Math.max(mEmpty.getEmptyLayoutID() == 0 ? 0 : 1, oldList.size());
                }
            }

            @Override
            public int getNewListSize() {
                if (newList == null) {
                    return mEmpty.getNullLayoutID() == 0 ? 0 : 1;
                }
                else {
                    return Math.max(mEmpty.getEmptyLayoutID() == 0 ? 0 : 1, newList.size());
                }
            }

            @Override
            public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
                if (oldList == null || newList == null) {
                    return false;
                }
                if (oldItemPosition >= oldList.size() || newItemPosition >= newList.size()) {
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

    @NonNull
    public static DiffUtil.Callback getDefaultDiffCallback(
            @Nullable final List<?> oldList, @Nullable final List<?> newList) {
        return new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return Math.max(1, oldList == null ? 0 : oldList.size());
            }

            @Override
            public int getNewListSize() {
                return Math.max(1, newList == null ? 0 : newList.size());
            }

            @Override
            public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
                if (oldList == null || newList == null) {
                    return false;
                }
                if (oldItemPosition >= oldList.size() || newItemPosition >= newList.size()) {
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

    @Override
    public void diff(@Nullable final List<?> oldList, @Nullable final List<?> newList) {
        DiffUtil.calculateDiff(getDiffCallback(oldList, newList), true).dispatchUpdatesTo(this);
    }

    @Override
    public void diff(@NonNull final DiffUtil.Callback callback) {
        getDiff(callback).dispatchUpdatesTo(this);
    }

    @Override
    public void diff(@NonNull final DiffUtil.Callback callback, final boolean detectMoves) {
        getDiff(callback, detectMoves).dispatchUpdatesTo(this);
    }

    @NonNull
    private static DiffUtil.DiffResult getDiff(final DiffUtil.Callback callback) {
        return DiffUtil.calculateDiff(callback);
    }

    @NonNull
    private static DiffUtil.DiffResult getDiff(final DiffUtil.Callback callback, final boolean detectMoves) {
        return DiffUtil.calculateDiff(callback, detectMoves);
    }

    public abstract static class DiffAdapter<T> extends DiffUtil.Callback {
        @Nullable
        public final List<T> oldList;

        @Nullable
        public final List<T> newList;

        public DiffAdapter(@Nullable final List<T> oldList, @Nullable final List<T> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList == null ? 0 : oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList == null ? 0 : newList.size();
        }

        @Override
        public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
            try {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Handler for null or empty datasets, showing loading views or empty messages
     * Intended use:
     * - If dataset is null then show a loading view
     * - If dataset is has no items then show a view that explains this to the user
     */
    public interface EmptyViews {
        /**
         * Return a ViewHolder to use when the dataset=null
         * This usually means that data loading is in progress
         */
        BaseViewHolder getNullViewHolder(final View view);

        /**
         * Return a ViewHolder to use when getDataset().isEmpty()
         * This usually means data loading has finished but has no contents
         */
        BaseViewHolder getEmptyViewHolder(final View view);

        int getNullLayoutID();
        int getEmptyLayoutID();

        /**
         * Return the dataset for this adapter
         */
        Collection<?> getDataset();
    }

    public abstract static class EmptyViewsAdapter implements EmptyViews {
        public BaseViewHolder getNullViewHolder(final View v) {
            return new LoadingViewHolder(v);
        }


        public BaseViewHolder getEmptyViewHolder(final View v) {
            return new EmptyViewHolder(v);
        }

        @Override
        public int getNullLayoutID() {
            return R.layout.vh_loading;
        }

        @Override
        public int getEmptyLayoutID() {
            return R.layout.vh_empty;
        }

        private class EmptyViewHolder extends BaseViewHolder {
            EmptyViewHolder(final View v) {
                super(v);
            }

            @Override
            public void bind(final int position) {

            }
        }

        private class LoadingViewHolder extends BaseViewHolder {
            LoadingViewHolder(final View v) {
                super(v);
            }

            @Override
            public void bind(final int position) {

            }
        }
    }

    private static class InvisibleViewHolder extends BaseViewHolder {
        InvisibleViewHolder(final View v) {
            super(v);
        }

        @Override
        public void bind(final int position) {

        }
    }
}
