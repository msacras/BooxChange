package nl.booxchange.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_library.*
import kotlinx.android.synthetic.main.list_item_book_offer.view.*
import nl.booxchange.R
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.model.BookModel
import nl.booxchange.model.OfferType
import nl.booxchange.utilities.*
import org.jetbrains.anko.startActivity

/**
 * Created by Dima on 3/10/2018.
 */
class LibraryActivity: BaseActivity() {

  private val userBooksAdapter = RecyclerViewAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_library)
    initializeLayout()
    reloadUsersBooks()
  }

  private fun initializeLayout() {
    user_books_list.layoutManager = GridLayoutManager(this, 2)
    user_books_list.adapter = userBooksAdapter
      userBooksAdapter.addModelToViewBinding(R.layout.list_item_book_offer, BookModel::class) { view, model ->
      listOf<View>(view.icon_type_sell, view.icon_type_trade, view.book_price, view.or_label, view.for_trade_label).forEach { it.toGone() }

//      Tools.initializeImage(view.book_image, model.image)
      view.book_author.text = model.author
      view.book_title.text = model.title
          view.book_price.text = model.offerPrice?.prependIndent("€")

      with (OfferType.valueOf(model.offerType ?: "NONE")) {
        if (isExchange) {
          view.icon_type_trade.toVisible()
          view.for_trade_label.toVisible()
        }
        if (isSell) {
          view.icon_type_sell.toGone()
          view.book_price.toVisible()
        }
        if (isBoth) {
          view.or_label.toGone()
        }
      }

          view.setOnClickListener {
              startActivity<BookInfoActivity>(Constants.EXTRA_PARAM_BOOK_MODEL to model)
          }
    }

      listOf(add_book_button, action_add_book).forEach { button ->
          button.setOnClickListener {
              //startActivityForResult<BookEditActivity>(Constants.REQUEST_BOOK_EDIT)
          }
    }
  }

  private fun reloadUsersBooks() {
      loadingView.show()
      loadingView.message = "Synchronizing"
    UserData.Session.fetchUserBooksList { success ->
      userBooksAdapter.swapItems(UserData.Session.userBooks)
      if (success) {
        if (UserData.Session.userBooks.isEmpty()) {
          no_books_view.toVisible()
            user_books_list.toGone()
            action_add_book.toGone()
        } else {
          no_books_view.toGone()
            user_books_list.toVisible()
            action_add_book.toVisible()
        }
      } else {
          retryView.show()
      }
        loadingView.hide()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == Constants.REQUEST_BOOK_EDIT && resultCode == Activity.RESULT_OK) {
      if (data?.extras?.getBoolean(Constants.EXTRA_PARAM_BOOK_EDIT_RESULT) == true) {
        reloadUsersBooks()
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }
}
