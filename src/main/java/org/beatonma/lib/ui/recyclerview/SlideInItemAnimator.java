package org.beatonma.lib.ui.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 24/06/2016.
 */
public class SlideInItemAnimator extends DefaultItemAnimator {
    private final static String TAG = "SlideInItemAnimator";

    private final List<RecyclerView.ViewHolder> pendingAdds = new ArrayList<>();
    private final List<RecyclerView.ViewHolder> pendingRemoves = new ArrayList<>();
    private final int slideFromEdge;

    /**
     * Default to sliding in upward.
     */
    public SlideInItemAnimator() {
        this(Gravity.BOTTOM, -1); // undefined layout dir; bottom isn't relative
    }

    public SlideInItemAnimator(int slideFromEdge, int layoutDirection) {
        this.slideFromEdge = Gravity.getAbsoluteGravity(slideFromEdge, layoutDirection);
        setAddDuration(160L);
        setRemoveDuration(120L);
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
                        .setStartDelay((int) (holder.getAdapterPosition() * getAddDuration() * 0.2f))
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
//                        .setInterpolator(AnimationUtils.getEnterInterpolator())
                        .start();
                pendingAdds.remove(i);
            }
        }
        if (!pendingRemoves.isEmpty()) {
            for (int i = pendingRemoves.size() - 1; i >= 0; i--) {
                final RecyclerView.ViewHolder holder = pendingRemoves.get(i);
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
//                        .setInterpolator(AnimationUtils.getExitInterpolator())
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
//            clearAnimatedValues(holder.itemView);
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
//            clearAnimatedValues(holder.itemView);
            dispatchRemoveFinished(holder);
            pendingRemoves.remove(i);
        }
        super.endAnimations();
    }

    @Override
    public boolean isRunning() {
        return (!pendingAdds.isEmpty() && !pendingAdds.isEmpty()) || super.isRunning();
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