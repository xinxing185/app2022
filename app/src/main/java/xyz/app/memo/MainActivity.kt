package xyz.app.memo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import xyz.app.memo.databinding.ActivityMainBinding
import xyz.app.memo.view.nestedscroll.NestedFixTopActivity
import xyz.app.memo.view.nestedscroll.NestedScaleActivity
import kotlin.reflect.KProperty

// 属性委托
class TextLazy(private val id: Int) {
    var viewMap = mutableMapOf<Int, TextView>()
    operator fun getValue(mainActivity: MainActivity, property: KProperty<*>): TextView {
        var view = viewMap[id]
        if (view == null) {
            view = mainActivity.findViewById<TextView>(id)
            viewMap[id] = view
        }
        return view!!
    }

    operator fun setValue(mainActivity: MainActivity, property: KProperty<*>, textView: TextView) {
    }

}
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var tv01:TextView by TextLazy(R.id.text1)
    private var tv02:TextView by TextLazy(R.id.text2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        tv01.setOnClickListener {
            startActivity(Intent(this, NestedScaleActivity::class.java))
        }
        tv02.setOnClickListener {
            startActivity(Intent(this, NestedFixTopActivity::class.java))
        }
    }
}


