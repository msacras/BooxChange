package com.example.dima.booxchange.screens

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.example.dima.booxchange.R
import com.example.dima.booxchange.extension.toGone
import com.example.dima.booxchange.extension.toVisible
import com.example.dima.booxchange.model.BookModel
import com.example.dima.booxchange.model.OfferType
import com.example.dima.booxchange.utilities.BaseActivity
import com.example.dima.booxchange.utilities.RecyclerViewAdapter
import com.example.dima.booxchange.utilities.Tools
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
        book_price.text = bookModel?.offer_price?.prependIndent("€") ?: ""
/*
        button.setOnClickListener {
            if (book_image.visibility == View.VISIBLE) {
                book_image.visibility = View.GONE
            } else {
                book_image.visibility = View.VISIBLE
            }
        }
*/
        initializeLayout()
        fetchUserBooks()
    }

    private fun initializeLayout() {
        userBooksList.adapter = booksListAdapter
        userBooksList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        booksListAdapter.addModelToViewBinding(R.layout.offer_list_book_item, BookModel::class.java) { view, model ->
            Tools.initializeImage(view.book_image, model.image)
            view.book_author.text = model.author
            view.book_title.text = model.title
            view.book_price.text = model.offer_price?.prependIndent("€") ?: ""
            when (OfferType.valueOf(model.offer_name ?: "NONE")) {
                OfferType.EXCHANGE -> {
                    view.icon_type_sell.toGone()
                    view.icon_type_trade.toVisible()
                    view.book_price.toGone()
                    view.or_label.toGone()
                    view.for_trade_label.toVisible()
                }
                OfferType.SELL -> {
                    view.icon_type_sell.toVisible()
                    view.icon_type_trade.toGone()
                    view.book_price.toVisible()
                    view.or_label.toGone()
                    view.for_trade_label.toGone()
                }
                OfferType.BOTH -> {
                    view.icon_type_sell.toVisible()
                    view.icon_type_trade.toVisible()
                    view.book_price.toVisible()
                    view.or_label.toVisible()
                    view.for_trade_label.toVisible()
                }
                OfferType.NONE -> {
                    view.icon_type_sell.toGone()
                    view.icon_type_trade.toGone()
                    view.book_price.toGone()
                    view.or_label.toGone()
                    view.for_trade_label.toGone()
                }
            }
        }

    }

    private fun fetchUserBooks() {
        requestManager.fetchBooksByUserId(1) {
            it?.result?.let { userBooksList ->
                booksListAdapter.swapItems(userBooksList)
            }
        }
    }
}
