package org.beatonma.lib.ui.recyclerview.itemanimator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.view.Gravity;
import android.view.View;

import org.beatonma.lib.ui.style.Interpolate;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Michael on 24/06/2016.
 */
public class SlideInItemAnimator extends DefaultItemAnimator {
    private final static String TAG = "SlideInItemAnimator";

    private final List<RecyclerView.ViewHolder> pendingAdds = new ArrayList<>();
    private final List<RecyclerView.ViewHolder> pendingRemoves = new ArrayList<>();
    private final int slideFromEdge;
    private final int itemDelayMs;

    private final TimeInterpolator enterInterpolator = Interpolate.getEnterInterpolator();
    private final TimeInterpolator exitInterpolator = Interpolate.getExitInterpolator();

    /**
     * Default to sliding in upward.
     */
    public SlideInItemAnimator() {
        this(Gravity.BOTTOM, -1, 20); // undefined layout dir; bottom isn't relative
    }

    public SlideInItemAnimator(final int itemDelayMs) {
        this(Gravity.BOTTOM, -1, itemDelayMs);
    }

    public SlideInItemAnimator(int slideFromEdge, int layoutDirection) {
        this(slideFromEdge, layoutDirection, 20);
    }

    public SlideInItemAnimator(final int slideFromEdge, final int layoutDirection, final int itemDelayMs) {
        this.slideFromEdge = Gravity.getAbsoluteGravity(slideFromEdge, layoutDirection);
        setAddDuration(160L);
        setRemoveDuration(120L);
        this.itemDelayMs = itemDelayMs;
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        holder.itemView.setAlpha(0f);
        switch (slideFromEdge) {
            case Gravity.LEFT:
            case Gravity.START:
                holder.itemView.setTranslationX(-holder.itemView.getWidth() / 3);
                break;
            case Gravity.TOP:
                holder.itemView.setTranslationY(-holder.itemView.getHeight() / 3);
                break;
            case Gravity.RIGHT:
            case Gravity.END:
                holder.itemView.setTranslationX(holder.itemView.getWidth() / 3);
                break;
            default: // Gravity.BOTTOM
                holder.itemView.setTranslationY(holder.itemView.getHeight() / 3);
        }
        pendingAdds.add(holder);
        return true;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        pendingRemoves.add(holder);
        return true;
    }

    @Override
    public void runPendingAnimations() {
        super.runPendingAnimations();
        if (!pendingAdds.isEmpty()) {
            for (int i = pendingAdds.size() - 1; i >= 0; i--) {
                final RecyclerView.ViewHolder holder = pendingAdds.get(i);
                holder.itemView.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .translationY(0f)
                        .setDuration(getAddDuration())
                        .setStartDelay(holder.getAdapterPosition() * itemDelayMs)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                dispatchAddStarting(holder);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animation.getListeners().remove(this);
                                dispatchAddFinished(holder);
                                dispatchFinishedWhenDone();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                clearAnimatedValues(holder.itemView);
                            }
                        })
                        .setInterpolator(enterInterpolator)
                        .start();
                pendingAdds.remove(i);
            }
        }
        if (!pendingRemoves.isEmpty()) {
            for (int i = pendingRemoves.size() - 1; i >= 0; i--) {
                final androidx.recyclerview.widget.RecyclerView.ViewHolder holder = pendingRemoves.get(i);
                holder.itemView.animate()
                        .alpha(0f)
                        .setDuration(getRemoveDuration())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                dispatchRemoveStarting(holder);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animation.getListeners().remove(this);
                                dispatchRemoveFinished(holder);
                                dispatchFinishedWhenDone();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                clearAnimatedValues(holder.itemView);
                            }
                        })
                        .setInterpolator(exitInterpolator)
                        .start();
                pendingRemoves.remove(i);
            }
        }
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder holder) {
        holder.itemView.animate().cancel();
        if (pendingAdds.remove(holder)) {
            dispatchAddFinished(holder);
            clearAnimatedValues(holder.itemView);
        }
        if (pendingRemoves.remove(holder)) {
            dispatchRemoveFinished(holder);
            clearAnimatedValues(holder.itemView);
        }
        super.endAnimation(holder);
    }

    @Override
    public void endAnimations() {
        for (int i = pendingAdds.size() - 1; i >= 0; i--) {
            final RecyclerView.ViewHolder holder = pendingAdds.get(i);
            clearAnimatedValues(holder.itemView);
            dispatchAddFinished(holder);
            pendingAdds.remove(i);
        }
        for (int i = pendingRemoves.size() - 1; i >= 0; i--) {
            final RecyclerView.ViewHolder holder = pendingRemoves.get(i);
            clearAnimatedValues(holder.itemView);
            dispatchRemoveFinished(holder);
            pendingRemoves.remove(i);
        }
        super.endAnimations();
    }

    @Override
    public boolean isRunning() {
        return !pendingAdds.isEmpty() || super.isRunning();
    }

    private void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    private void clearAnimatedValues(final View view) {
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
    }
}