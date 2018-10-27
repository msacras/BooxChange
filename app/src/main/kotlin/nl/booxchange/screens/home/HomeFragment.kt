package nl.booxchange.screens.home

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.vcristian.combus.post
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import nl.booxchange.R
import nl.booxchange.model.BooksListOpenedEvent
import nl.booxchange.utilities.BaseFragment
import org.jetbrains.anko.dip

class HomeFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_home
    override val viewModel = HomeFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listGroups = listOf(
            Triple(view.most_viewed_more, view.most_viewed_list, "SELL"),
            Triple(view.latest_exchange_more, view.latest_exchange_list, "TRADE"),
            Triple(view.latest_sell_more, view.latest_sell_list, "VIEWS")
        )

        listGroups.forEach { (button, list, type) ->
            button.setOnClickListener {
//                post(BooksListOpenedEvent(type))
                startActivity(Intent(activity, MoreFragment::class.java))
            }

            list.addItemDecoration(object: RecyclerView.ItemDecoration() {
                val `8dp` = view.dip(8)

                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(`8dp`, `8dp`, `8dp`, `8dp`)
                }
            })

/*
            button.setOnClickListener {
                val previousLayoutManager = list.tag as? RecyclerView.LayoutManager ?: GridLayoutManager(list.context, 2)
                val currentLayoutManager = list.layoutManager

                list.layoutManager = previousLayoutManager
                list.tag = currentLayoutManager

                if (list.layoutManager is GridLayoutManager) {
                    button.setText(R.string.less)
                    listGroups.forEach { if (it.third != group) it.third.toGone() }
                } else {
                    button.setText(R.string.more)
                    listGroups.forEach { if (it.third.isGone) it.third.toVisible() }
                }
            }
*/
        }
    }
}
