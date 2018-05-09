package org.beatonma.lib.ui.recyclerview;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Michael on 04/07/2016.
 */
public abstract class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final static int DRAG_ELEVATION = 4;
    private final static int DRAG_MARGIN = 16;

    private final static int SWIPE_ELEVATION = 2;
    private final static int SWIPE_MARGIN = 4;

    private BaseViewHolder selectedViewHolder;
    private int previousActionState = ItemTouchHelper.ACTION_STATE_IDLE;
    private final int dp;

    public SimpleItemTouchHelperCallback(View v) {
        this((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, v.getResources().getDisplayMetrics()));
    }

    private SimpleItemTouchHelperCallback(int dp) {
        this.dp = dp;
    }

    @Override
    public int getMovementFlags(androidx.recyclerview.widget.RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public void onSelectedChanged(androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        if (viewHolder != null) {
            selectedViewHolder = (BaseViewHolder) viewHolder;
            selectedViewHolder.lift();
        }
        else {
            if (selectedViewHolder != null) {
                selectedViewHolder.drop();
            }
            selectedViewHolder = null;
        }

        previousActionState = actionState;
    }

    /**
     *
     * @param recyclerView
     * @param adapter
     * @param swipeable
     * @param moveable
     * @return
     */
    public static SimpleItemTouchHelperCallback getSimpleCallback(View recyclerView, final BaseRecyclerViewAdapter adapter, final boolean swipeable, final boolean moveable) {
        return new SimpleItemTouchHelperCallback(recyclerView) {
            @Override
            public boolean isLongPressDragEnabled() {
                return moveable;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return swipeable;
            }

            @Override
            public boolean onMove(final androidx.recyclerview.widget.RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(final androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, final int direction) {
                adapter.onItemDismiss(viewHolder.getAdapterPosition());
            }
        };
    }

    public static class Builder {
        final View recyclerView;
        final BaseRecyclerViewAdapter adapter;
        int swipeFlags;
        int dragFlags;

        public Builder(View recyclerView, BaseRecyclerViewAdapter adapter) {
            this.recyclerView = recyclerView;
            this.adapter = adapter;
        }

        /**
         * @param swipeLeft     Allow swiping to the left
         * @param swipeRight    Allow swiping to the right
         */
        public Builder swipe(boolean swipeLeft, boolean swipeRight) {
            swipeFlags = 0;
            swipeFlags = swipeFlags | (swipeLeft ? ItemTouchHelper.START : 0);
            swipeFlags = swipeFlags | (swipeRight ? ItemTouchHelper.END : 0);
            return this;
        }

        /**
         * @param dragUp      Allow dragging up
         * @param dragDown    Allow dragging down
         * @return
         */
        public Builder drag(boolean dragUp, boolean dragDown) {
            dragFlags = 0;
            dragFlags = dragFlags | (dragUp ? ItemTouchHelper.UP : 0);
            dragFlags = dragFlags | (dragDown ? ItemTouchHelper.DOWN : 0);
            return this;
        }

        /**
         * Enable swiping to both sides
         */
        public Builder swipe() {
            swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return this;
        }

        /**
         * Enable dragging both up and down
         */
        public Builder drag() {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return this;
        }

        public SimpleItemTouchHelperCallback build() {
            return new SimpleItemTouchHelperCallback(recyclerView) {
                @Override
                public int getMovementFlags(RecyclerView recyclerView, androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder) {
                    return makeMovementFlags(dragFlags, swipeFlags);
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    return dragFlags != 0;
                }

                @Override
                public boolean isItemViewSwipeEnabled() {
                    return swipeFlags != 0;
                }

                @Override
                public boolean onMove(final RecyclerView recyclerView, final androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, final androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                    adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    return true;
                }

                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
                    adapter.onItemDismiss(viewHolder.getAdapterPosition());
                }
            };
        }
    }


    public interface ItemTouchHelperAdapter {
        void onItemDismiss(int position);
        boolean onItemMove(int fromPosition, int toPosition);
    }
}