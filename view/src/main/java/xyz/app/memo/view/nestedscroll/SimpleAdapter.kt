package xyz.app.memo.view.nestedscroll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import xyz.app.memo.view.R
import xyz.app.memo.view.databinding.ItemViewBinding

/**
 *
 * Created by zxx on 2022/6/21 14:20:24
 */
class SimpleAdapter<T>(): RecyclerView.Adapter<SimpleAdapter<T>.ViewHolder>() {
    var items = mutableListOf<T>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.itemText.text = "${item.toString()} ${position + 1}"
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var binding: ItemViewBinding = DataBindingUtil.bind(itemView)!!
    }
}

