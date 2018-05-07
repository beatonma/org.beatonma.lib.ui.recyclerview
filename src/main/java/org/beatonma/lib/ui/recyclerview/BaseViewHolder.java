package org.beatonma.lib.ui.recyclerview;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.beatonma.lib.recyclerview.R;

/**
 * Created by Michael on 14/07/2016.
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    private final static String TAG = "BaseViewHolder";

    private ValueAnimator mAnimator;
    private final TimeInterpolator mInterpolator = new FastOutSlowInInterpolator();
    private final RectF mBaseMarginSize = new RectF(-1, -1, -1, -1);

    protected final float liftMargin;
    protected final float liftElevation;

    public BaseViewHolder(View v) {
        super(v);
        final Resources res = v.getResources();
        liftMargin = res.getDimension(R.dimen.selected_item_margin);
        liftElevation = res.getDimension(R.dimen.selected_item_raise_elevation);
    }

    public abstract void bind(int position);

    private void initMargins() {
        if (mBaseMarginSize.left == -1) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            mBaseMarginSize.set(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
        }
    }

    public void lift() {
        lift(liftMargin, liftElevation);
    }

    public void drop() {
        drop(liftMargin, liftElevation);
    }

    public void lift(final float margin, final float elevation) {
        initMargins();

        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.addUpdateListener(animation -> {
            float progress = animation.getAnimatedFraction();
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            float extraMarginSize = progress * margin;
            lp.topMargin = (int) (mBaseMarginSize.top + extraMarginSize);
            lp.bottomMargin = (int) (mBaseMarginSize.bottom + extraMarginSize);
            itemView.setLayoutParams(lp);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setTranslationZ(progress * elevation);
            }
        });
        mAnimator.setInterpolator(mInterpolator);
        mAnimator.start();
    }

    public void drop(final float margin, final float elevation) {
        initMargins();

        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.addUpdateListener(animation -> {
            float progress = 1f - animation.getAnimatedFraction();
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            float extraMarginSize = progress * margin;
            lp.topMargin = (int) (mBaseMarginSize.top + extraMarginSize);
            lp.bottomMargin = (int) (mBaseMarginSize.bottom + extraMarginSize);
            itemView.setLayoutParams(lp);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setTranslationZ(progress * elevation);
            }
        });
        mAnimator.setInterpolator(mInterpolator);
        mAnimator.start();
    }
}
