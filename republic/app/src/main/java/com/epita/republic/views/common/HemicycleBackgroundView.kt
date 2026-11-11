package com.epita.republic.views.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class HemicycleBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundColor = MaterialColors.getColor(
        context, android.R.attr.colorBackground, Color.WHITE
    )
    private val seatColor = MaterialColors.getColor(
        context, com.google.android.material.R.attr.colorOutlineVariant, Color.LTGRAY
    )
    private val leftColor = MaterialColors.getColor(
        context, com.google.android.material.R.attr.colorSecondary, Color.RED
    )
    private val centerColor = MaterialColors.getColor(
        context, com.google.android.material.R.attr.colorTertiary, Color.YELLOW
    )
    private val rightColor = MaterialColors.getColor(
        context, com.google.android.material.R.attr.colorPrimary, Color.BLUE
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var seatX = FloatArray(0)
    private var seatY = FloatArray(0)
    private var seatSide = FloatArray(0)
    private var litColor = IntArray(0)
    private var litStart = LongArray(0)
    private var pressStart = LongArray(0)
    private var seatRadius = 0f
    private var lastSpawn = 0L

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildSeats(w, h)
    }

    private fun buildSeats(w: Int, h: Int) {
        val centerX = w / 2f
        val centerY = h * 0.97f
        val outerRadius = min(w * 0.47f, h * 0.9f)
        val innerRadius = outerRadius * INNER_RATIO
        val rowGap = (outerRadius - innerRadius) / (ROWS - 1)
        seatRadius = rowGap * SEAT_RATIO

        val xs = mutableListOf<Float>()
        val ys = mutableListOf<Float>()
        val sides = mutableListOf<Float>()
        for (row in 0 until ROWS) {
            val radius = innerRadius + row * rowGap
            val count = floor(PI * radius / rowGap).toInt() + 1
            for (i in 0 until count) {
                val angle = PI - PI * i / (count - 1)
                xs.add(centerX + (radius * cos(angle)).toFloat())
                ys.add(centerY - (radius * sin(angle)).toFloat())
                sides.add(cos(angle).toFloat())
            }
        }

        seatX = xs.toFloatArray()
        seatY = ys.toFloatArray()
        seatSide = sides.toFloatArray()
        litColor = IntArray(seatX.size)
        litStart = LongArray(seatX.size)
        pressStart = LongArray(seatX.size)
    }

    private fun colorForSeat(index: Int): Int {
        val side = seatSide[index] + Random.nextFloat() - 0.5f
        return when {
            side < -SIDE_THRESHOLD -> leftColor
            side > SIDE_THRESHOLD -> rightColor
            else -> centerColor
        }
    }

    private fun light(index: Int, now: Long) {
        litColor[index] = colorForSeat(index)
        litStart[index] = now
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)
        if (seatX.isEmpty()) return

        val animate = ValueAnimator.areAnimatorsEnabled()
        val now = SystemClock.uptimeMillis()

        if (animate) {
            if (lastSpawn == 0L) lastSpawn = now
            while (now - lastSpawn >= SPAWN_INTERVAL_MS) {
                light(Random.nextInt(seatX.size), now)
                lastSpawn += SPAWN_INTERVAL_MS
            }
        }

        for (i in seatX.indices) {
            var radius = seatRadius
            if (pressStart[i] != 0L) {
                val progress = (now - pressStart[i]).toFloat() / PRESS_DURATION_MS
                if (progress >= 1f) {
                    pressStart[i] = 0L
                } else {
                    radius *= 1f + PRESS_SCALE * (1f - progress)
                }
            }

            paint.color = seatColor
            canvas.drawCircle(seatX[i], seatY[i], radius, paint)

            if (litStart[i] != 0L) {
                val progress = (now - litStart[i]).toFloat() / LIT_DURATION_MS
                if (progress >= 1f || !animate) {
                    litStart[i] = 0L
                } else {
                    val alpha = (sin(PI * progress) * 255).toInt()
                    paint.color = ColorUtils.setAlphaComponent(litColor[i], alpha)
                    canvas.drawCircle(seatX[i], seatY[i], radius, paint)
                }
            }
        }

        if (animate) postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val now = SystemClock.uptimeMillis()
                val reach = seatRadius * TOUCH_REACH
                for (i in seatX.indices) {
                    val dx = seatX[i] - event.x
                    val dy = seatY[i] - event.y
                    if (dx * dx + dy * dy <= reach * reach) {
                        light(i, now)
                        pressStart[i] = now
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    companion object {
        private const val ROWS = 11
        private const val INNER_RATIO = 0.36f
        private const val SEAT_RATIO = 0.3f
        private const val SIDE_THRESHOLD = 0.35f
        private const val SPAWN_INTERVAL_MS = 90L
        private const val LIT_DURATION_MS = 2600L
        private const val PRESS_DURATION_MS = 450L
        private const val PRESS_SCALE = 0.8f
        private const val TOUCH_REACH = 4f
    }
}
