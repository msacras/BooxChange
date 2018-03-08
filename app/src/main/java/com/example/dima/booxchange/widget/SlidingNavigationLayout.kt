package com.example.dima.booxchange.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.support.annotation.LayoutRes
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.dima.booxchange.R
import org.jetbrains.anko.find
import org.jetbrains.anko.px2dip

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
class SlidingNavigationLayout(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): CoordinatorLayout(context, attrs, defStyleAttr) {
  constructor(context: Context?, attrs: AttributeSet?): this(context, attrs, 0)
  constructor(context: Context?): this(context, null, 0)

  private var touchDown = false
  private var horizontalDrag = false
  private var verticalDrag = false
  private val startTouchPoint = PointF()
  private val lastTouchPoint = PointF()
  private var currentTranslation = 0f
  private val maxTranslation by lazy { drawer.measuredWidth.toFloat() }
  private val transitionDuration = 250L //ms
  private var transitionAnimator: ValueAnimator? = null
  private var touchStartTime = 0L
  private val swipeVelocity = 1500 //px/s
  private val swipeDuration = 250 //ms

  private var toggle: View? = null
  private lateinit var content: LinearLayout
  private lateinit var drawer: LinearLayout
  private var _isDrawerOpen: Boolean
  private var _setDrawerOpen: (Boolean) -> Unit

  val isDrawerOpen: Boolean
    get() = _isDrawerOpen
  val setDrawerOpen: (Boolean) -> Unit
    get() = _setDrawerOpen

  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    return if (!isDrawerOpen) {
      super.onInterceptTouchEvent(ev)
    } else {
      val toggle = toggle ?: return true
      val toggleBounds = Rect()
      toggle.getHitRect(toggleBounds)
      toggleBounds.offset(-currentTranslation.toInt(), 0)
      if (toggleBounds.contains(ev?.rawX?.toInt() ?: 0, ev?.rawY?.toInt() ?: 0)) {
        return super.onInterceptTouchEvent(ev)
      } else true
    }
  }

  init {
    View.inflate(context, R.layout.sliding_navigation_layout, this)

    setOnTouchListener(object: View.OnTouchListener {
      init {
        _isDrawerOpen = false
        _setDrawerOpen = { open ->
          val (transitionStart, transitionEnd, transitionDuration) = if (open) {
            Triple(currentTranslation, maxTranslation, (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong())
          } else {
            Triple(currentTranslation, 0f, (transitionDuration * (currentTranslation / maxTranslation)).toLong())
          }
          animateDrawer(transitionStart, transitionEnd, transitionDuration)
        }
      }

      override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        view ?: return false
        motionEvent ?: return false
        return processTouchEvent(motionEvent)
      }
    })
  }

  private fun processTouchEvent(motionEvent: MotionEvent): Boolean {
    when (motionEvent.action) {
      MotionEvent.ACTION_DOWN -> {
        when (currentTranslation) {
          0f -> {
            val distanceFromScreenMargin = px2dip((measuredWidth - motionEvent.rawX).toInt())
            if (distanceFromScreenMargin < 30) {
              touchDown = true
            }
          }
          maxTranslation -> {
            touchDown = true
            horizontalDrag = true
          }
          else -> {
            transitionAnimator?.cancel()
            transitionAnimator = null
            touchDown = true
            horizontalDrag = true
          }
        }
        if (touchDown) {
          startTouchPoint.set(motionEvent.rawX, motionEvent.rawY)
          lastTouchPoint.set(startTouchPoint)
          touchStartTime = System.currentTimeMillis()
        }
        return touchDown
      }
      MotionEvent.ACTION_UP -> {
        horizontalDrag = false
        verticalDrag = false

        if (touchDown) {
          val touchEndTime = System.currentTimeMillis()
          val endTouchPoint = PointF(motionEvent.rawX, motionEvent.rawY)
          val motionDistance = PointF(endTouchPoint.x - startTouchPoint.x, endTouchPoint.y - startTouchPoint.y)
          val motionDuration = touchEndTime - touchStartTime
          val motionVelocity = motionDistance.x / (motionDuration / 1000f)
          val (transitionStart, transitionEnd, transitionDuration) = if (motionDuration <= swipeDuration) {
            if (motionVelocity < -swipeVelocity) {
              Triple(currentTranslation, maxTranslation, (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong())
            } else if (motionVelocity > swipeVelocity) {
              Triple(currentTranslation, 0f, (transitionDuration * (currentTranslation / maxTranslation)).toLong())
            } else {
              if (currentTranslation > maxTranslation / 2) {
                Triple(currentTranslation, maxTranslation, (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong())
              } else {
                Triple(currentTranslation, 0f, (transitionDuration * (currentTranslation / maxTranslation)).toLong())
              }
            }
          } else {
            if (currentTranslation > maxTranslation / 2) {
              Triple(currentTranslation, maxTranslation, (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong())
            } else {
              Triple(currentTranslation, 0f, (transitionDuration * (currentTranslation / maxTranslation)).toLong())
            }
          }

          animateDrawer(transitionStart, transitionEnd, transitionDuration)

          touchDown = false
          return true
        }
      }
      MotionEvent.ACTION_MOVE -> {
        if (touchDown) {
          val currentTouchPoint = PointF(motionEvent.rawX, motionEvent.rawY)
          val motionDelta = PointF(lastTouchPoint.x - currentTouchPoint.x, lastTouchPoint.y - currentTouchPoint.y)
          if (!(horizontalDrag || verticalDrag) && currentTranslation == 0f) {
            val motionDelta = PointF(Math.abs(px2dip(motionDelta.x.toInt())), Math.abs(px2dip(motionDelta.y.toInt())))
            if (motionDelta.x < motionDelta.y) {
              if (motionDelta.y > 30) {
                verticalDrag = true
              }
            } else {
              if (motionDelta.x > 30) {
                horizontalDrag = true
              }
            }
          } else {
            if (horizontalDrag) {
              currentTranslation = (currentTranslation + motionDelta.x).coerceIn(0f, maxTranslation)
              updateState()
              lastTouchPoint.set(currentTouchPoint)
            } else {
              touchDown = false
            }
          }
        }

        return horizontalDrag || verticalDrag
      }
    }

    return false
  }

  private fun updateState() {
    content.translationX = -currentTranslation
    drawer.translationX = -currentTranslation
    if (currentTranslation == 0f) {
      _isDrawerOpen = false
    } else if (currentTranslation == maxTranslation) {
      _isDrawerOpen = true
    }
  }

  private fun animateDrawer(transitionStart: Float, transitionEnd: Float, transitionDuration: Long) {
    transitionAnimator = ValueAnimator.ofFloat(transitionStart, transitionEnd).apply {
      duration = transitionDuration
      addUpdateListener {
        currentTranslation = animatedValue as Float
        updateState()
      }
    }
    transitionAnimator?.start()
  }

  fun setDrawerView(@LayoutRes layout: Int) {
    drawer.addView(LayoutInflater.from(context).inflate(layout, this, false))
    toggle = content.find(R.id.drawer_button)
    toggle?.setOnClickListener { setDrawerOpen(!isDrawerOpen) }
  }

  override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
    when (child?.id) {
      R.id.content -> {
        content = child as LinearLayout
      }
      R.id.drawer -> {
        drawer = child as LinearLayout
      }
      else -> {
        content.addView(child, params)
        return
      }
    }
    super.addView(child, params)
  }
}
