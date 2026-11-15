package com.epita.republic.views.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.epita.republic.R
import com.google.android.material.color.MaterialColors
import kotlin.math.min

class HemicycleArcsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.border_arc)
        color = MaterialColors.getColor(
            context, com.google.android.material.R.attr.colorOutlineVariant, Color.LTGRAY
        )
    }
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height * CENTER_Y_RATIO
        val outer = min(width * OUTER_WIDTH_RATIO, height * OUTER_HEIGHT_RATIO)
        val inner = outer * INNER_RATIO
        for (i in 0 until ARCS) {
            val radius = inner + (outer - inner) * i / (ARCS - 1)
            rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            canvas.drawArc(rect, START_ANGLE, SWEEP_ANGLE, false, paint)
        }
    }

    companion object {
        private const val ARCS = 5
        private const val CENTER_Y_RATIO = 0.97f
        private const val OUTER_WIDTH_RATIO = 0.47f
        private const val OUTER_HEIGHT_RATIO = 0.9f
        private const val INNER_RATIO = 0.36f
        private const val START_ANGLE = 180f
        private const val SWEEP_ANGLE = 180f
    }
}
