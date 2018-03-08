package com.example.dima.booxchange.screens.homepage

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import com.example.dima.booxchange.R
import com.example.dima.booxchange.model.BookModel
import com.example.dima.booxchange.utilities.BaseActivity
import com.example.dima.booxchange.utilities.RecyclerViewItemSpacer
import kotlinx.android.synthetic.main.activity_homepage.*
import org.jetbrains.anko.dip

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
class HomepageActivity: BaseActivity() {
  val booksListAdapter = BookListAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_homepage)
    initializeSwipeRefreshLayout()
    initializeLayout()
    fetchBookOffersList()
  }

  private fun initializeLayout() {
    offers_list_view.layoutManager = GridLayoutManager(this, 2)
    offers_list_view.adapter = booksListAdapter
    offers_list_view.addItemDecoration(RecyclerViewItemSpacer(dip(12), dip(6)))
  }

  private fun fetchBookOffersList() {
    //temporary, no backend yet

    val booksList = (0..10).map { i ->
      BookModel(i, "Random book long example title", "Some human", 2018, "249-411433512", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum tristique ultricies laoreet. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Pellentesque ultrices, justo ac molestie laoreet, augue dui pharetra nunc, eu viverra enim odio non quam. Donec consectetur tortor vel libero imperdiet, nec porta dolor ullamcorper. Integer eu magna sit amet nunc consequat lobortis. Vivamus accumsan auctor sem et sollicitudin. Proin porta nisi ut mollis placerat. In lacinia rhoncus risus, in malesuada nunc pellentesque nec. Ut in aliquet libero. Mauris nec hendrerit velit, non suscipit massa. Nam nibh dui, pharetra a urna a, malesuada fermentum mauris. Suspendisse malesuada turpis tortor, in varius felis pulvinar in. Duis aliquet volutpat magna id mattis. Aenean lacinia imperdiet augue, accumsan faucibus ex fermentum eget.\n\nNulla aliquet ultrices tellus, at elementum dolor tincidunt id. Etiam aliquam, diam et maximus tempus, nunc lectus molestie ligula, at sollicitudin ex erat et risus. Sed rhoncus, ligula non tempor pellentesque, leo tellus aliquet neque, nec malesuada arcu neque et metus. Curabitur at posuere purus, sed cursus augue. Morbi lobortis facilisis lorem sed mollis. Nullam neque lorem, suscipit a leo sit amet, maximus interdum est. Nulla a faucibus erat. Vestibulum feugiat mollis lobortis. Sed varius nisl nec mollis luctus. Fusce posuere tincidunt nisl tempus dictum.", "29293482jvh3fwsgthw54gg4g3.jpeg")
    }
    booksListAdapter.swapItems(booksList)
  }

  private fun initializeSwipeRefreshLayout() {
    swipe_refresh_layout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorAccent))
    swipe_refresh_layout.setOnRefreshListener {
      swipe_refresh_layout.isRefreshing = false
    }
  }
}
