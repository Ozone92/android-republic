package com.epita.republic.views.ending

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epita.republic.R


class EndingFragment : Fragment() {
    val endingViewModel: EndingViewModel by viewModels()
    private val args: EndingFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuButton: Button = view.findViewById(R.id.ending_menu_btn)
        menuButton.setOnClickListener {
            findNavController().navigate(EndingFragmentDirections.actionEndingFragmentToHomeFragment())
        }

        val playButton: Button = view.findViewById(R.id.ending_play_btn)
        playButton.setOnClickListener {
            findNavController().navigate(EndingFragmentDirections.actionEndingFragmentToCharacterFragment())
        }

        val winnerImageView: ImageView = view.findViewById(R.id.ending_winner_player_image)
        val winnerNameText: TextView = view.findViewById(R.id.ending_winner_player_text)
        val winnerVictoryText: TextView = view.findViewById(R.id.ending_winner_victory_text)
        endingViewModel.winnerPlayer.observe(this.viewLifecycleOwner) { winner ->
            if (winner != null) {
                winnerImageView.visibility = View.VISIBLE
                winnerNameText.visibility = View.VISIBLE
                winnerVictoryText.visibility = View.VISIBLE

                winnerImageView.setImageResource(winner.character.centerImageRes)
                winnerNameText.text = getString(R.string.game_player_card_name, winner.playerIndex)
                winnerVictoryText.text = getString(winner.endPlayerState.text)
            }
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.ending_recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        endingViewModel.playersLost.observe(this.viewLifecycleOwner) { players ->
            recyclerView.adapter = LostPlayerAdapter(requireContext(), players)
        }

        endingViewModel.parseResult(args.result)
    }
}