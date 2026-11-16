package com.epita.republic.views.game

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.epita.republic.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class QuitGameDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.game_quit_title)
            .setMessage(R.string.game_quit_message)
            .setNegativeButton(R.string.game_quit_cancel, null)
            .setPositiveButton(R.string.game_quit_confirm) { _, _ ->
                setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
            }
            .create()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? AlertDialog ?: return
        val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        button.setTextColor(MaterialColors.getColor(button, com.google.android.material.R.attr.colorError))
    }

    companion object {
        const val TAG = "QuitGameDialog"
        const val REQUEST_KEY = "quit_game"

        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                QuitGameDialogFragment().show(fragmentManager, TAG)
            }
        }
    }
}
