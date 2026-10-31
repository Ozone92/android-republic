package com.epita.republic.views.game

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.epita.republic.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DiceLegendDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
        val rows = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(24), dp(8), dp(24), dp(8))
        }
        val faces = listOf(
            R.drawable.dice_one to R.string.dice_value_one,
            R.drawable.dice_two to R.string.dice_value_two,
            R.drawable.dice_three to R.string.dice_value_three,
            R.drawable.dice_money to R.string.dice_face_money,
            R.drawable.dice_damage to R.string.dice_face_damage,
            R.drawable.dice_health to R.string.dice_face_health
        )
        faces.chunked(3).forEach { group ->
            val column = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }
            rows.addView(column, LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ))
            group.forEach { (image, label) ->
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(6), 0, dp(6))
                }
                row.addView(ImageView(context).apply {
                    setImageResource(image)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }, LinearLayout.LayoutParams(dp(44), dp(44)))
                row.addView(TextView(context).apply {
                    text = getString(label)
                    textSize = 18f
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = dp(20) })
                column.addView(row)
            }
        }
        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dice_legend_title)
            .setView(ScrollView(context).apply { addView(rows) })
            .setPositiveButton(R.string.dice_legend_close, null)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8f).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "DiceLegendDialog"
    }
}
