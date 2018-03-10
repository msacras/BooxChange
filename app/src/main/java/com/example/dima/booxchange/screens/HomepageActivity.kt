package com.example.dima.booxchange.screens

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import com.example.dima.booxchange.R
import com.example.dima.booxchange.extension.toGone
import com.example.dima.booxchange.extension.toVisible
import com.example.dima.booxchange.model.BookModel
import com.example.dima.booxchange.model.OfferType.*
import com.example.dima.booxchange.utilities.BaseActivity
import com.example.dima.booxchange.utilities.RecyclerViewAdapter
import com.example.dima.booxchange.utilities.RecyclerViewItemSpacer
import com.example.dima.booxchange.utilities.Tools
import kotlinx.android.synthetic.main.activity_homepage.*
import kotlinx.android.synthetic.main.offer_list_book_item.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
class HomepageActivity: BaseActivity() {
  val booksListAdapter = RecyclerViewAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_homepage)
    initializeSwipeRefreshLayout()
    initializeLayout()
    fetchBookOffersList()
  }

  private fun initializeLayout() {
    booksListAdapter.addModelToViewBinding(R.layout.offer_list_book_item, BookModel::class.java) { view, model ->
      Tools.initializeImage(view.book_image, model.image)
      view.book_author.text = model.author
      view.book_title.text = model.title
      view.book_price.text = model.offer_price?.prependIndent("€") ?: ""
        when (valueOf(model.offer_name ?: "NONE")) {
          EXCHANGE -> {
            view.icon_type_sell.toGone()
            view.icon_type_trade.toVisible()
            view.book_price.toGone()
            view.or_label.toGone()
            view.for_trade_label.toVisible()
          }
          SELL -> {
            view.icon_type_sell.toVisible()
            view.icon_type_trade.toGone()
            view.book_price.toVisible()
            view.or_label.toGone()
            view.for_trade_label.toGone()
          }
          BOTH -> {
            view.icon_type_sell.toVisible()
            view.icon_type_trade.toVisible()
            view.book_price.toVisible()
            view.or_label.toVisible()
            view.for_trade_label.toVisible()
          }
          NONE -> {
            view.icon_type_sell.toGone()
            view.icon_type_trade.toGone()
            view.book_price.toGone()
            view.or_label.toGone()
            view.for_trade_label.toGone()
          }
        }

      view.setOnClickListener {
        startActivity<BookInfoActivity>("book_model" to model)
      }
    }

    offers_list_view.layoutManager = GridLayoutManager(this, 2)
    offers_list_view.adapter = booksListAdapter
    offers_list_view.addItemDecoration(RecyclerViewItemSpacer(dip(12), dip(6)))
  }

  private fun fetchBookOffersList() {
    requestManager.fetchAllAvailableBooks {
      it?.result?.let { list ->
        booksListAdapter.swapItems(list)
      } ?: showErrorSnackbar(R.string.data_fetch_failed)
      swipe_refresh_layout.isRefreshing = false
    }
  }

  private fun initializeSwipeRefreshLayout() {
    swipe_refresh_layout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorAccent))
    swipe_refresh_layout.setOnRefreshListener {
      fetchBookOffersList()
    }
  }
}
