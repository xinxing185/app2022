package xyz.app.memo.view.nestedscroll

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.app.memo.view.R
import xyz.app.memo.view.databinding.ActivityNestedBinding

/**
 *
 * Created by zxx on 2022/7/7 14:30:55
 */
class NestedScaleActivity: AppCompatActivity() {
    private lateinit var viewBinding: ActivityNestedBinding

    private val items = mutableListOf<String>()
    private val adapter: SimpleAdapter<String> = SimpleAdapter<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_nested)
        initList()
        viewBinding.parentView.setScrollHandler(ImageScaleScrollHandler(viewBinding.headerImage))
    }

    private fun initList() {
        viewBinding.recycleView.isNestedScrollingEnabled = true
        viewBinding.recycleView.layoutManager = LinearLayoutManager(this)
        items.add("First Data")
        repeat(8) {
            items.add("RecyclerView Data")
        }
        items.add("End Data")
        viewBinding.recycleView.adapter = adapter
        adapter.items = items
        adapter.notifyDataSetChanged()
        viewBinding.recycleView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
//            println("onScrollShange $scrollY")
        }

        viewBinding.recycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (items.size < 25) {
                        println("load more")
                        repeat(5) {
                            items.add("Add Data")
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}