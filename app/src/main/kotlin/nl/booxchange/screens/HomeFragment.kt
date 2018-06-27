package nl.booxchange.screens

import android.graphics.Rect
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.transition.TransitionManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_profile.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import nl.booxchange.R
import nl.booxchange.extension.isGone
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.utilities.BaseFragment
import org.jetbrains.anko.dip

class HomeFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_home
    override val viewModel = HomeFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listGroups = listOf(
            Triple(view.most_viewed_more, view.most_viewed_list, view.most_viewed_group),
            Triple(view.latest_exchange_more, view.latest_exchange_list, view.latest_exchange_group),
            Triple(view.latest_sell_more, view.latest_sell_list, view.latest_sell_group)
        )

        listGroups.forEach { (button, list, group) ->
            list.addItemDecoration(object: RecyclerView.ItemDecoration() {
                val `8dp` = view.dip(8)

                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    if (parent.layoutManager is GridLayoutManager) {
                        view.layoutParams = view.layoutParams.apply { width = ViewGroup.LayoutParams.MATCH_PARENT }
                    } else {
                        view.layoutParams = view.layoutParams.apply { width = ViewGroup.LayoutParams.WRAP_CONTENT }
                    }

                    outRect.set(`8dp`, `8dp`, `8dp`, `8dp`)
                }
            })

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
        }
    }
}
