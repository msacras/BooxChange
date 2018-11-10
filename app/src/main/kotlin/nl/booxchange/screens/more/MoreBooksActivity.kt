package nl.booxchange.screens.more


import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_more.*
import nl.booxchange.BR
import nl.booxchange.R

class MoreBooksActivity: AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(MoreBooksViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_more)
        val bookListType = intent.getStringExtra(KEY_BOOK_LIST_TYPE)

        viewModel.initializeWithConfig(bookListType)
        viewBinding.setVariable(BR.viewModel, viewModel)

        books_list.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (recyclerView.computeVerticalScrollOffset() == recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent()) {
                        viewModel.fetchMoreBooks()
                    }
                }
            }
        })
    }

    companion object {
        const val KEY_BOOK_LIST_TYPE = "BOOK_LIST_TYPE"
    }
}
