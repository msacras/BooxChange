package com.example.dima.booxchange.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.support.annotation.LayoutRes
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.example.dima.booxchange.R
import org.jetbrains.anko.find
import org.jetbrains.anko.px2dip
import kotlin.properties.Delegates

/**
 * Created by Cristian Velinciuc on 3/9/18.
 */
class SlidingNavigationLayout(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): CoordinatorLayout(context, attrs, defStyleAttr) {
  constructor(context: Context?, attrs: AttributeSet?): this(context, attrs, 0)
  constructor(context: Context?): this(context, null, 0)

  private var touchDown = false
  private val lastTouchPoint = PointF()
  private var currentTranslation by Delegates.observable(0f) { _, _, newTranslation -> _isDrawerOpen = newTranslation > 0f }
  private val maxTranslation by lazy { drawer.measuredWidth.toFloat() - 1 }
  private val transitionDuration = 250L //ms
  private var transitionAnimator: ValueAnimator? = null
  private var touchStartTime = 0L
  private val swipeVelocity = 100 //px/s
  private val swipeDuration = 200 //ms

  private var toggle: View? = null
  private lateinit var content: CardView
  private lateinit var drawer: LinearLayout
  private var _isDrawerOpen: Boolean
  private var _setDrawerOpen: (Boolean) -> Unit

  val isDrawerOpen: Boolean
    get() = _isDrawerOpen
  val setDrawerOpen: (Boolean) -> Unit
    get() = _setDrawerOpen

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return if (_isDrawerOpen) isDrawerOpen else processTouchEvent(ev)
  }

  override fun onTouchEvent(ev: MotionEvent): Boolean {
    return processTouchEvent(ev)
  }

  init {
    View.inflate(context, R.layout.sliding_navigation_layout, this)

    _isDrawerOpen = false
    _setDrawerOpen = { open ->
      val (transitionEnd, transitionDuration) = if (open) {
        maxTranslation to (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong()
      } else {
        0f to (transitionDuration * (currentTranslation / maxTranslation)).toLong()
      }
      animateDrawer(transitionEnd, transitionDuration)
    }
  }

  private fun processTouchEvent(motionEvent: MotionEvent): Boolean {
    when (motionEvent.action) {
      MotionEvent.ACTION_DOWN -> {
        when (currentTranslation) {
          0f -> {
            val distanceFromScreenMargin = px2dip((measuredWidth - motionEvent.x).toInt())
            if (distanceFromScreenMargin < 30) {
              touchDown = true
            }
          }
          else -> {
            transitionAnimator?.cancel()
            transitionAnimator = null
            touchDown = true
          }
        }
        if (touchDown) {
          lastTouchPoint.set(motionEvent.x, motionEvent.y)
          touchStartTime = System.currentTimeMillis()
        }
        return touchDown
      }
      MotionEvent.ACTION_UP -> {
        toggle?.let { toggle ->
          val hitRect = Rect()
          toggle.getHitRect(hitRect)
          if (hitRect.contains((motionEvent.x + currentTranslation).toInt(), motionEvent.y.toInt())) {
            toggle.performClick()
          }
        }

        if (touchDown) {
          val touchEndTime = System.currentTimeMillis()
          val endTouchPoint = PointF(motionEvent.x, motionEvent.y)
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
            Toast.makeText(this.context, "$motionDuration $motionDistance $motionVelocity", Toast.LENGTH_SHORT)
            if (currentTranslation >= maxTranslation / 2) {
              maxTranslation to (transitionDuration * (1f - (currentTranslation / maxTranslation))).toLong()
            } else {
              0f to (transitionDuration * (currentTranslation / maxTranslation)).toLong()
            }
          }

          animateDrawer(transitionEnd, transitionDuration)

          touchDown = false
          return true
        }
      }
      MotionEvent.ACTION_MOVE -> {
        if (touchDown) {
          val currentTouchPoint = PointF(motionEvent.x, motionEvent.y)
          val motionDelta = PointF(lastTouchPoint.x - currentTouchPoint.x, lastTouchPoint.y - currentTouchPoint.y)
          currentTranslation = (currentTranslation + motionDelta.x).coerceIn(0f, maxTranslation)
          updateState()
          lastTouchPoint.set(currentTouchPoint)
        }

        return touchDown
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

  fun setDrawerView(@LayoutRes layout: Int) {
    drawer.addView(LayoutInflater.from(context).inflate(layout, this, false))
    toggle = content.find(R.id.drawer_button)
    toggle?.setOnClickListener { setDrawerOpen(!isDrawerOpen) }
  }

  override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
    when (child?.id) {
      R.id.content -> {
        content = child as CardView
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
