package nl.booxchange.screens.library

import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main_fragment.*
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.android.synthetic.main.fragment_library.view.*
import nl.booxchange.R
import nl.booxchange.utilities.BaseFragment
import org.jetbrains.anko.dip

class LibraryFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_library
    override val viewModel = LibraryFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.user_books_list.addItemDecoration(object: RecyclerView.ItemDecoration() {
            val `8dp` = view.dip(8)

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                view.layoutParams = view.layoutParams.apply { width = ViewGroup.LayoutParams.MATCH_PARENT }
                outRect.set(`8dp`, `8dp`, `8dp`, `8dp`)
            }
        })

        view.postDelayed({
            val `56dp` = context!!.dip(56)
            activity?.app_bar_layout?.addOnOffsetChangedListener { _, verticalOffset ->
                view.add_book_button.translationY = -verticalOffset.toFloat() - `56dp`
            }
        }, 56)
    }
}
