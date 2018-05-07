package org.beatonma.lib.ui.recyclerview;

import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Michael on 25/08/2017.
 * A container to help deliver {@link DiffUtil.DiffResult} objects from a worker thread
 * back to the main thread.
 *
 * On the main thread, apply the new data to your Adapter via getData(),
 * then call one of the dispatchUpdatesTo() methods to apply changes.
 */
@SuppressWarnings("unused")
public class AsyncDiffResult<D> {
    private DiffUtil.DiffResult mResult;
    private D mData;

    public AsyncDiffResult(final D newData, final DiffUtil.Callback callback) {
        mResult = DiffUtil.calculateDiff(callback);
        mData = newData;
    }

    public AsyncDiffResult(final D newData,
                           final DiffUtil.Callback callback,
                           final boolean detectMoves) {
        mResult = DiffUtil.calculateDiff(callback, detectMoves);
        mData = newData;
    }

    public D getData() {
        return mData;
    }

    public void dispatchUpdatesTo(final RecyclerView.Adapter adapter) {
        mResult.dispatchUpdatesTo(adapter);
    }

    public void dispatchUpdatesTo(final ListUpdateCallback callback) {
        mResult.dispatchUpdatesTo(callback);
    }
}
