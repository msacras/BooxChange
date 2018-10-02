package nl.booxchange.screens.more


import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import nl.booxchange.BR
import nl.booxchange.R
import nl.booxchange.model.events.BooksListOpenedEvent

class MoreBooksActivity: AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(MoreBooksViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.fragment_more)
        val bookListType = intent.getStringExtra(KEY_BOOK_LIST_TYPE)

        viewModel.initializeWithConfig(BooksListOpenedEvent(bookListType))
        viewBinding.setVariable(BR.viewModel, viewModel)
    }

    companion object {
        const val KEY_BOOK_LIST_TYPE = "BOOK_LIST_TYPE"
    }
}
