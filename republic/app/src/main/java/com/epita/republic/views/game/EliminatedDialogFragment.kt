package com.epita.republic.views.game

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.epita.republic.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EliminatedDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.game_eliminated_title)
            .setMessage(R.string.game_eliminated_message)
            .setNegativeButton(R.string.game_eliminated_menu) { _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf(KEY_MENU to true))
            }
            .setPositiveButton(R.string.game_eliminated_watch) { _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf(KEY_MENU to false))
            }
            .create()
    }

    companion object {
        const val TAG = "EliminatedDialog"
        const val REQUEST_KEY = "eliminated"
        const val KEY_MENU = "menu"

        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                EliminatedDialogFragment().show(fragmentManager, TAG)
            }
        }
    }
}
