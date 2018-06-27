package org.beatonma.lib.ui.recyclerview.itemanimator

/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import org.beatonma.lib.ui.style.Interpolate

/**
 * An extensible Clone of [androidx.recyclerview.widget.DefaultItemAnimator] with
 * overridable animation 'Impl' methods:
 * - animateAddImpl
 * - animateChangeImpl
 * - animateMoveImpl
 * - animateRemoveImpl
 */
abstract class BaseItemAnimator(
        addDuration: Long = DEFAULT_DURATION,
        removeDuration: Long = DEFAULT_DURATION,
        changeDuration: Long = DEFAULT_DURATION,
        moveDuration: Long = DEFAULT_DURATION,
        val enterInterpolator: TimeInterpolator = Interpolate.getEnterInterpolator(),
        val exitInterpolator: TimeInterpolator = Interpolate.getExitInterpolator(),
        val changeInterpolator: TimeInterpolator = Interpolate.getMotionInterpolator()
) : SimpleItemAnimator() {

    protected fun View.widthF(): Float {
        return width.toFloat()
    }

    protected fun View.heightF(): Float {
        return height.toFloat()
    }

    companion object {
        private val TAG = "BaseItemAnimator"
        private val DEBUG = false
        const val DEFAULT_DURATION = 2000L
//
//        @NonNull
//        private var sDefaultInterpolator: TimeInterpolator? = null
    }

    protected val mPendingRemovals = ArrayList<RecyclerView.ViewHolder>()
    protected val mPendingAdditions = ArrayList<RecyclerView.ViewHolder>()
    protected val mPendingMoves = ArrayList<MoveInfo>()
    protected val mPendingChanges = ArrayList<ChangeInfo>()

    protected val mAddAnimations = ArrayList<RecyclerView.ViewHolder>()
    protected val mMoveAnimations = ArrayList<RecyclerView.ViewHolder>()
    protected val mRemoveAnimations = ArrayList<RecyclerView.ViewHolder>()
    protected val mChangeAnimations = ArrayList<RecyclerView.ViewHolder>()

    class MoveInfo(val holder: RecyclerView.ViewHolder,
                   val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)

    class ChangeInfo(var oldHolder: RecyclerView.ViewHolder?,
                     var newHolder: RecyclerView.ViewHolder?,
                     val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)

    init {
        supportsChangeAnimations = true
        this.addDuration = addDuration
        this.moveDuration = moveDuration
        this.removeDuration = removeDuration
        this.changeDuration = changeDuration
    }

    @CallSuper
    open fun getAddAnimator(holder: RecyclerView.ViewHolder?): Animator? {
        holder?.addTo(mAddAnimations)
        return null
    }

    @CallSuper
    open fun getRemoveAnimator(holder: RecyclerView.ViewHolder?): Animator? {
        holder?.addTo(mRemoveAnimations)
        return null
    }

    @CallSuper
    open fun getMoveAnimator(holder: RecyclerView.ViewHolder?): Animator? {
        holder?.addTo(mMoveAnimations)
        return null
    }

    @CallSuper
    open fun getChangeAnimator(changeInfo: ChangeInfo): Animator? {
        changeInfo.oldHolder?.addTo(mChangeAnimations)
        return null
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        resetAnimation(holder)
        mPendingRemovals.add(holder)
        return onAnimateRemove(holder)
    }

    /**
     * Prepare viewholder for removal
     */
    protected open fun onAnimateRemove(holder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        resetAnimation(holder)
        mPendingAdditions.add(holder)
        return onAnimateAdd(holder)
    }

    /**
     * Prepare viewholder for being added
     */
    protected open fun onAnimateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.alpha = 0f
        return true
    }

    override fun animateMove(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int,
                             toX: Int, toY: Int): Boolean {
        mPendingMoves.add(MoveInfo(holder, fromX, fromY, toX, toY))
        return onAnimateMove(holder, fromX, fromY, toX, toY)
    }

    /**
     * Prepare viewholder for being moved
     */
    protected open fun onAnimateMove(holder: RecyclerView.ViewHolder,
                                     _fromX: Int, _fromY: Int,
                                     toX: Int, toY: Int): Boolean {
        val view = holder.itemView
        val fromX = _fromX + holder.itemView.translationX.toInt()
        val fromY = _fromX + holder.itemView.translationY.toInt()
        resetAnimation(holder)
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (deltaX != 0) {
            view.translationX = (-deltaX).toFloat()
        }
        if (deltaY != 0) {
            view.translationY = (-deltaY).toFloat()
        }
        return true
    }

    override fun animateChange(oldHolder: RecyclerView.ViewHolder?,
                               newHolder: RecyclerView.ViewHolder?,
                               fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {

        if (onAnimateChange(oldHolder, newHolder, fromX, fromY, toX, toY)) {
            mPendingChanges.add(ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY))
            return true
        }
        return false
    }

    /**
     * Prepare viewholder for being changed
     */
    protected open fun onAnimateChange(oldHolder: RecyclerView.ViewHolder?,
                                       newHolder: RecyclerView.ViewHolder?,
                                       fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        val prevTranslationX = oldHolder?.itemView?.translationX ?: 0F
        val prevTranslationY = oldHolder?.itemView?.translationY ?: 0F
        val prevAlpha = oldHolder?.itemView?.alpha ?: 0F
        resetAnimation(oldHolder)
        val deltaX = (toX.toFloat() - fromX.toFloat() - prevTranslationX).toInt()
        val deltaY = (toY.toFloat() - fromY.toFloat() - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        oldHolder?.itemView?.apply {
            translationX = prevTranslationX
            translationY = prevTranslationY
            alpha = prevAlpha
        }

        if (newHolder != null) {
            // carry over translation values
            resetAnimation(newHolder)
            newHolder.itemView.apply {
                translationX = (-deltaX).toFloat()
                translationY = (-deltaY).toFloat()
                alpha = 0f
            }
        }
        return true
    }

    private fun endChangeAnimation(infoList: MutableList<ChangeInfo>,
                                   item: RecyclerView.ViewHolder) {
        for (i in infoList.indices.reversed()) {
            val changeInfo = infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder)
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo,
                                              item: RecyclerView.ViewHolder?): Boolean {
        var oldItem = false
        when {
            changeInfo.newHolder === item -> changeInfo.newHolder = null
            changeInfo.oldHolder === item -> {
                changeInfo.oldHolder = null
                oldItem = true
            }
            else -> return false
        }
        item?.itemView?.apply {
            alpha = 1f
            translationX = 0f
            translationY = 0f
        }
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        view.animate().cancel()
        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (i in mPendingMoves.indices.reversed()) {
            val moveInfo = mPendingMoves[i]
            if (moveInfo.holder === item) {
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(item)
                mPendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(mPendingChanges, item)
        if (mPendingRemovals.remove(item)) {
            view.alpha = 1f
            dispatchRemoveFinished(item)
        }
        if (mPendingAdditions.remove(item)) {
            view.alpha = 1f
            dispatchAddFinished(item)
        }

        // animations should be ended by the cancel above.
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (mRemoveAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mRemoveAnimations list")
        }

        //noinspection PointlessBooleanExpression,ConstantConditions
        if (mAddAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mAddAnimations list")
        }

        //noinspection PointlessBooleanExpression,ConstantConditions
        if (mChangeAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mChangeAnimations list")
        }

        //noinspection PointlessBooleanExpression,ConstantConditions
        if (mMoveAnimations.remove(item) && DEBUG) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mMoveAnimations list")
        }
        dispatchFinishedWhenDone()
    }

    private fun resetAnimation(holder: RecyclerView.ViewHolder?) {
        if (holder != null) {
            holder.itemView.animate().interpolator = changeInterpolator
            endAnimation(holder)
        }
    }

    override fun isRunning(): Boolean {
        return (!mPendingAdditions.isEmpty()
                || !mPendingChanges.isEmpty()
                || !mPendingMoves.isEmpty()
                || !mPendingRemovals.isEmpty()
                || !mMoveAnimations.isEmpty()
                || !mRemoveAnimations.isEmpty()
                || !mAddAnimations.isEmpty()
                || !mChangeAnimations.isEmpty())
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call [.dispatchAnimationsFinished] to notify any
     * listeners.
     */
    protected fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimations() {
        var count = mPendingMoves.size
        for (i in count - 1 downTo 0) {
            val item = mPendingMoves[i]
            val view = item.holder.itemView
            view.translationY = 0f
            view.translationX = 0f
            dispatchMoveFinished(item.holder)
            mPendingMoves.removeAt(i)
        }
        count = mPendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = mPendingRemovals[i]
            dispatchRemoveFinished(item)
            mPendingRemovals.removeAt(i)
        }
        count = mPendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = mPendingAdditions[i]
            item.itemView.alpha = 1f
            dispatchAddFinished(item)
            mPendingAdditions.removeAt(i)
        }
        count = mPendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(mPendingChanges[i])
        }
        mPendingChanges.clear()
        if (!isRunning) {
            return
        }

        cancelAll(mRemoveAnimations)
        cancelAll(mMoveAnimations)
        cancelAll(mAddAnimations)
        cancelAll(mChangeAnimations)

        dispatchAnimationsFinished()
    }

    protected fun cancelAll(viewHolders: List<RecyclerView.ViewHolder>) {
        for (i in viewHolders.indices.reversed()) {
            viewHolders[i].itemView.animate().cancel()
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     * If the payload list is not empty, DefaultItemAnimator returns `true`.
     * When this is the case:
     *
     *  * If you override [.animateChange], both
     * ViewHolder arguments will be the same instance.
     *
     *  *
     * If you are not overriding [.animateChange],
     * then DefaultItemAnimator will call [.animateMove] and
     * run a move animation instead.
     *
     *
     */

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder,
                                           payloads: MutableList<Any>): Boolean {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads)
    }

    open inner class AddAnimatorListenerAdapter(private val holder: RecyclerView.ViewHolder) : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            dispatchAddStarting(holder)
        }

        override fun onAnimationCancel(animation: Animator?) {
            super.onAnimationCancel(animation)
            holder.itemView.apply {
                alpha = 1F
                translationX = 0F
                translationY = 0F
            }
        }

        override fun onAnimationEnd(animation: Animator?) {
            animation?.removeListener(this)
            dispatchAddFinished(holder)
            holder.removeFrom(mAddAnimations)
            holder.itemView.apply {
                alpha = 1F
                translationX = 0F
                translationY = 0F
            }
            dispatchFinishedWhenDone()
        }
    }

    open inner class RemoveAnimatorListenerAdapter(private val holder: RecyclerView.ViewHolder) : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            dispatchRemoveStarting(holder)
        }

        override fun onAnimationCancel(animation: Animator?) {
            super.onAnimationCancel(animation)
            holder.itemView.apply {
                alpha = 0F
                translationX = 0F
                translationY = 0F
            }
        }

        override fun onAnimationEnd(animation: Animator?) {
            animation?.removeListener(this)
            dispatchRemoveFinished(holder)
            holder.removeFrom(mRemoveAnimations)
            holder.itemView.apply {
                alpha = 0F
                translationX = 0F
                translationY = 0F
            }
            dispatchFinishedWhenDone()
        }
    }

    open inner class ChangeAnimatorListenerAdapter(private val changeInfo: ChangeInfo) : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            dispatchChangeStarting(changeInfo.oldHolder, true)
            dispatchChangeStarting(changeInfo.newHolder, false)
        }

        override fun onAnimationCancel(animation: Animator?) {
            changeInfo.newHolder?.itemView?.alpha = 1F
            changeInfo.oldHolder?.itemView?.alpha = 0F
        }

        override fun onAnimationEnd(animation: Animator?) {
            changeInfo.oldHolder?.apply {
                itemView.alpha = 0F
                itemView.translationX = 0F
                itemView.translationY = 0F
                dispatchChangeFinished(this, true)
                removeFrom(mChangeAnimations)
            }
            changeInfo.newHolder?.apply {
                itemView.alpha = 1F
                itemView.translationX = 0F
                itemView.translationY = 0F
                dispatchChangeFinished(this, false)
            }
            animation?.removeListener(this)
            dispatchFinishedWhenDone()
        }
    }
}

/**
 * Null-safe way to add an item to a list
 */
internal fun <T> T.addTo(list: MutableList<T>) {
    list.add(this)
}

/**
 * Null-safe way to remove an item from a list
 */
internal fun <T> T.removeFrom(list: MutableList<T>) {
    list.remove(this)
}