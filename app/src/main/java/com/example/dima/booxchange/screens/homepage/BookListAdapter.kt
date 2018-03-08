package com.example.dima.booxchange.screens.homepage

import com.example.dima.booxchange.R
import com.example.dima.booxchange.model.BookModel
import com.example.dima.booxchange.utilities.RecyclerViewAdapter
import kotlinx.android.synthetic.main.offer_list_book_item.view.*

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
class BookListAdapter: RecyclerViewAdapter() {
  init {
    addModelToViewBinding(R.layout.offer_list_book_item, BookModel::class.java) { view, model ->
      //view.book_image TODO: load images (Glide?)
      view.book_author.text = model.author
      //view.book_price.text TODO: retreive offer data
      view.book_title.text = model.title
    }
  }
}
