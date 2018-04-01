package nl.booxchange.screens

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import nl.booxchange.R
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.model.BookModel
import nl.booxchange.model.OfferType
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.RecyclerViewAdapter
import nl.booxchange.utilities.Tools
import kotlinx.android.synthetic.main.activity_book_info.*
import kotlinx.android.synthetic.main.offer_list_book_item.view.*

class BookInfoActivity: BaseActivity() {
    private val booksListAdapter = RecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)
        back_button.setOnClickListener { onBackPressed() }
        val bookModel = intent?.getSerializableExtra("book_model") as? BookModel
        Tools.initializeImage(book_image, bookModel?.image)
        book_price.text = bookModel?.offerPrice?.prependIndent("€") ?: ""
        initializeLayout()
    }

    private fun initializeLayout() {
        user_books_list.adapter = booksListAdapter
        user_books_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        booksListAdapter.addModelToViewBinding(R.layout.offer_list_book_item, BookModel::class.java) { view, model ->
            Tools.initializeImage(view.book_image, model.image)
            view.book_author.text = model.author
            view.book_title.text = model.title
            view.book_price.text = model.offerPrice?.prependIndent("€") ?: ""
        }
    }
}
