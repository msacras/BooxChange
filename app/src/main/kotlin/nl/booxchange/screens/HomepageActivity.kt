package nl.booxchange.screens

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RelativeLayout
import nl.booxchange.R
import nl.booxchange.extension.*
import nl.booxchange.model.BookModel
import nl.booxchange.model.OfferType
import nl.booxchange.model.OfferType.*
import nl.booxchange.utilities.BaseActivity
import nl.booxchange.utilities.RecyclerViewAdapter
import nl.booxchange.utilities.RecyclerViewItemSpacer
import nl.booxchange.utilities.Tools
import kotlinx.android.synthetic.main.activity_homepage.*
import kotlinx.android.synthetic.main.offer_list_book_item.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.timer
import kotlin.properties.Delegates

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
class HomepageActivity: BaseActivity() {
  private val booksListAdapter = RecyclerViewAdapter()
  private val searchListAdapter = RecyclerViewAdapter()

  private lateinit var actionButtons: MutableMap<FloatingActionButton, Boolean>

  private var isFilterMenuOpen = false
  private var isPurchaseFilterEnabled: Boolean by Delegates.observable(true) { _, _, state -> selectedOfferType = OfferType.getByFilters(isExchangeFilterEnabled, state) }
  private var isExchangeFilterEnabled: Boolean by Delegates.observable(true) { _, _, state -> selectedOfferType = OfferType.getByFilters(state, isPurchaseFilterEnabled) }

  private var bottomReached = false
  private var isSearchOpen = false
  private var selectedOfferType by Delegates.observable(OfferType.BOTH) { _, _, type -> fetchBookOffersList(true, true) }
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
    action_filter_purchase.postDelayed({
      showActionButton(action_filter_purchase)
    }, 200)
    action_filter_menu.setImageResource(R.drawable.temporary_cross)
    disableActionButton(action_filter_menu)
    isFilterMenuOpen = true
  }

  private fun closeFilterMenu() {
    hideActionButton(action_filter_purchase)
    action_filter_exchange.postDelayed({
      hideActionButton(action_filter_exchange)
    }, 50)
    action_filter_menu.setImageResource(R.drawable.temporary_filter)
    enableActionButton(action_filter_menu)
    isFilterMenuOpen = false
  }

  private fun initializeLayout() {
    actionButtons = mutableMapOf(
      action_filter_menu to false,
      action_filter_purchase to false,
      action_filter_exchange to false
    )

    hideActionButton(action_filter_purchase, true)
    hideActionButton(action_filter_exchange, true)

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
          search_cancel_button.setImageResource(R.drawable.temporary_search)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator.ofFloat(search_wrapper.elevation, 0f).apply {
              addUpdateListener {
                search_wrapper.elevation = (it.animatedValue as Float)
              }
              duration = 100
              start()
            }
          }
        }
      } else {
        (search_wrapper.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.RIGHT_OF, R.id.logo)
        search_query_input.toVisible()
        search_query_input.requestFocus()
        search_cancel_button.setImageResource(R.drawable.temporary_cross)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          ValueAnimator.ofFloat(search_wrapper.elevation, dip(2f).toFloat()).apply {
            addUpdateListener {
              search_wrapper.elevation = (it.animatedValue as Float)
            }
            duration = 100
            start()
          }
        }
      }
      isSearchOpen = !isSearchOpen
    }

    search_query_input.addTextChangedListener(object: TextWatcher {
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
            offers_list_view.adapter = searchListAdapter
            swipe_refresh_layout.isEnabled = false
          }
          query.length == 0 -> {
            offers_list_view.adapter = booksListAdapter
            swipe_refresh_layout.isEnabled = true
          }
        }
      }
    })

    booksListAdapter.addModelToViewBinding(R.layout.offer_list_book_item, BookModel::class.java, this::bindItem)
    searchListAdapter.addModelToViewBinding(R.layout.offer_list_book_item, BookModel::class.java, this::bindItem)
    offers_list_view.layoutManager = GridLayoutManager(this, 2)
    offers_list_view.adapter = booksListAdapter
    offers_list_view.addItemDecoration(RecyclerViewItemSpacer(dip(8)))
    offers_list_view.addOnScrollListener(object: RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 10) {
          if (isFilterMenuOpen) {
            actionButtons[action_filter_menu] = true
            closeFilterMenu()
            offers_list_view.postDelayed({
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
  }

  private fun enableActionButton(view: FloatingActionButton) {
    val (fromColorNormal, fromColorDark) = getColorById(R.color.lightGray) to getColorById(R.color.midGray)
    val (toColorNormal, toColorDark) = when (view.id) {
      R.id.action_filter_exchange -> getColorById(R.color.colorAccent) to getColorById(R.color.colorAccentDark)
      else -> getColorById(R.color.colorPrimary) to getColorById(R.color.colorPrimaryDark)
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
    val (toColorNormal, toColorDark) = getColorById(R.color.lightGray) to getColorById(R.color.midGray)
    val (fromColorNormal, fromColorDark) = when (view.id) {
      R.id.action_filter_exchange -> getColorById(R.color.colorAccent) to getColorById(R.color.colorAccentDark)
      else -> getColorById(R.color.colorPrimary) to getColorById(R.color.colorPrimaryDark)
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
    val isAnimating: Boolean = actionButtons[view] ?: return
    val duration: Long = if (immediate) 1 else 250
    if (!isAnimating && !view.isVisible) {
      actionButtons[view] = true
      view.toVisible()
      view.animate().alpha(1f).translationY(0f).setDuration(duration).withEndAction { actionButtons[view] = false }.start()
    }
  }

  private fun hideActionButton(view: FloatingActionButton, immediate: Boolean = false) {
    val isAnimating: Boolean = actionButtons[view] ?: return
    val duration: Long = if (immediate) 1 else 100
    if (!isAnimating && !view.isGone) {
      actionButtons[view] = true
      view.animate().alpha(0f).translationY(view.measuredHeight.toFloat()).setDuration(duration).withEndAction { view.toGone(); actionButtons[view] = false }.start()
    }
  }

  private fun fetchBookOffersList(isRefresh: Boolean, clear: Boolean) {
    if (!isRefresh && bottomReached) return
    val (targetAdapter, targetAction) = if (isSearchOpen) {
      searchListAdapter to if (isRefresh) RecyclerViewAdapter::swapItems else RecyclerViewAdapter::appendItems
    } else {
      booksListAdapter to if (isRefresh) RecyclerViewAdapter::prependItems else RecyclerViewAdapter::appendItems
    }
    if (clear) {
      targetAdapter.clearItems()
      bottomReached = false
    }
    val listPosition = if (isRefresh || clear) 0 else targetAdapter.itemCount
    data_fetch_progress.toVisible()
    requestManager.fetchAvailableBooks(queryKeyword, selectedOfferType, listPosition) {
      it?.result?.let { list ->
        if (list.isEmpty()) {
          bottomReached = true
        } else {
          targetAction.invoke(targetAdapter, list)
        }
      } ?: showSnackbar(R.string.data_fetch_failed)
      offers_list_view.postDelayed({
        data_fetch_progress.toGone()
      }, 500)
    }
  }

  private fun bindItem(view: View, model: BookModel) {
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

  private fun initializeSwipeRefreshLayout() {
    swipe_refresh_layout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorAccent))
    swipe_refresh_layout.setOnRefreshListener {
      swipe_refresh_layout.isRefreshing = false
      fetchBookOffersList(true, false)
    }
  }
}
