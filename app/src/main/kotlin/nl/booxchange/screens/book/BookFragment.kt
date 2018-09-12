package nl.booxchange.screens.book

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.vcristian.combus.expect
import kotlinx.android.synthetic.main.activity_main_fragment.*
import kotlinx.android.synthetic.main.fragment_book.view.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.fragment_library.*
import nl.booxchange.R
import nl.booxchange.extension.isVisible
import nl.booxchange.extension.setTintCompat
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.model.BookOpenedEvent
import nl.booxchange.screens.MainFragmentActivity
import nl.booxchange.utilities.BaseFragment

class BookFragment: BaseFragment() {
    override val contentViewResourceId = R.layout.fragment_book
    override val viewModel = BookFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIcon?.setTintCompat(R.color.darkGray)

        expect(BookOpenedEvent::class.java) {
            (activity as? MainFragmentActivity)?.showFragment("book_view", false)
        }

/*        view.image_pager.offscreenPageLimit = 5
        view.book_image.setOnClickListener {
            view.image_pager.setCurrentItem(0, false)
            view.image_pager.toVisible()
            view.app_bar_layout.animate().alpha(0.5f).start()
        }*/

        view.delete_book_button.setOnClickListener {
            viewModel.deleteBook()
            onBackPressed()
        }
    }

    override fun onBackPressed(): Boolean {
/*        val view = view ?: return true

        if (view.image_pager.isVisible) {
            view.image_pager.toGone()
            view.app_bar_layout.animate().alpha(1f).start()
        } else {
            if (viewModel.isEditModeEnabled.get()) {
                viewModel.toggleEditMode(null)
            }
            (activity as? MainFragmentActivity)?.hideFragment("book_view")
        }*/
        (activity as? MainFragmentActivity)?.hideFragment("book_view")
        return isHidden
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
