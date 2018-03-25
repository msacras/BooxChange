package nl.booxchange.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.sliding_navigation_layout.view.*
import nl.booxchange.R
import org.jetbrains.anko.find
import org.jetbrains.anko.findOptional
import org.jetbrains.anko.px2dip
import kotlin.properties.Delegates

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
class SlidingNavigationLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): RelativeLayout(context, attrs, defStyleAttr) {

  private var touchDown = false
  private val lastTouchPoint = PointF()
  private var currentTranslation by Delegates.observable(0f) { _, _, newTranslation -> _isDrawerOpen = newTranslation > 0f }
  private val maxTranslation by lazy { drawer.measuredWidth.toFloat() - 1 }
  private val transitionDuration = 250L //ms
  private var transitionAnimator: ValueAnimator? = null
  private var touchStartTime = 0L
  private val swipeVelocity = 100 //px/s
  private val swipeDuration = 250 //ms

  private var toggle: View? = null
  private lateinit var drawer: ViewGroup
  private lateinit var content: ViewGroup
  private var _isDrawerOpen = false
  private var _setDrawerOpen = { open: Boolean ->
    val (transitionEnd, transitionDuration) = if (open) {
      maxTranslation to (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong()
    } else {
      0f to (transitionDuration * (currentTranslation / maxTranslation)).toLong()
    }
    animateDrawer(transitionEnd, transitionDuration)
  }

  val isDrawerOpen: Boolean
    get() = _isDrawerOpen
  val setDrawerOpen: (Boolean) -> Unit
    get() = _setDrawerOpen

  init {
    View.inflate(context, R.layout.sliding_navigation_layout, this)
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    val distanceFromScreenMargin = px2dip((measuredWidth - ev.x).toInt())
    return if (_isDrawerOpen) ev.x < measuredWidth - maxTranslation else distanceFromScreenMargin < 8
  }

  private fun processTouchEvent(motionEvent: MotionEvent): Boolean {
    return when (motionEvent.action.and(MotionEvent.ACTION_MASK)) {
      MotionEvent.ACTION_DOWN -> {
        transitionAnimator?.cancel()
        transitionAnimator = null
        touchDown = _isDrawerOpen || px2dip((measuredWidth - motionEvent.x).toInt()) < 8
        lastTouchPoint.set(motionEvent.rawX, motionEvent.rawY)
        touchStartTime = System.currentTimeMillis()
        return touchDown
      }
      MotionEvent.ACTION_UP -> {
        if (touchDown) {
          val touchEndTime = System.currentTimeMillis()
          val endTouchPoint = PointF(motionEvent.rawX, motionEvent.rawY)

          toggle?.let { toggle ->
            val hitRect = Rect()
            toggle.getHitRect(hitRect)
            if (hitRect.contains((motionEvent.x + currentTranslation).toInt(), motionEvent.y.toInt())) {
              toggle.performClick()
            }
          }

          val motionDistance = PointF(endTouchPoint.x - lastTouchPoint.x, endTouchPoint.y - lastTouchPoint.y)
          val motionDuration = touchEndTime - touchStartTime
          val motionVelocity = motionDistance.x / (motionDuration / 1000f)
          val (transitionEnd, transitionDuration) = if (motionDuration <= swipeDuration) {
            if (motionVelocity < -swipeVelocity) {
              maxTranslation to (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong()
            } else if (motionVelocity > swipeVelocity) {
              0f to (transitionDuration * (currentTranslation / maxTranslation)).toLong()
            } else {
              if (currentTranslation >= maxTranslation / 2) {
                maxTranslation to (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong()
              } else {
                0f to (transitionDuration * (currentTranslation / maxTranslation)).toLong()
              }
            }
          } else {
            if (currentTranslation >= maxTranslation / 2) {
              maxTranslation to (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong()
            } else {
              0f to (transitionDuration * (currentTranslation / maxTranslation)).toLong()
            }
          }

          animateDrawer(transitionEnd, transitionDuration)

          touchDown = false
        }
        return touchDown
      }
      MotionEvent.ACTION_MOVE -> {
        val currentTouchPoint = PointF(motionEvent.rawX, motionEvent.rawY)
        val motionDelta = PointF(lastTouchPoint.x - currentTouchPoint.x, lastTouchPoint.y - currentTouchPoint.y)
        currentTranslation = (currentTranslation + motionDelta.x).coerceIn(0f, maxTranslation)
        updateState()
        lastTouchPoint.set(currentTouchPoint)

        return touchDown
      }
      else -> false
    }
  }

  private fun updateState() {
    content.translationX = -currentTranslation
    drawer.translationX = -currentTranslation
    shadow.translationX = -currentTranslation
    if (currentTranslation == 0f) {
      _isDrawerOpen = false
    } else if (currentTranslation == maxTranslation) {
      _isDrawerOpen = true
    }
  }

  private fun animateDrawer(endTranslation: Float, transitionDuration: Long) {
    transitionAnimator = ValueAnimator.ofFloat(currentTranslation, endTranslation).apply {
      duration = transitionDuration
      addUpdateListener {
        currentTranslation = animatedValue as Float
        updateState()
      }
    }
    transitionAnimator?.start()
  }

  fun setDrawerView(@LayoutRes layout: Int): View {
    val drawerView = LayoutInflater.from(context).inflate(layout, drawer, false)
    drawer.addView(drawerView)
    toggle = content.findOptional(R.id.drawer_button)
    toggle?.setOnClickListener { setDrawerOpen(!isDrawerOpen) }
    return drawerView
  }

  fun showSnackbar(@StringRes message: Int) {
    Snackbar.make(content, message, Snackbar.LENGTH_SHORT).show()
  }

  override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
    when (child?.id) {
      R.id.shadow -> {}
      R.id.content -> {
        content = child as ViewGroup
      }
      R.id.drawer -> {
        drawer = child as ViewGroup
        setOnTouchListener { _, motionEvent ->
          processTouchEvent(motionEvent)
        }
      }
      else -> {
        content.addView(child, params)
        return
      }
    }
    super.addView(child, params)
  }
}
