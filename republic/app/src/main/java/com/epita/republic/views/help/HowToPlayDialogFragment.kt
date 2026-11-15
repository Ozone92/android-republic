package com.epita.republic.views.help

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.epita.republic.R
import kotlin.math.min

class HowToPlayDialogFragment : DialogFragment(R.layout.dialog_how_to_play) {

    private class Page(
        @param:DrawableRes val icon: Int,
        @param:StringRes val title: Int,
        @param:StringRes val body: Int
    )

    private val pages = listOf(
        Page(R.drawable.stat_vote, R.string.help_goal_title, R.string.help_goal_body),
        Page(R.drawable.dice_three, R.string.help_dice_title, R.string.help_dice_body),
        Page(R.drawable.ic_crown, R.string.help_elysee_title, R.string.help_elysee_body),
        Page(R.drawable.stat_influence, R.string.help_influence_title, R.string.help_influence_body),
        Page(R.drawable.dice_money, R.string.help_shop_title, R.string.help_shop_body)
    )

    private var pageIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pageIndex = savedInstanceState?.getInt(STATE_PAGE) ?: 0

        val icon = view.findViewById<ImageView>(R.id.help_icon)
        val title = view.findViewById<TextView>(R.id.help_title)
        val body = view.findViewById<TextView>(R.id.help_body)
        val pageLabel = view.findViewById<TextView>(R.id.help_page)
        val previous = view.findViewById<Button>(R.id.help_previous)
        val next = view.findViewById<Button>(R.id.help_next)

        fun render() {
            val page = pages[pageIndex]
            icon.setImageResource(page.icon)
            title.setText(page.title)
            body.setText(page.body)
            pageLabel.text = getString(R.string.help_page, pageIndex + 1, pages.size)
            previous.isEnabled = pageIndex > 0
            next.setText(if (pageIndex == pages.lastIndex) R.string.help_done else R.string.help_next)
        }

        previous.setOnClickListener {
            if (pageIndex > 0) {
                pageIndex--
                render()
            }
        }
        next.setOnClickListener {
            if (pageIndex < pages.lastIndex) {
                pageIndex++
                render()
            } else {
                dismiss()
            }
        }
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_PAGE, pageIndex)
    }

    override fun onStart() {
        super.onStart()
        val metrics = resources.displayMetrics
        val maxWidth = resources.getDimensionPixelSize(R.dimen.size_dialog_max_width)
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                min(maxWidth, (metrics.widthPixels * DIALOG_WIDTH_RATIO).toInt()),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    companion object {
        private const val TAG = "how_to_play"
        private const val STATE_PAGE = "page"
        private const val PREFERENCES = "help"
        private const val KEY_SEEN = "seen"
        private const val DIALOG_WIDTH_RATIO = 0.9f

        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                HowToPlayDialogFragment().show(fragmentManager, TAG)
            }
        }

        fun showOnce(context: Context, fragmentManager: FragmentManager) {
            val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            if (preferences.getBoolean(KEY_SEEN, false)) return
            preferences.edit().putBoolean(KEY_SEEN, true).apply()
            show(fragmentManager)
        }
    }
}
