package nl.booxchange.screens.home

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_home.view.*
import nl.booxchange.R
import nl.booxchange.screens.more.MoreBooksActivity
import nl.booxchange.utilities.BaseFragment
import org.jetbrains.anko.startActivity

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

        listGroups.forEach { (button, _, type) ->
            button.setOnClickListener {
                view.context.startActivity<MoreBooksActivity>(MoreBooksActivity.KEY_BOOK_LIST_TYPE to type)
            }
        }
    }
}
