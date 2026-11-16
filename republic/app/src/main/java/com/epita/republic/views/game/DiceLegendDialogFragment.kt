package com.epita.republic.views.game

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.epita.republic.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DiceLegendDialogFragment : DialogFragment() {
    private data class Entry(val images: List<Int>, val title: Int, val effect: Int)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
        val columns = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(24), dp(8), dp(24), dp(8))
        }
        val entries = listOf(
            Entry(
                listOf(R.drawable.dice_one, R.drawable.dice_two, R.drawable.dice_three),
                R.string.dice_legend_numbers,
                R.string.dice_legend_numbers_effect
            ),
            Entry(listOf(R.drawable.dice_money), R.string.dice_face_money, R.string.dice_legend_money_effect),
            Entry(listOf(R.drawable.dice_health), R.string.dice_face_health, R.string.dice_legend_health_effect),
            Entry(listOf(R.drawable.dice_damage), R.string.dice_face_damage, R.string.dice_legend_damage_effect)
        )
        entries.chunked(2).forEachIndexed { index, group ->
            val column = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }
            columns.addView(column, LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { if (index > 0) marginStart = dp(24) })
            group.forEach { entry ->
                val icons = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, dp(8), 0, dp(4))
                }
                entry.images.forEach { image ->
                    icons.addView(ImageView(context).apply {
                        setImageResource(image)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    }, LinearLayout.LayoutParams(dp(40), dp(40)).apply { marginEnd = dp(6) })
                }
                icons.addView(TextView(context).apply {
                    text = getString(entry.title)
                    setTextAppearance(R.style.TextAppearance_Republic_Title)
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = dp(6)
                    gravity = android.view.Gravity.CENTER_VERTICAL
                })
                column.addView(icons)
                column.addView(TextView(context).apply {
                    text = getString(entry.effect)
                    setTextAppearance(R.style.TextAppearance_Republic_Body)
                })
            }
        }
        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dice_legend_title)
            .setView(ScrollView(context).apply { addView(columns) })
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
