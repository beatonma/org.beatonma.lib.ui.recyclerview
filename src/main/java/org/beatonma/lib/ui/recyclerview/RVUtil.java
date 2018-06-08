package org.beatonma.lib.ui.recyclerview;

import org.beatonma.lib.log.Log;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class RVUtil {
    private final static String TAG = "RecyclerViewUtil";
    public static void setup(@NonNull final RecyclerView recyclerView,
                             @NonNull final RecyclerView.Adapter adapter) {
        setup(recyclerView, adapter,
                new LinearLayoutManager(recyclerView.getContext()));
    }

    public static void setup(@NonNull final RecyclerView recyclerView,
                             @NonNull final RecyclerView.Adapter adapter,
                             @NonNull final RecyclerView.LayoutManager layoutManager) {
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public static void setup(@NonNull final RecyclerView recyclerView,
                             @NonNull final BaseRecyclerViewAdapter adapter) {
        setup(recyclerView, adapter,
                new LinearLayoutManager(recyclerView.getContext()));
        adapter.setupWithTouchHelper(recyclerView);
    }

    public static void setupGrid(@NonNull final RecyclerView recyclerView,
                                 @NonNull final BaseRecyclerViewAdapter adapter,
                                 final int columnWidth) {
        setup(recyclerView, adapter,
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        final WeakReference<RecyclerView> weakRv = new WeakReference<>(recyclerView);

        recyclerView.post(() -> {
            final RecyclerView recyclerView1 = weakRv.get();
            if (recyclerView1 == null) {
                return;
            }

            try {
                final int columnCount = Math.max(1, (int) Math.floor(recyclerView1.getMeasuredWidth() / columnWidth));
                final StaggeredGridLayoutManager lm = ((StaggeredGridLayoutManager) recyclerView1.getLayoutManager());
                lm.setSpanCount(columnCount);
            } catch (final Exception e) {
                Log.e(TAG, "Error updating StaggeredGridLayoutManager column count: " + e.toString());
            }
        });
    }
}
