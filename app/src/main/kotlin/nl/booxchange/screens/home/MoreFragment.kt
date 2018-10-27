package nl.booxchange.screens.home


import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.fragment_more.*
import nl.booxchange.BR
import nl.booxchange.R
import org.jetbrains.anko.dip

class MoreFragment: AppCompatActivity() {
    private val viewModel = MoreFragmentViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.fragment_more).apply {
            executePendingBindings()
            setVariable(BR.viewModel, viewModel)
        }

        list.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val `8dp` = view.dip(16)
            val `0dp` = view.dip(0)

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(`8dp`, `8dp`, `0dp`, `0dp`)
            }
        })

        back.setOnClickListener{ onBackPressed() }
//        tool.navigationIcon?.setTintCompat(R.color.darkGray)

/*        expect(MoreFragment::class.java) {
            startActivity(Intent(this, MoreFragment::class.java))
//            (activity as? MainFragmentActivity)?.showFragment("more_view", false)
        }*/
    }
}
