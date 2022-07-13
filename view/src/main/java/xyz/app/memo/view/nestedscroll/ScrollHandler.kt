package xyz.app.memo.view.nestedscroll

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.recyclerview.widget.RecyclerView
import xyz.app.memo.view.R
import kotlin.math.abs

/**
 * 嵌套滑动具体处理类
 * Created by zxx on 2022/7/6 16:56:55
 */
class ImageScaleScrollHandler(var headerView: View): NestedScrollingParent.ScrollHandler {
    private var targetViewH: Int = 0    // 设置的RecyclerView高度
    private var scrollHeight = 0 // 父View最多滑动距离
    private var headerInitHeight = 0 // 可滚动View初始大小
    private var parentView: View? = null
    override fun getCanScrollHeight(): Int {
        return scrollHeight
    }

    override fun onScroll(dy: Int): Int {
        if (dy >= 0) {
            if (targetViewH == 0) {
                // 调整RecyclerView高度使其充满屏幕
                val targetView = parentView?.findViewById<RecyclerView>(R.id.recycleView)
                println("recyclerView Height=${targetView!!.measuredHeight} t=${targetView!!.top} b=${targetView.bottom} scrollHeight=$scrollHeight")
                targetViewH = targetView.measuredHeight + scrollHeight
                updateHeight(targetView!!, targetViewH)
            }

            val headerChangedSize = headerInitHeight - headerView.measuredHeight
            if (headerChangedSize < scrollHeight) {
                val remainingY = scrollHeight - headerChangedSize //剩余可滑动距离，or 剩余的头部image可缩小的size
                val consumeY = if (dy > remainingY) remainingY else dy
                val height = headerView.measuredHeight - consumeY
                if (height >= headerInitHeight - scrollHeight) {
                    // 调整Image大小
                    updateHeight(headerView, height)
                }
                parentView?.scrollBy(0, consumeY)
                return consumeY
            }
        } else {
            val headerSize = headerView.measuredHeight
            if (headerSize < headerInitHeight) {
                var height = headerSize - dy
                height = if (height <= headerInitHeight) height else headerInitHeight
                updateHeight(headerView, height)
            }
            // parent滚动
            val scrollY = parentView?.scrollY ?: 0
            if (abs(dy) < scrollY) parentView!!.scrollBy(0, dy)
            else parentView!!.scrollBy(0, -1 * scrollY)
            return dy
        }
        return 0
    }

    override fun onFinishInflate(parent: ViewGroup) {
        parentView = parent
        headerView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 保存头图原始尺寸
                headerInitHeight = headerView.measuredHeight
                // 根据头图最大可缩小比例，计算可滚动距离
                scrollHeight = (headerInitHeight * 0.5f).toInt()
                headerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (headerInitHeight > 0) {
                    // 同步设置占位透明view的高度
                    updateHeight(parent!!.getChildAt(0), headerInitHeight)
                }
            }
        })
    }

    private fun updateHeight(view: View, height: Int) {
        val lp = view.layoutParams
        lp.height = height
        view.layoutParams = lp
    }
}

class TopScrollHandler: NestedScrollingParent.ScrollHandler {
    private var scrollHeight = 0 // 父View最多滑动距离
    private var headerView: View? = null
    private var targetView: View? = null
    private var parentView: View? = null

    override fun getCanScrollHeight(): Int {
        return scrollHeight
    }

    override fun onScroll(dy: Int): Int {
        if (dy >= 0) {
            if (headerView != null) {
                val headerChangedSize = parentView?.scrollY ?: 0
                if (headerChangedSize < scrollHeight) {
                    val remainingY = scrollHeight - headerChangedSize //剩余可滑动距离
                    val consumeY = if (dy > remainingY) remainingY else dy
                    parentView?.scrollBy(0, consumeY)
                    return consumeY
                }
            }
        } else {
            val scrollY = parentView?.scrollY ?: 0
            if (abs(dy) < scrollY) parentView!!.scrollBy(0, dy)
            else parentView!!.scrollBy(0, -1 * scrollY)
            return dy
        }
        return 0
    }

    override fun onFinishInflate(parent: ViewGroup) {
        parentView = parent
        targetView = parent.findViewById(R.id.recycleView)
        headerView = parent.getChildAt(0)
        headerView?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                println("onGlobalLayout")
                scrollHeight = headerView?.measuredHeight ?: 0
                headerView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                println("scrollHeight2=$scrollHeight targetView=${targetView?.measuredHeight}")
                // 调整RecyclerView高度使其充满屏幕
                val lp = targetView!!.layoutParams
                lp.height = targetView!!.measuredHeight + scrollHeight
                targetView!!.layoutParams = lp
            }
        })
    }

}