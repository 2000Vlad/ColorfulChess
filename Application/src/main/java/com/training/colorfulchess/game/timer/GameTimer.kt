package com.training.colorfulchess.game.timer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.TimeUtils
import android.view.View
import android.view.View.MeasureSpec.*
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import com.training.colorfulchess.R
import java.lang.IllegalArgumentException
import kotlin.math.floor
import kotlin.math.min

class GameTimer(context: Context, attrs: AttributeSet) : View(context, attrs) {

    lateinit var listener: TimerStateListener
    private var timeAnimator: ValueAnimator = ValueAnimator.ofFloat(60f, 0f)
    private var colorAnimator: ValueAnimator = ValueAnimator()


    fun start() {

        timeAnimator = ValueAnimator.ofFloat(60f, 0f)
        timeAnimator.interpolator = LinearInterpolator()
        timeAnimator.duration = MILLIS_PER_SECOND * SECONDS_PER_MINUTE
        timeAnimator.repeatCount = 0
        timeAnimator.addUpdateListener {
            time = it.animatedValue as Float
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


            }

        })
        timeAnimator.start()


        val green = Color.parseColor("#00FF00")
        val yellow = Color.parseColor("#FFFF00")
        val red = Color.parseColor("#FF0000")
        colorAnimator = ValueAnimator.ofArgb(green, yellow, red)
        colorAnimator.interpolator = LinearInterpolator()
        colorAnimator.repeatCount = 0
        colorAnimator.duration = MILLIS_PER_SECOND * SECONDS_PER_MINUTE
        colorAnimator.addUpdateListener {
            val colorInt = it.animatedValue as Int
            sweepColor = colorInt
        }
        colorAnimator.start()

    }

    fun reset() {
        timeAnimator.cancel()
        time = 0f
        colorAnimator.cancel()
        sweepColor = 0
    }

    private var sweepColor: Int = 0
    private var time: Float = 0f
        set(value) {
            field = value

        }
    private val timeText: String
        get() {
            val timeInt = floor(time).toInt()
            return timeInt.toString()
        }
    private val sweep: Float
        get() =
            if (time > 0) (60f - time) * 6f
            else 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        width = when (widthMode) {
            AT_MOST -> {
                min(widthSize, context.resources.getDimension(R.dimen.timer_size).toInt())
            }

            UNSPECIFIED -> {
                context.resources.getDimension(R.dimen.timer_size).toInt()
            }
            EXACTLY -> {
                min(widthSize, context.resources.getDimension(R.dimen.timer_size).toInt())
            }
            else -> context.resources.getDimension(R.dimen.timer_size).toInt() // To satisfy the compiler

        }
        height = when (heightMode) {
            AT_MOST -> {
                min(heightSize, (width * HEIGHT_PER_WIDTH).toInt())
            }

            UNSPECIFIED -> {
                width * HEIGHT_PER_WIDTH.toInt()
            }
            EXACTLY -> {
                min(heightSize, (width * HEIGHT_PER_WIDTH).toInt())
            }
            else -> (width * HEIGHT_PER_WIDTH).toInt() // To satisfy the compiler

        }
        setMeasuredDimension(width, height)
    }

    val timerDrawable = context.getDrawable(R.drawable.ic_timer)!!
    val paint = Paint()
    val drawRect: RectF = RectF()
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return


        drawRect.left = 0f
        drawRect.right = width.toFloat()
        drawRect.top = height - width.toFloat()
        drawRect.bottom = height.toFloat()
        paint.color = sweepColor
        canvas.drawArc(drawRect, 270f, sweep, true, paint)
        timerDrawable.setBounds(0, 0, width, height)
        timerDrawable.draw(canvas)
        paint.color = Color.BLACK
        val textX: Float
        val textY: Float
        textX = if (timeText.length == 1) {
            width / 2.5f
        } else {
            width / 3f
        }
        textY = height - (width / 2.5f)
        paint.textSize = 48f
        canvas.drawText(timeText, textX, textY, paint)

    }

    interface TimerStateListener {
        fun onTimerStopped() {

        }
    }

}

fun GameTimer.doOnEnd(function : () -> Unit) {
    listener = object  : GameTimer.TimerStateListener {
        override fun onTimerStopped() {
            function()
        }
    }
}

const val MILLIS_PER_SECOND: Long = 1000
const val SECONDS_PER_MINUTE: Long = 60
const val HEIGHT_PER_WIDTH: Float = 1.11f //Ideal size ratio