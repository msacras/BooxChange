package nl.booxchange.screens.home


import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.vcristian.combus.expect
import kotlinx.android.synthetic.main.fragment_more.*
import nl.booxchange.R
import nl.booxchange.extension.*
import nl.booxchange.model.BooksListOpenedEvent
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.utilities.BaseFragment

class MoreFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_more
    override val viewModel = MoreFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tool.setNavigationOnClickListener { onBackPressed() }
        tool.navigationIcon?.setTintCompat(R.color.darkGray)

        expect(BooksListOpenedEvent::class.java) {
            (activity as? MainFragmentActivity)?.showFragment("more_view", false)
        }

        filter_btn.setOnClickListener {
            filter.setVisible(!filter.isVisible)
        }

        filter_btn.setOnTouchListener { _, motionEvent ->
//            motionEvent.action == MotionEvent.ACTION_UP
            false
        }
    }

    override fun onBackPressed(): Boolean {
        (activity as? MainFragmentActivity)?.hideFragment("more_view")
        return isHidden
    }
}
