package xyz.app.memo.view.nestedscroll

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import android.widget.OverScroller
import androidx.core.view.NestedScrollingChild2
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

/**
 * 嵌套滑动的子布局
 * Created by zxx on 2022/6/15 18:00:06
 */
class NestedScrollingChild @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NestedScrollingChild2 {
    private val mChildHelper = NestedScrollingChildHelper(this)
    private var mLastFlingY: Int = 0
    private var mTouchDownY = 0
    private var mScrollOffset = IntArray(2)
    private var mScrollConsumed = IntArray(2)
    private var mViewHeight = 0     // 初始View的高度
    private var mContentHeight = 0  // 实际内容高度
    private var mTotalScrollY = 0   // 最多能滑动的距离

    // 处理fling
    private val mVelocityTracker = VelocityTracker.obtain()
    private var mViewConfiguration: ViewConfiguration = ViewConfiguration.get(context)
    private val mOverScroller: OverScroller = OverScroller(context)

    private var loadMoreCallback: (() -> Unit)? = null  // 滚动到底部的回调函数

    init {
        mChildHelper.isNestedScrollingEnabled = true
        setOnScrollChangeListener(object : OnScrollChangeListener {
            override fun onScrollChange(
                v: View?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                if (scrollY + 20 >= mTotalScrollY) {
                    loadMoreCallback?.invoke()
                }
            }
        })
    }

    fun setOnScrollEnd(callback: () -> Unit) {
        this.loadMoreCallback = callback
    }

    /**
     * 父View可滚动距离，父View滚动时子View的可用高度会变多，相应的子View可滚动高度要减少
     */
    fun setParentScrollHeight(parentScrollHeight: Int) {
        mTotalScrollY -= parentScrollHeight
        mTotalScrollY = if (mTotalScrollY < 0) 0 else mTotalScrollY
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mViewHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            mViewHeight = measuredHeight
            println("first onMeasure measuredHeight=$mViewHeight")
        } else {
            val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
            if (mTotalScrollY == 0) {
                mContentHeight = measuredHeight
                mTotalScrollY = measuredHeight - mViewHeight
                println("second onMeasure measuredHeight=$measuredHeight")
                println("second onMeasure mTotalScrollY=$mTotalScrollY")
            } else {
                if (mContentHeight != measuredHeight) {
                    mTotalScrollY += measuredHeight - mContentHeight
                    mContentHeight = measuredHeight
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
            mVelocityTracker.clear()
        }
        mVelocityTracker.addMovement(event)

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchDownY = event!!.rawY.toInt()
                // 使用getY()会抖动
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }
            MotionEvent.ACTION_MOVE -> {
                var deltaY = mTouchDownY - event.rawY.toInt()
                mTouchDownY = event!!.rawY.toInt()
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                    deltaY -= mScrollConsumed[1]
                }
                scrollBy(0, deltaY)
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker.computeCurrentVelocity(1000, mViewConfiguration.scaledMaximumFlingVelocity.toFloat())
                val initialVelocity = mVelocityTracker.yVelocity.toInt()
                if (abs(initialVelocity) > mViewConfiguration.scaledMinimumFlingVelocity) {
                    if (!dispatchNestedPreFling(0f, -initialVelocity.toFloat())) {
                        dispatchNestedFling(0f, -initialVelocity.toFloat(), true)
                        fling(-initialVelocity)
                    }
                }

                stopNestedScroll(ViewCompat.TYPE_TOUCH)
            }
            MotionEvent.ACTION_CANCEL -> {
                stopNestedScroll(ViewCompat.TYPE_TOUCH)
            }
        }
        return true
    }

    override fun computeScroll() {
        val v = mOverScroller.computeScrollOffset()
        println("computeScroll offset=$v mLastFlingY=$mLastFlingY")
        if (mOverScroller.computeScrollOffset()) {
            val y = mOverScroller.currY
            var dy = y - mLastFlingY
            mLastFlingY = y
            if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_NON_TOUCH)) {
                dy -= mScrollConsumed[1]
            }
            println("dy=$dy")
            scrollBy(0, dy)
        } else {
            stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
        }
    }

    private fun fling(velocityY: Int) {
        if (childCount > 0) {
            mOverScroller.fling(
                scrollX, scrollY,  // start
                0, velocityY,  // velocities
                0, 0, Int.MIN_VALUE, Int.MAX_VALUE,  // y
                0, 0
            ) // overscroll
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
            mLastFlingY = scrollY
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return mChildHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        mChildHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return mChildHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }


    override fun scrollTo(x: Int, y: Int) {
        var toY = y
        toY = if (toY < 0) 0 else toY
        toY = if (toY > this.mTotalScrollY) this.mTotalScrollY else toY
        super.scrollTo(x, toY)
    }
}