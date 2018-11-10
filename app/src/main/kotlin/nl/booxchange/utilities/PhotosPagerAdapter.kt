package nl.booxchange.utilities

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.graphics.PointF
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.widget.AppCompatImageView
import android.view.*
import nl.booxchange.BR
import kotlinx.android.synthetic.main.item_view_photo.view.*
import nl.booxchange.BooxchangeApp
import nl.booxchange.R
import nl.booxchange.model.entities.ImageModel
import java.lang.ref.WeakReference

class PhotosPagerAdapter(private val handler: Any?): PagerAdapter() {

    private val items = ArrayList<ImageModel>()
    private var viewPager = WeakReference<ViewPager>(null)

    fun swapItems(items: List<ImageModel>) {
        val viewPager = viewPager.get()
/*
        val viewPager = viewPager.get() ?: run {
            this.items.clear()
            this.items.addAll(items)
            notifyDataSetChanged()
            return
        }

        val newItemPosition = when {
            this.items.size > items.size -> {
                if (viewPager.currentItem == 0) {
                    viewPager.setCurrentItem(1, false)
                    0
                } else {
                    viewPager.currentItem
                }
            }
            this.items.size < items.size -> {
                items.size - 2
            }
            else -> 0
        }

*/

        if ((items + this.items).distinct().size in this.items.size - 1 .. this.items.size + 1) {}

        this.items.clear()
        this.items.addAll(items)

        notifyDataSetChanged()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        viewPager.get() ?: { viewPager = WeakReference(container as ViewPager) }()

        val item = items[position]
        val layout = when (item.type) {
            ImageModel.EditablePhotoType.ADD -> R.layout.item_add_photo
            else -> R.layout.item_view_photo
        }
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(container.context), layout, container, true)

        binding.setVariable(BR.itemModel, item)
        binding.setVariable(BR.itemHandler, handler)

/*
        if (item.type in listOf(ImageModel.EditablePhotoType.LOCAL, ImageModel.EditablePhotoType.REMOTE)) {
            PhotoScalingUtility.setupScaleGestureForView(binding.root.image_view, binding.root)
        }
*/

        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return items.size
    }

    private object PhotoScalingUtility {

        fun setupScaleGestureForView(targetView: AppCompatImageView, rootView: View) {
            var translationX = 0f
            var translationY = 0f

            val scaleGestureListener = object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    targetView.pivotX = detector.focusX
                    targetView.pivotY = detector.focusY

                    targetView.scaleX *= detector.scaleFactor
                    targetView.scaleY *= detector.scaleFactor

                    val translationWeightX = 0.5f - (1f - targetView.pivotX / targetView.width)
                    val sizeDeltaX = targetView.width * targetView.scaleX - targetView.width

                    targetView.translationX = translationX + sizeDeltaX * translationWeightX

                    return true
                }
            }

            val scaleGestureDetector = ScaleGestureDetector(BooxchangeApp.context, scaleGestureListener)

            val touchPointLast = PointF()

            rootView.setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        touchPointLast.set(event.x, event.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val distanceX = touchPointLast.x - event.x
                        val distanceY = touchPointLast.y - event.y

                        translationX -= distanceX
                        translationY -= distanceY

                        targetView.translationX -= distanceX
                        targetView.translationY -= distanceY

                        touchPointLast.set(event.x, event.y)
                    }
                    MotionEvent.ACTION_UP -> {

                    }
                }

                scaleGestureDetector.onTouchEvent(event)
            }
        }
    }
}
