package nl.booxchange.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main_fragment.*
import kotlinx.android.synthetic.main.fragment_library.*
import nl.booxchange.R
import nl.booxchange.extension.toVisible
import nl.booxchange.utilities.Constants

class LibraryFragment : Fragment() {

    val colorGray = Color.parseColor("#939393")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(container?.context).inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeLayout()
        reloadUsersBooks()

        library_icon.setColorFilter(colorGray)

        (activity as MainFragmentActivity).setTitle("Library")
        (activity as MainFragmentActivity).toolbar_title.setTextAppearance(activity, R.style.restPage)
        (activity as MainFragmentActivity).add_book_button.toVisible()
        add_book_btn.setOnClickListener {
            val nextFrag = BookEditFragment()
            activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, nextFrag, "findThisFragment")
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun initializeLayout() {
/*
        user_books_list.layoutManager = GridLayoutManager(this, 2)
        user_books_list.adapter = userBooksAdapter
        userBooksAdapter.addModelToViewBinding(R.layout.offer_list_book_item, BookModel::class.java) { view, model ->
            listOf<View>(view.icon_type_sell, view.icon_type_trade, view.book_price, view.or_label, view.for_trade_label).forEach { it.toGone() }

            Tools.initializeImage(view.book_image, model.image)
            view.book_author.text = model.author
            view.book_title.text = model.title
            view.book_price.text = model.offerPrice?.prependIndent("€") ?: ""

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
        }

        add_book_button.setOnClickListener {
            startActivityForResult<BookEditActivity>(Constants.REQUEST_BOOK_EDIT)
        }
*/
    }
    private fun reloadUsersBooks() {
/*
        loading_view.show()
        loading_view.message = "Synchronizing"
        UserData.Session.fetchUserBooksList { success ->
            userBooksAdapter.swapItems(UserData.Session.userBooks)
            if (success) {
                if (UserData.Session.userBooks.isEmpty()) {
                    no_books_view.toVisible()
                } else {
                    no_books_view.toGone()
                }
            } else {
                //        retry_view.show()
            }
            loading_view.hide()
        }
*/
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
