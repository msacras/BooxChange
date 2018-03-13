package nl.booxchange.utilities

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
class RecyclerViewItemSpacer(val spaceHorizontal: Int, val spaceVertical: Int = spaceHorizontal): RecyclerView.ItemDecoration() {
  override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
    outRect?.let {
      outRect.left = spaceHorizontal
      outRect.right = spaceHorizontal
      outRect.bottom = spaceVertical
      outRect.top = spaceVertical
    }
  }
}
