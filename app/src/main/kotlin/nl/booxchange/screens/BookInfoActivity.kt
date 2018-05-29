package nl.booxchange.screens

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Checkable
import kotlinx.android.synthetic.main.activity_book_info.*
import nl.booxchange.R
import nl.booxchange.extension.*
import nl.booxchange.model.BookModel
import nl.booxchange.model.MessageModel
import nl.booxchange.model.MessageType
import nl.booxchange.model.OfferType
import nl.booxchange.utilities.*
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.firstChildOrNull
import org.jetbrains.anko.toast
import org.joda.time.DateTime

class BookInfoActivity: BaseActivity() {
    private val booksListAdapter = RecyclerViewAdapter()
    private var isEditModeEnabled = false
    private lateinit var bookModel: BookModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)
        (intent?.getSerializableExtra(Constants.EXTRA_PARAM_BOOK_ID) as? String)?.let(::fetchBook) ?: initializeLayout(intent?.getSerializableExtra(Constants.EXTRA_PARAM_BOOK_MODEL) as? BookModel)
    }

    private fun initializeLayout(existingBookModel: BookModel? = bookModel) {
        make_an_offer_button.setOnClickListener { sendRequest() }

        existingBookModel?.let {
            bookModel = it
            writeBookModelToView()
            toggleEditMode(false)
        } ?: run {
            bookModel = BookModel("", userId = UserData.Session.userModel?.id)
            toggleEditMode(true)
        }

        for_sale_label.setOnCheckedChangeListener { _, isChecked ->
            listOf<View>(currency_label, book_price).forEach { it.setVisible(isChecked) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            delete_book_button.stateListAnimator = edit_book_button.stateListAnimator.clone()
        }

        edit_book_button.setTextColor(ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(android.R.attr.state_pressed)),
            arrayOf(getColorById(R.color.whiteGray), getColorById(R.color.transparent)).toIntArray()
        ))

        if (bookModel.userId == UserData.Session.userModel?.id) {
            listOf(offer_type_radio_group, make_an_offer_button, user_books_list).forEach { it.toGone() }

            edit_book_button.setOnClickListener {
                if (!isEditModeEnabled) {
                    toggleEditMode()
                } else {
                    readBookModelFromView()
                    uploadBook()
                }
                //startActivityForResult<BookEditActivity>(Constants.REQUEST_BOOK_EDIT, Constants.EXTRA_PARAM_BOOK_MODEL to bookModel)
            }
            delete_book_button.setOnClickListener {
                if (isEditModeEnabled) {
                    toggleEditMode()
                    initializeLayout()//writeBookModelToView(bookModel)
                } else {
                    toast("Deleting book.. Please wait")
                    requestManager.bookDelete(bookModel.id) { response ->
                        response?.let {
                            if (response.success) {
                                toast("Book was removed")
                                delete_book_button.postDelayed({ finish() }, 1000)
                            } else {
                                toast("Failed to remove\nReason:\n${response.message}")
                            }
                        } ?: run {
                            toast("Connection failed\nThe book wasn't removed")
                            //TODO: show retry view
                        }
                    }
                }
            }
        } else {
            listOf<View>(edit_book_button, delete_book_button).forEach { it.toGone() }
            user_books_list.adapter = booksListAdapter
            user_books_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
/*
            booksListAdapter.addModelToViewBinding(R.layout.list_item_book_offer, BookModel::class) { view, model ->
                Tools.initializeImage(view.book_image, model.image)
                view.book_author.text = model.author
                view.book_title.text = model.title
                view.book_price.text = model.offerPrice?.prependIndent("€") ?: ""
            }
*/
//            booksListAdapter.swapItems(UserData.Session.userBooks)

        }
    }

    private val editBookButtonTexts = mapOf(
        true to "Save changes",
        false to "Edit book"
    )
    private val cancelButtonDrawables = mapOf(
        true to R.drawable.close,
        false to R.drawable.delete
    )

    private fun toggleEditMode(shouldEnableEditMode: Boolean? = null) {
        isEditModeEnabled = shouldEnableEditMode ?: !isEditModeEnabled

        listOf(book_title, book_author, book_edition, book_isbn, book_price, book_info, for_trade_label, for_sale_label, *book_condition_radio_group.childrenSequence().filter { !(it as Checkable).isChecked }.toList().toTypedArray()).forEach { it.isEnabled = isEditModeEnabled }
        edit_book_button.text = editBookButtonTexts[isEditModeEnabled]
        delete_book_button.setImageResource(cancelButtonDrawables[isEditModeEnabled] ?: 0)

        if (isEditModeEnabled) {
            listOf(for_trade_label, for_sale_label, book_condition_radio_group, book_price, currency_label).forEach { it.toVisible() }
            book_condition_unknown.toGone()
        } else {

        }
    }

    private fun writeBookModelToView() {
//        bookModel.image?.let { Tools.initializeImage(book_image, it) }

        book_price.toVisible()
        currency_label.toVisible()
        offer_type_radio_group.toGone()
        for_trade_label.toGone()
        for_sale_label.toGone()

        for_trade_label.isChecked = false
        for_sale_label.isChecked = false

        book_title.setText(bookModel.title)
        book_author.setText(bookModel.author)
        book_edition.setText(bookModel.edition?.string)
        book_isbn.setText(bookModel.isbn)
        book_info.setText(bookModel.info)
        bookModel.offerPrice?.let(book_price::setText) ?: run { book_price.toGone(); currency_label.toGone() }
        bookModel.condition?.let { (book_condition_radio_group.getChildAt(it - 1) as? AppCompatRadioButton)?.isChecked = true } ?: run { book_condition_radio_group.toGone(); book_condition_unknown.toVisible() }

        when (OfferType.valueOf(bookModel.offerType ?: "NONE")) {
            OfferType.NONE -> {
            }
            OfferType.EXCHANGE -> {
                offer_type_radio_group.toVisible()
                radio_button_buy.isEnabled = false
                for_trade_label.toVisible()
                for_trade_label.isChecked = true
                book_price.toGone()
                currency_label.toGone()
            }
            OfferType.SELL -> {
                offer_type_radio_group.toVisible()
                radio_button_trade.isEnabled = false
                for_sale_label.toVisible()
                for_sale_label.isChecked = true
            }
            OfferType.BOTH -> {
                offer_type_radio_group.toVisible()
                for_trade_label.toVisible()
                for_sale_label.toVisible()
                for_trade_label.isChecked = true
                for_sale_label.isChecked = true
            }
        }
    }

    private fun readBookModelFromView() {
//        Tools.initializeImage(book_image, bookModel.image)
        bookModel.title = book_title.text.string.takeNotBlank
        bookModel.author = book_author.text.string.takeNotBlank
        bookModel.edition = book_edition.text.string.takeNotBlank?.toInt()
        bookModel.isbn = book_isbn.text.string.takeNotBlank
        bookModel.offerPrice = book_price.text.string.takeNotBlank
        bookModel.info = book_info.text.string.takeNotBlank
        bookModel.condition = book_condition_radio_group.let { it.indexOfChild(it.firstChildOrNull { (it as? AppCompatRadioButton)?.isChecked == true }) }.takeIf { it > -1 }
        bookModel.offerType = OfferType.getByFilters(for_trade_label.isChecked, for_sale_label.isChecked).name
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun uploadBook() {
        val requestAction = if (intent.hasExtra(Constants.EXTRA_PARAM_BOOK_MODEL)) requestManager::bookUpdate else requestManager::bookAdd
        loadingView.show()
        loadingView.message = "Uploading"
        requestAction(bookModel) { response ->
            response?.let {
                toast("Upload finished")
                if (response.success) {
                    loadingView.message = "Success"
                    toast("Request success")
                    toggleEditMode()
                    //TODO: Show success view
//                    responseIntent.putExtra(Constants.EXTRA_PARAM_BOOK_EDIT_RESULT, true)
//                    logo.postDelayed({ onBackPressed() }, 1000)
                } else {
                    toast("Request failure")
                    //TODO: Show failure view; hide loading view
                }
            } ?: run {
                toast("Upload failed")
                //TODO: Show connection failure message
            }
            loadingView.hide()
        }
    }

    //(\d{3}-\d-\d{5}-\d{3}-\d)|(\d{3}-\d{10})
    private fun sendRequest() {
        loadingView.show()
        loadingView.message = "Finding chat room"
        requestManager.findChatRoom(listOf(bookModel.userId ?: return, UserData.Session.userModel?.id ?: return), "Exchange book ${bookModel.title}") { response ->
            response?.let { chat ->
                val requestMessage = "User [[#USERMODELID#${UserData.Session.userModel?.id}]] wants to exchange [[YOUR BOOK#BOOKMODELID#${bookModel.id}]] with [[HIS/HER BOOK#BOOKMODELID#undefined]] and is ready to talk about it!"
                val requestModel = MessageModel("", chat.id, UserData.Session.userModel?.id ?: return@let, requestMessage, MessageType.REQUEST.name, DateTime.now().serverTimestamp)
                loadingView.message = "Sending request"
                requestManager.postMessage(requestModel) { response ->
                    response?.let { toast("Request sent, wait for user's response") } ?: toast("Failed to send a request")
                    loadingView.hide()
                }
            } ?: run {
                toast("Failed to contact the book's owner")
                loadingView.hide()
            }
        }
    }

    private fun fetchBook(bookId: String) {
        loadingView.show()
        requestManager.bookGet(bookId) {
            it?.also(::bookModel::set)?.also(::initializeLayout) ?: retryView.show()
            loadingView.hide()
        }
    }

/*
    override fun onBackPressed() {
        setResult(Constants.REQUEST_BOOK_EDIT, Intent().putExtra(Constants.EXTRA_PARAM_BOOK_EDIT_RESULT, bookWasEdited))
        super.onBackPressed()
    }
*/

/*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_BOOK_EDIT && resultCode == Activity.RESULT_OK) {
            bookWasEdited = data?.getBooleanExtra(Constants.EXTRA_PARAM_BOOK_EDIT_RESULT, false) ?: false
            if (bookWasEdited) {
                writeBookModelToView(data?.getSerializableExtra(Constants.EXTRA_PARAM_BOOK_MODEL) as BookModel)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
*/
}
