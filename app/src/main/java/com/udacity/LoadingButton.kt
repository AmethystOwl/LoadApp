package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var progressBackgroundColor: Int = 0
    private var barBackgroundColor: Int = 0
    private var customTextSize : Float = 0.0f
    private val cornersRadius = 20f
    private lateinit var circleValueAnimator: ValueAnimator
    private lateinit var barValueAnimator: ValueAnimator

    private var text: String = ""
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val r = Rect()
    private var circleProgress = 0f
    private var barProgress = 0f

    private var buttonState: ButtonState by Delegates.observable(
        ButtonState.Completed,
        object : (KProperty<*>, ButtonState, ButtonState) -> Unit {
            override fun invoke(p: KProperty<*>, old: ButtonState, new: ButtonState) {
                when (new) {
                    is ButtonState.Loading -> {
                        isClickable = false
                        startAnimations()

                    }
                    is ButtonState.Completed -> {
                        isClickable = true
                    }
                }

            }

        })

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            // TODO: set color = lbBkcolor and font
            barBackgroundColor = getColor(R.styleable.LoadingButton_lbBackgroundColor,0)
            progressBackgroundColor = getColor(R.styleable.LoadingButton_lbProgressBackgroundColor,0)
            customTextSize = getFloat(R.styleable.LoadingButton_lbTextSize,0.0f)
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (buttonState) {
            is ButtonState.Loading -> drawLoading(canvas)
            is ButtonState.Completed -> drawCompleted(canvas)
            else -> drawCompleted(canvas)
        }

    }


    private fun drawCompleted(canvas: Canvas?) {
        canvas?.let {
            paint.color = barBackgroundColor


            canvas.drawRoundRect(
                0f,
                0f,
                widthSize.toFloat(),
                heightSize.toFloat(),
                cornersRadius,
                cornersRadius,
                paint
            )
            paint.reset()
            canvas.getClipBounds(r)
            val cHeight: Int = r.height()
            val cWidth: Int = r.width()
            paint.color = Color.WHITE
            paint.textSize = customTextSize
            text = context.getString(R.string.button_download)
            paint.textAlign = Paint.Align.LEFT
            paint.getTextBounds(text, 0, text.length, r)
            val x: Float = cWidth / 2f - r.width() / 2f - r.left
            val y: Float = cHeight / 2f + r.height() / 2f - r.bottom
            canvas.drawText(text, x, y, paint)
        }

    }

    private fun drawLoading(canvas: Canvas?) {
        canvas?.let {
            paint.textSize = customTextSize
            text = context.getString(R.string.button_loading)
            paint.color = barBackgroundColor
            canvas.drawRoundRect(
                0f,
                0f,
                widthSize.toFloat(),
                heightSize.toFloat(),
                cornersRadius,
                cornersRadius,
                paint
            )

            paint.reset()


            //bar progress
            paint.color = progressBackgroundColor
            canvas.drawRoundRect(
                0f,
                0f,
                barProgress,
                heightSize.toFloat(),
                cornersRadius,
                cornersRadius,
                paint
            )
            paint.reset()

            //circle progress
            canvas.getClipBounds(r)
            val cHeight: Int = r.height()
            val cWidth: Int = r.width()
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = customTextSize
            paint.isAntiAlias = true
            paint.getTextBounds(text, 0, text.length, r)
            val x: Float = cWidth / 2f - r.width() / 2f - r.left
            val y: Float = cHeight / 2f + r.height() / 2f - r.bottom
            canvas.drawText(text, x - 20, y, paint)

            canvas.drawArc(
                (r.right + x + 25),
                (r.top + y - 25),
                (r.right + x + 100),
                (r.bottom + y + 25),
                0f,
                circleProgress,
                true,
                paint
            )

        }
    }

    private fun startAnimations() {
        // circle
        circleValueAnimator = ValueAnimator.ofFloat(0f, 360f)
        circleValueAnimator.apply {
            duration = 5000
            addUpdateListener {
                circleProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
        // bar
        barValueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat())
        barValueAnimator.apply {
            duration = 5000
            addUpdateListener {
                barProgress = it.animatedValue as Float
                invalidate()
            }
            start()
            doOnEnd {
                buttonState = ButtonState.Completed
            }
        }
    }

    fun seekToEnd() {
        if(::barValueAnimator.isInitialized && ::circleValueAnimator.isInitialized) {
            barValueAnimator.end()
            circleValueAnimator.end()
        }
    }

    override fun performClick(): Boolean {
        buttonState = ButtonState.Loading
        return super.performClick()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthSize = w
        heightSize = h
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}