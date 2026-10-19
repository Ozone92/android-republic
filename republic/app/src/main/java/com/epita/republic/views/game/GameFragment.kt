package com.epita.republic.views.game

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.epita.republic.R
import com.epita.republic.repository.Player


class GameFragment : Fragment() {
    val gameViewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        gameViewModel.currentPhase.observe(this.viewLifecycleOwner) {}
        gameViewModel.inCity.observe(this.viewLifecycleOwner) {}
    }

    private fun cityChangeHandler(playerInCity: Player?) {}

    private fun gamePhaseChangeHandler(newGamePhase: GamePhase) {

    }
}