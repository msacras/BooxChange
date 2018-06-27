/*
package nl.booxchange.screens

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_homepage.*
import kotlinx.android.synthetic.main.list_item_book_offer.view.*
import nl.booxchange.R
import nl.booxchange.extension.*
import nl.booxchange.model.BookModel
import nl.booxchange.model.OfferType
import nl.booxchange.model.OfferType.*
import nl.booxchange.screens.book_info_fragment.BookInfoView
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.RecyclerViewAdapter
import nl.booxchange.utilities.RecyclerViewItemSpacer
import nl.booxchange.utilities.Tools
import org.jetbrains.anko.dip
import java.util.*
import kotlin.concurrent.timer
import kotlin.properties.Delegates

*/
/**
 * Created by Cristian Velinciuc on 3/9/18.
 *//*

class HomepageActivity: BaseActivity() {
    private val booksListAdapter = RecyclerViewAdapter()
    private val searchListAdapter = RecyclerViewAdapter()

    private lateinit var bookInfoFragment: BookInfoView

    private lateinit var actionButtons: MutableMap<FloatingActionButton, Boolean>

    private var isFilterMenuOpen = false
    private var isPurchaseFilterEnabled: Boolean by Delegates.observable(true) { _, _, state -> selectedOfferType = OfferType.getByFilters(isExchangeFilterEnabled, state) }
    private var isExchangeFilterEnabled: Boolean by Delegates.observable(true) { _, _, state -> selectedOfferType = OfferType.getByFilters(state, isPurchaseFilterEnabled) }

    private var bottomReached = false
    private var isSearchOpen = false
    private var selectedOfferType by Delegates.observable(OfferType.BOTH) { _, _, _ -> fetchBookOffersList(true, true) }
    private val queryKeyword
        get() = search_query_input.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        initializeSwipeRefreshLayout()
        initializeLayout()
        fetchBookOffersList(true, true)
    }

    private fun openFilterMenu() {
        showActionButton(action_filter_exchange)
        action_filter_purchase.postDelayed({ showActionButton(action_filter_purchase) }, 200)
        action_filter_menu.setImageResource(R.drawable.filter)
        disableActionButton(action_filter_menu)
        isFilterMenuOpen = true
    }

    private fun closeFilterMenu() {
        hideActionButton(action_filter_purchase)
        action_filter_exchange.postDelayed({ hideActionButton(action_filter_exchange) }, 50)
        action_filter_menu.setImageResource(R.drawable.filter)
        enableActionButton(action_filter_menu)
        isFilterMenuOpen = false
    }

    private fun initializeLayout() {
        actionButtons = mutableMapOf(
            action_filter_menu to false,
            action_filter_purchase to false,
            action_filter_exchange to false
        )

        action_filter_menu.post {
            hideActionButton(action_filter_purchase, true)
            hideActionButton(action_filter_exchange, true)
        }

        action_filter_purchase.setOnClickListener {
            if (isPurchaseFilterEnabled) disableActionButton(action_filter_purchase) else enableActionButton(action_filter_purchase)
            isPurchaseFilterEnabled = !isPurchaseFilterEnabled
        }

        action_filter_exchange.setOnClickListener {
            if (isExchangeFilterEnabled) disableActionButton(action_filter_exchange) else enableActionButton(action_filter_exchange)
            isExchangeFilterEnabled = !isExchangeFilterEnabled
        }

        action_filter_menu.setOnClickListener {
            if (isFilterMenuOpen) {
                closeFilterMenu()
            } else {
                openFilterMenu()
            }
        }

        search_cancel_button.setOnClickListener {
            if (isSearchOpen) {
                if (queryKeyword.isNotBlank()) {
                    search_query_input.text.clear()
                } else {
                    search_query_input.toGone()
                    (search_wrapper.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.RIGHT_OF)
                    search_cancel_button.setImageResource(R.drawable.magnify)
                }
            } else {
                (search_wrapper.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.RIGHT_OF, R.id.logo)
                search_query_input.toVisible()
                search_query_input.requestFocus()
                search_cancel_button.setImageResource(R.drawable.close)
            }
            isSearchOpen = !isSearchOpen
        }

        search_query_input.addTextChangedListener(object : TextWatcher {
            private var queryDelayTimer: Timer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(string: Editable?) {
                val query = string?.toString() ?: return
                queryDelayTimer?.cancel()
                queryDelayTimer = null
                searchListAdapter.clearItems()
                when {
                    query.length > 3 && query.isNotBlank() -> {
                        queryDelayTimer = timer(initialDelay = 200, period = 200) {
                            bottomReached = false
                            runOnUiThread { fetchBookOffersList(true, true) }
                            queryDelayTimer = null
                            cancel()
                        }
                    }
                    query.length > 0 -> {
                        books_list_view.adapter = searchListAdapter
                        swipe_refresh_layout.isEnabled = false
                    }
                    query.length == 0 -> {
                        books_list_view.adapter = booksListAdapter
                        swipe_refresh_layout.isEnabled = true
                    }
                }
            }
        })

        booksListAdapter.addModelToViewBinding(R.layout.list_item_book_offer, BookModel::class, ::bindItem)
        searchListAdapter.addModelToViewBinding(R.layout.list_item_book_offer, BookModel::class, ::bindItem)
        books_list_view.layoutManager = GridLayoutManager(this, 2)
        books_list_view.adapter = booksListAdapter
        books_list_view.addItemDecoration(RecyclerViewItemSpacer(dip(8)))
        books_list_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 10) {
                    if (isFilterMenuOpen) {
                        actionButtons[action_filter_menu] = true
                        closeFilterMenu()
                        books_list_view.postDelayed({
                            actionButtons[action_filter_menu] = false
                            hideActionButton(action_filter_menu)
                        }, 400)
                    } else {
                        hideActionButton(action_filter_menu)
                    }
                }
                if (dy < -10) {
                    showActionButton(action_filter_menu)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                recyclerView ?: return

                if (recyclerView.computeVerticalScrollOffset() > 0 && newState == RecyclerView.SCROLL_STATE_IDLE && recyclerView.computeVerticalScrollRange() == recyclerView.computeVerticalScrollOffset() + recyclerView.computeVerticalScrollExtent()) {
                    fetchBookOffersList(false, false)
                }
            }
        })

        bookInfoFragment = BookInfoView()
        supportFragmentManager.beginTransaction()
            .replace(R.id.book_info_fragment, bookInfoFragment)
            .hide(bookInfoFragment)
            .commit()
    }

    private fun enableActionButton(view: FloatingActionButton) {
        val (fromColorNormal, fromColorDark) = getColorCompat(R.color.lightGray) to getColorCompat(R.color.midGray)
        val (toColorNormal, toColorDark) = when (view.id) {
            R.id.action_filter_exchange -> getColorCompat(R.color.colorAccent) to getColorCompat(R.color.colorAccentDark)
            else -> getColorCompat(R.color.colorPrimary) to getColorCompat(R.color.colorPrimaryDark)
        }

        ValueAnimator.ofObject(ArgbEvaluator(), fromColorNormal, toColorNormal).apply {
            addUpdateListener {
                view.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
            }
            duration = 100
            start()
        }
        ValueAnimator.ofObject(ArgbEvaluator(), fromColorDark, toColorDark).apply {
            addUpdateListener {
                view.setColorFilter(it.animatedValue as Int)
            }
            duration = 100
            start()
        }
    }

    private fun disableActionButton(view: FloatingActionButton) {
        val (toColorNormal, toColorDark) = getColorCompat(R.color.lightGray) to getColorCompat(R.color.midGray)
        val (fromColorNormal, fromColorDark) = when (view.id) {
            R.id.action_filter_exchange -> getColorCompat(R.color.colorAccent) to getColorCompat(R.color.colorAccentDark)
            else -> getColorCompat(R.color.colorPrimary) to getColorCompat(R.color.colorPrimaryDark)
        }

        ValueAnimator.ofObject(ArgbEvaluator(), fromColorNormal, toColorNormal).apply {
            addUpdateListener {
                view.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
            }
            duration = 100
            start()
        }
        ValueAnimator.ofObject(ArgbEvaluator(), fromColorDark, toColorDark).apply {
            addUpdateListener {
                view.setColorFilter(it.animatedValue as Int)
            }
            duration = 100
            start()
        }
    }

    private fun showActionButton(view: FloatingActionButton, immediate: Boolean = false) {
        val isAnimating = actionButtons[view] ?: return
        val duration = if (immediate) 1L else 250L
        if (!isAnimating && !view.isVisible) {
            actionButtons[view] = true
            view.toVisible()
            view.animate().alpha(1f).translationY(0f).setDuration(duration).withEndAction { actionButtons[view] = false }.start()
        }
    }

    private fun hideActionButton(view: FloatingActionButton, immediate: Boolean = false) {
        val isAnimating = actionButtons[view] ?: return
        val duration = if (immediate) 1L else 100L
        if (!isAnimating && !view.isGone) {
            actionButtons[view] = true
            view.animate().alpha(0f).translationY(view.measuredHeight.toFloat()).setDuration(duration).withEndAction { view.toGone(); actionButtons[view] = false }.start()
        }
    }

    private fun fetchBookOffersList(isRefresh: Boolean, clear: Boolean) {
        if (!isRefresh && bottomReached) {
            swipe_refresh_layout.isRefreshing = false
            return
        }
        val (targetAdapter, targetAction) = if (isSearchOpen) {
            searchListAdapter to if (isRefresh) searchListAdapter::swapItems else searchListAdapter::appendItems
        } else {
            booksListAdapter to if (isRefresh) booksListAdapter::prependItems else booksListAdapter::appendItems
        }
        if (clear) {
            targetAdapter.clearItems()
            bottomReached = false
        }
        val listPosition = if (isRefresh || clear) 0 else targetAdapter.itemCount

        requestManager.fetchAvailableBooks(queryKeyword, selectedOfferType, listPosition) {
            it?.result?.let(targetAction)
            bottomReached = it?.result?.isEmpty() ?: bottomReached
            if (isRefresh) swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun bindItem(view: View, model: BookModel) {
//        Tools.initializeImage(view.book_image, model.image)
        view.book_author.text = model.author
        view.book_title.text = model.title
        view.book_price.text = model.offerPrice?.prependIndent("€") ?: ""
        when (valueOf(model.offerType ?: "NONE")) {
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
//            bookInfoFragment.updateBookModel(model)
//            bookInfoFragment.setSize(view.width, view.height)
*/
/*
            Rect().apply {
                view.getGlobalVisibleRect(this)
                val topOffset = Rect().apply { (contentView as ViewGroup).offsetDescendantRectToMyCoords(view, this) }.top
                bookInfoFragment.setBounds(left, topOffset, right, bottom)
            }
*//*

//            book_info_fragment.toVisible()

//            book_info_fragment.left = view.left
//            book_info_fragment.top = view.top
//            book_info_fragment.right = view.right
//            book_info_fragment.bottom = view.bottom
            //book_info_fragment.
            supportFragmentManager
                .beginTransaction()
                .show(bookInfoFragment)
                .commit()
//            Rect().apply { view.getGlobalVisibleRect(this) }.apply { offset(0, -window.decorView.rootWindowInsets.stableInsetTop) }.let(bookInfoFragment::setBounds)
            //startActivity<BookInfoActivity>(Constants.EXTRA_PARAM_BOOK_MODEL to model)
        }
    }

    private fun initializeSwipeRefreshLayout() {
        swipe_refresh_layout.canRefreshUp = true
        swipe_refresh_layout.setOnUpRefreshListener {
            fetchBookOffersList(false, false)
        }
        swipe_refresh_layout.setOnDownRefreshListener {
            fetchBookOffersList(true, false)
        }
    }
}
*/
