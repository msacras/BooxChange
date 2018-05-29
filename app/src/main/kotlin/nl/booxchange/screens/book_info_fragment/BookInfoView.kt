package nl.booxchange.screens.book_info

import android.os.Bundle
import android.support.v7.widget.AppCompatRadioButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_book_info.*
import nl.booxchange.R
import nl.booxchange.extension.string
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.model.BookModel
import nl.booxchange.model.OfferType
import nl.booxchange.screens.HomepageActivity
import nl.booxchange.utilities.BaseFragment
import nl.booxchange.utilities.Tools
import nl.booxchange.widget.CustomDismissLayout


class BookInfoFragment: BaseFragment() {
    private lateinit var bookModel: BookModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_book_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view as? CustomDismissLayout)?.setOnDismissAction { (activity as HomepageActivity).supportFragmentManager.beginTransaction().hide(this).commit() }
    }

    fun updateBookModel(bookModel: BookModel) {
        this.bookModel = bookModel
        writeBookModelToView()
    }

/*
    var bounds = Rect()
    fun setBounds(rect: Rect) {
        view?.layout(rect.left, rect.top, rect.right, rect.bottom)
    }
*/

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            (view as? CustomDismissLayout)?.appear()
        }
    }

/*
    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        view?.postDelayed({
            view?.let { view ->
                view.layout(left, top, right, bottom)
                view.animate()
                    .alpha(1f)
                    .setDuration(100)
                    .withEndAction(::expandView)
                    .start()
            }
        }, 10)
    }

    fun expandView() {
        val view = view ?: return
        val padding = view.context.dip(16)
        val (left, top, right, bottom) = listOf(view.left, view.top, view.right, view.bottom)
        val (expandedLeft, expandedTop, expandedRight, expandedBottom) = listOf(padding, padding, activity.contentView!!.width - padding, activity.contentView!!.height)

        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                view.layout(
                    interpolate(left, expandedLeft, it.animatedFraction),
                    interpolate(top, expandedTop, it.animatedFraction),
                    interpolate(right, expandedRight, it.animatedFraction),
                    interpolate(bottom, expandedBottom, it.animatedFraction)
                )
            }
            interpolator = FastOutSlowInInterpolator()
            duration = 150
            start()
        }
    }

    fun setSize(w: Int, h: Int) {
        view?.layoutParams = view?.layoutParams?.apply {
            width = w
            height = h
        }
    }
*/

    private fun writeBookModelToView() {
        bookModel.image?.let { Tools.initializeImage(book_image, it) }

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
        bookModel.state?.let { (book_condition_radio_group.getChildAt(it - 1) as? AppCompatRadioButton)?.isChecked = true } ?: run { book_condition_radio_group.toGone(); book_condition_unknown.toVisible() }

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
}
