package nl.booxchange.screens.book

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_book_details.*
import nl.booxchange.BR
import nl.booxchange.R
import nl.booxchange.extension.isVisible
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
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

        setupViews()
    }

    private fun setupViews() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        image_pager.offscreenPageLimit = 5
        book_image.setOnClickListener {
            if (viewModel.images.isNotEmpty() || viewModel.isEditModeEnabled.get()) {
                image_pager.setCurrentItem(0, false)
                image_pager.toVisible()
            }
        }
    }

    override fun onBackPressed() {
        when {
            image_pager.isVisible -> image_pager.toGone()
            viewModel.isEditModeEnabled.get() -> viewModel.toggleEditMode(null)
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val KEY_BOOK_ID = "BOOK_ID"
        const val KEY_BOOK_MODEL = "BOOK_MODEL"
    }
}
