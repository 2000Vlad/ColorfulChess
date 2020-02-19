package com.training.colorfulchess.game.timer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.icu.lang.UCharacter.DecompositionType.SMALL
import android.util.AttributeSet
import android.util.Size
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.android.material.resources.TextAppearance
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min

class RadioTimer(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    lateinit var listener: TimerStateListener
    private var timeAnimator: ValueAnimator = ValueAnimator.ofFloat(60f, 0f)
    private var colorAnimator: ValueAnimator = ValueAnimator()
    var font: Typeface = Typeface.DEFAULT
    private val paint: Paint = Paint()
    var textColor: Int = 0
        set(value){
            field = value
            animTextColor = value
            invalidate()

        }
    private var animTextColor = textColor
    private var time: Float = 0f
    var warnTime: Float = 30f
    private var warningAnimationStarted = false

    var timeSpan = 60f

    fun start() {
        timeAnimator = ValueAnimator.ofFloat(timeSpan, 0f)
        timeAnimator.interpolator = LinearInterpolator()
        timeAnimator.duration = (MILLIS_PER_SECOND * timeSpan).toLong()
        timeAnimator.repeatCount = 0
        timeAnimator.addUpdateListener {
            time = it.animatedValue as Float
            if(time <= warnTime && !warningAnimationStarted) {
                startWarningAnimation()
            }
            postInvalidate()


        }
        //    animator.addListener(animator.doOnEnd {
        //        val animator = it as ValueAnimator
        //        if(animator.animatedValue == 0)
        //            listener.onTimerStopped()
        //    }
        //    )
        //    animator.addListener(animator.doOnEnd {
        //        val animator = it as ValueAnimator
        //        if(animator.animatedValue == 0)
        //             listener.onTimerStopped()
        //    })

        timeAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                val animator = animation as ValueAnimator
                if (animator.animatedValue == 0f)
                    listener.onTimerStopped()
                    warningAnimationStarted = false


            }

        })
        timeAnimator.start()




    }

    private fun startWarningAnimation() {
        val red = Color.parseColor("#FF0000")
        colorAnimator = ValueAnimator.ofArgb(textColor, red)
        colorAnimator.interpolator = LinearInterpolator()
        colorAnimator.repeatCount = 0
        colorAnimator.duration = (MILLIS_PER_SECOND * warnTime).toLong()
        colorAnimator.addUpdateListener {
            val colorInt = it.animatedValue as Int
            animTextColor = colorInt
        }
        warningAnimationStarted = true
        colorAnimator.start()
    }

    fun stop() {
        timeAnimator.cancel()
        time = 0f
        colorAnimator.cancel()
        animTextColor = textColor
        invalidate()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        val prefferedSize = prefferedSize(80f)
        width = when (widthMode) {
            MeasureSpec.AT_MOST -> {
                min(widthSize, prefferedSize.width)
            }

            MeasureSpec.UNSPECIFIED -> {
                prefferedSize.width
            }
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            else -> prefferedSize.width // To satisfy the compiler

        }
        height = when (heightMode) {
            MeasureSpec.AT_MOST -> {
                min(heightSize, prefferedSize.height)
            }

            MeasureSpec.UNSPECIFIED -> {
               prefferedSize.height
            }
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            else -> (width * HEIGHT_PER_WIDTH).toInt() // To satisfy the compiler

        }
        setMeasuredDimension(width, height)
    }

    private val drawingRect = Rect()
    override fun onDraw(canvas: Canvas?) {
        if(canvas == null){
            return
        }
        drawingRect.top = 0
        drawingRect.left = 0
        drawingRect.right = width
        drawingRect.bottom = height

        paint.color = animTextColor
        paint.textSize = 70f
        paint.typeface = font

        val text = timeText(time)
        canvas.drawText(text, 0f, height.toFloat(), paint)



    }

    private fun timeText(time: Float): String{
        val t = ceil(time).toInt()
        val mins = t / 60
        val secs = t % 60

        val minString = String.format("%02d", mins)
        val secString = String.format("%02d", secs)
        return "$minString:$secString"
    }

    private fun prefferedSize(textSize: Float) : Size {

        paint.textSize = textSize
        val bounds = Rect()
        paint.getTextBounds("00:00",0, 5, bounds)
        return Size(abs(bounds.width()), abs(bounds.height()))
    }

    interface TimerStateListener {
        fun onTimerStopped()
    }
}

fun RadioTimer.doOnEnd(function: () -> Unit) {
    listener = object : RadioTimer.TimerStateListener {
        override fun onTimerStopped() {
            function()
        }
    }
}