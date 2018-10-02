package nl.booxchange.screens.book

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import nl.booxchange.BR
import nl.booxchange.R
import nl.booxchange.model.entities.BookModel
import nl.booxchange.model.events.BookOpenedEvent

class BookDetailsActivity: AppCompatActivity() {
    private val viewModel by lazy { ViewModelProviders.of(this).get(BookDetailsViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_book_details)
        val bookModel = intent.getSerializableExtra(KEY_BOOK_MODEL) as? BookModel
        val bookId = bookModel?.id ?: intent.getStringExtra(KEY_BOOK_ID) ?: ""

        viewModel.initializeWithConfig(BookOpenedEvent(bookModel, bookId))
        viewBinding.setVariable(BR.viewModel, viewModel)
    }

/*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIcon?.setTintCompat(R.color.darkGray)

        expect(BookOpenedEvent::class.java) {
            (activity as? MainFragmentActivity)?.showFragment("book_view", false)
        }

*/
/*        view.image_pager.offscreenPageLimit = 5
        view.book_image.setOnClickListener {
            view.image_pager.setCurrentItem(0, false)
            view.image_pager.toVisible()
            view.app_bar_layout.animate().alpha(0.5f).start()
        }*//*


        view.delete_book_button.setOnClickListener {
            viewModel.deleteBook()
            onBackPressed()
        }
    }

    override fun onBackPressed(): Boolean {
        val view = view ?: return true

        if (view.image_pager.isVisible) {
            view.image_pager.toGone()
            view.app_bar_layout.animate().alpha(1f).start()
        } else {
            if (viewModel.isEditModeEnabled.get()) {
                viewModel.toggleEditMode(null)
            }
            (activity as? MainFragmentActivity)?.hideFragment("book_view")
        }
        (activity as? MainFragmentActivity)?.hideFragment("book_view")
        return isHidden
    }
*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val KEY_BOOK_ID = "BOOK_ID"
        const val KEY_BOOK_MODEL = "BOOK_MODEL"
    }
}
