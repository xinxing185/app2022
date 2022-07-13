package xyz.app.memo.view.nestedscroll

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParentHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * 嵌套滑动的父布局
 * Created by zxx on 2022/6/15 17:53:46
 */
class NestedScrollingParent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NestedScrollingParent2 {
    private val tag = "Parent"

    private var targetInitHeight = 0

    private var mParentHelper: NestedScrollingParentHelper = NestedScrollingParentHelper(this)
    private var mScrollHandler: ScrollHandler? = null
    private var isInflated = false

    fun setScrollHandler(scrollHandler: ScrollHandler) {
        this.mScrollHandler = scrollHandler
        if (isInflated) {
            mScrollHandler!!.onFinishInflate(this)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        isInflated = true
        mScrollHandler?.onFinishInflate(this)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if (targetInitHeight == 0) {
            if (target is NestedScrollingChild) {
                target.setParentScrollHeight(mScrollHandler?.getCanScrollHeight() ?: 0)
            }
            targetInitHeight = target.measuredHeight
        }
        return axes == SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun getNestedScrollAxes(): Int {
        return mParentHelper.nestedScrollAxes
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mParentHelper.onStopNestedScroll(target, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (dy >= 0) {
            // 手指向上，向下滑动
            consumed[1] = mScrollHandler?.onScroll(dy) ?: 0
        } else {
            // 手指向下，向上滑动
            var scrollY = if (target is RecyclerView) {
                target.computeVerticalScrollOffset()
            } else {
                target.scrollY
            }
            if (scrollY <= 0) {
                // 子View已滑到顶部，dy全部由父View消耗
                consumed[1] = mScrollHandler?.onScroll(dy) ?: 0
            }
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        val canScrollHeight = mScrollHandler?.getCanScrollHeight() ?: 0
        var safeY = if (y < 0) 0 else y
        safeY = if (safeY > canScrollHeight) canScrollHeight else y
        super.scrollTo(x, safeY)
    }

    interface ScrollHandler {
        fun onScroll(dy: Int): Int
        fun onFinishInflate(parent: ViewGroup)
        fun getCanScrollHeight(): Int
    }
}