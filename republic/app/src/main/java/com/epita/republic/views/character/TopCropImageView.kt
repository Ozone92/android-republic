package com.epita.republic.views.character

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

// Comme centerCrop, mais garde le haut de l'image : le visage reste visible même dans une case très large
class TopCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    // 0 = coller en haut, 0.5 = centré
    private val verticalBias = 0.15f

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        updateMatrix()
        return changed
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        updateMatrix()
    }

    private fun updateMatrix() {
        val image = drawable ?: return
        val viewWidth = width - paddingLeft - paddingRight
        val viewHeight = height - paddingTop - paddingBottom
        if (viewWidth <= 0 || viewHeight <= 0 || image.intrinsicWidth <= 0 || image.intrinsicHeight <= 0) {
            return
        }

        val scale = maxOf(
            viewWidth.toFloat() / image.intrinsicWidth,
            viewHeight.toFloat() / image.intrinsicHeight
        )
        val dx = (viewWidth - image.intrinsicWidth * scale) / 2f
        val dy = (viewHeight - image.intrinsicHeight * scale) * verticalBias

        imageMatrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }
    }
}
