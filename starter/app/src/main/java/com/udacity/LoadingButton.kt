package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import androidx.core.content.withStyledAttributes

class LoadingButton @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  private lateinit var title: String

  private var widthSize = 0
  private var heightSize = 0

  private var buttonColorOne = context.resources.getColor(R.color.colorPrimary, null)
  private var buttonColorTwo = context.resources.getColor(R.color.colorPrimaryDark, null)
  private var circleLoading = context.resources.getColor(R.color.colorAccent, null)

  private var downloadProgress = 0F
  private var circleProgress = 0F

  private val circleAnimation =
    ValueAnimator.ofFloat(0F, CIRCLE_ANIMATION_MAX_VALUE).apply {
      addUpdateListener { circleProgress = it.animatedValue as Float; invalidate() }
    }
  private val horizontalAnimation =
    ValueAnimator.ofFloat(0F, HORIZONTAL_ANIMATION_MAX_VALUE).apply {
      addUpdateListener { downloadProgress = it.animatedValue as Float; invalidate() }
      addListener(onEnd = { animationRestore() })
    }

  private val animator: AnimatorSet = AnimatorSet()
  private val actionButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

  private val loadingCirclePaint =
    createPaint(Color.WHITE, strokeWidth = LOADING_CIRCLE_STROKE_WIDTH, style = Paint.Style.FILL)
  private val paintText =
    createPaint(Color.WHITE, textSize = TEXT_SIZE, textAlign = Paint.Align.CENTER)

  companion object {
    private const val LOADING_CIRCLE_STROKE_WIDTH = 10F
    private const val TEXT_SIZE = 50F
    private const val ANIMATION_DURATION = 3000L
    private const val CIRCLE_ANIMATION_REPEAT_COUNT = ValueAnimator.INFINITE
    private const val HORIZONTAL_ANIMATION_REPEAT_COUNT = ValueAnimator.INFINITE
    private const val HORIZONTAL_ANIMATION_MAX_VALUE = 1000F
    private const val CIRCLE_ANIMATION_MAX_VALUE = 360F
  }

  private fun createPaint(
    color: Int,
    strokeWidth: Float? = null,
    style: Paint.Style? = null,
    textSize: Float? = null,
    textAlign: Paint.Align? = null
  ): Paint {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = color

    strokeWidth?.let { paint.strokeWidth = it }
    style?.let { paint.style = it }
    textSize?.let { paint.textSize = it }
    textAlign?.let { paint.textAlign = it }

    return paint
  }

  init {
    isClickable = true
    context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
      buttonColorOne = getColor(R.styleable.LoadingButton_buttonColorOne, 0)
      buttonColorTwo = getColor(R.styleable.LoadingButton_buttonColorTwo, 0)
      circleLoading = getColor(R.styleable.LoadingButton_circleLoad, 0)
    }
    animationRestore()
  }

  override fun performClick(): Boolean {
    super.performClick()
    return true
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    loadingButtonBackgroundDraw(canvas)
    loadingButtonProgressDraw(canvas)
    drawText(canvas)
    loadingCircleDraw(canvas)
  }

  private fun drawText(canvas: Canvas) {
    val textYOffset = height / 2 - (paintText.descent() + paintText.ascent()) / 2
    canvas.drawText(title, (width / 2).toFloat(), textYOffset, paintText)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
    val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
    val h: Int = resolveSizeAndState(
      MeasureSpec.getSize(w), heightMeasureSpec, 0
    )
    widthSize = w
    heightSize = h
    setMeasuredDimension(w, h)
  }

  private fun loadingCircleDraw(canvas: Canvas) {
    loadingCirclePaint.color = circleLoading
    val bounds = Rect()
    paintText.getTextBounds(title, 0, title.length, bounds)
    val oval = calculateCircleRect(height / 4F, bounds)
    canvas.drawArc(oval, 0F, circleProgress, true, loadingCirclePaint)
  }

  private fun calculateCircleRect(centerCircle: Float, rectBounds: Rect): RectF {
    val left = widthSize / 2F + rectBounds.right / 2F + 10
    val right = left + 2 * centerCircle
    val top = heightSize / 2F - centerCircle
    val bottom = heightSize / 2F + centerCircle
    return RectF(left, top, right, bottom)
  }

  private fun loadingButtonProgressDraw(canvas: Canvas) {
    with(actionButtonPaint) {
      color = buttonColorTwo
      canvas.drawRect(0F, 0F, downloadProgress, heightSize.toFloat(), this)
      reset()
    }
  }

  fun startAllAnimation() {
    title = context.getString(R.string.button_loading)
    animator.apply {
      playTogether(circleAnimation, horizontalAnimation)
      duration = ANIMATION_DURATION
      addListener(onStart = {
        circleAnimation.repeatCount = CIRCLE_ANIMATION_REPEAT_COUNT
        horizontalAnimation.repeatCount = HORIZONTAL_ANIMATION_REPEAT_COUNT
      }, onEnd = {
        animationRestore()
      })
      start()
    }
  }

  private fun loadingButtonBackgroundDraw(canvas: Canvas) {
    with(actionButtonPaint) {
      color = buttonColorOne
      canvas.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), this)
    }
  }

  fun stopAllAnimation() {
    circleAnimation.repeatCount = 0
    horizontalAnimation.repeatCount = 0
  }

  private fun animationRestore() {
    with(actionButtonPaint) {
      color = context.getColor(R.color.colorPrimary)
    }
    downloadProgress = 0F
    circleProgress = 0F
    title = context.getString(R.string.button_name)
    invalidate()
  }
}
