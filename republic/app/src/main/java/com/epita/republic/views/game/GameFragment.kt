package com.epita.republic.views.game

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.epita.republic.R
import com.epita.republic.domain.DiceFace
import com.epita.republic.repository.Player


class GameFragment : Fragment() {
    val gameViewModel: GameViewModel by viewModels()

    lateinit var playerCards: Array<PlayerCard>
    lateinit var middleCard: PlayerCard
    lateinit var turnPhaseTextView: TextView

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

        turnPhaseTextView = view.findViewById(R.id.game_phase_text)

        playerCards = arrayOf(
            PlayerCard(
                requireContext(),
                0,
                gameViewModel.players[0],
                view.findViewById(R.id.game_player1_card),
                view.findViewById(R.id.game_player1_name_text),
                view.findViewById(R.id.game_player1_influence_text),
                view.findViewById(R.id.game_player1_popularity_text),
                view.findViewById(R.id.game_player1_money_text),
                view.findViewById(R.id.game_player1_vote_text),
            ),
            PlayerCard(
                requireContext(),
                1,
                gameViewModel.players[1],
                view.findViewById(R.id.game_player2_card),
                view.findViewById(R.id.game_player2_name_text),
                view.findViewById(R.id.game_player2_influence_text),
                view.findViewById(R.id.game_player2_popularity_text),
                view.findViewById(R.id.game_player2_money_text),
                view.findViewById(R.id.game_player2_vote_text),
            ),
            PlayerCard(
                requireContext(),
                2,
                gameViewModel.players[2],
                view.findViewById(R.id.game_player3_card),
                view.findViewById(R.id.game_player3_name_text),
                view.findViewById(R.id.game_player3_influence_text),
                view.findViewById(R.id.game_player3_popularity_text),
                view.findViewById(R.id.game_player3_money_text),
                view.findViewById(R.id.game_player3_vote_text),
            ),
            PlayerCard(
                requireContext(),
                3,
                gameViewModel.players[3],
                view.findViewById(R.id.game_player4_card),
                view.findViewById(R.id.game_player4_name_text),
                view.findViewById(R.id.game_player4_influence_text),
                view.findViewById(R.id.game_player4_popularity_text),
                view.findViewById(R.id.game_player4_money_text),
                view.findViewById(R.id.game_player4_vote_text),
            ),
        )

        middleCard = PlayerCard(
            requireContext(),
            0,
            null,
            view.findViewById(R.id.game_middle_player_card),
            view.findViewById(R.id.game_middle_player_name_text),
            view.findViewById(R.id.game_middle_player_influence_text),
            view.findViewById(R.id.game_middle_player_popularity_text),
            view.findViewById(R.id.game_middle_player_money_text),
            view.findViewById(R.id.game_middle_player_vote_text),
        )
        renderPlayers()
        renderTurnPhase()

        val diceView: CardView = view.findViewById(R.id.game_dice_card)
        val stayLeaveView: CardView = view.findViewById(R.id.game_stay_leave_card)
        val diceButtons: Array<Button> = arrayOf(
            view.findViewById(R.id.game_dice1_btn),
            view.findViewById(R.id.game_dice2_btn),
            view.findViewById(R.id.game_dice3_btn),
            view.findViewById(R.id.game_dice4_btn),
            view.findViewById(R.id.game_dice5_btn),
            view.findViewById(R.id.game_dice6_btn)
        )
        for ((index, diceButton) in diceButtons.withIndex()) {
            diceButton.setOnClickListener {
                if (gameViewModel.diceButtonClick(index)) {
                    diceButton.setBackgroundColor(resources.getColor(R.color.dice_unlock, null))
                } else {
                    diceButton.setBackgroundColor(resources.getColor(R.color.dice_lock, null))
                }
            }
        }

        val diceRerollBtn: Button = view.findViewById(R.id.game_dice_reroll)
        diceRerollBtn.setOnClickListener { gameViewModel.rollDice() }
        gameViewModel.rerollLeft.observe(this.viewLifecycleOwner) { rerollLeft ->
            diceRerollBtn.text = getString(R.string.game_reroll_text, rerollLeft)
        }

        val diceConfirmBtn: Button = view.findViewById(R.id.game_dice_confirm)
        diceConfirmBtn.setOnClickListener { gameViewModel.signalController.sendNotify() }

        gameViewModel.currentPlayerTurnIndex.observe(this.viewLifecycleOwner) { renderTurnPhase() }
        gameViewModel.inCity.observe(this.viewLifecycleOwner) { renderPlayers() }
        gameViewModel.dices.observe(this.viewLifecycleOwner) { newDices ->
            renderDice(diceButtons, newDices)
        }

        val stayBtn: Button = view.findViewById(R.id.game_stay_btn)
        stayBtn.setOnClickListener {
            gameViewModel.signalController.sendNotify()
        }

        val leaveBtn: Button = view.findViewById(R.id.game_leave_btn)
        leaveBtn.setOnClickListener {
            gameViewModel.playerExitCityClick()
        }

        gameViewModel.currentPhase.observe(this.viewLifecycleOwner) { newPhase ->
            Log.d("GAME", newPhase.name)
            renderTurnPhase()
            renderPlayers()
            val isPlayerTurn = !gameViewModel.currentPlayer.value!!.isBot

            diceView.visibility = View.INVISIBLE
            stayLeaveView.visibility = View.INVISIBLE
            if (newPhase == GamePhase.DICE) {
                diceRerollBtn.isActivated = isPlayerTurn
                diceConfirmBtn.isActivated = isPlayerTurn

                diceButtons.forEach { btn ->
                    btn.setBackgroundColor(
                        resources.getColor(
                            R.color.dice_unlock,
                            null
                        )
                    )
                }
                diceView.visibility = View.VISIBLE
            } else if (newPhase == GamePhase.ASK_LEAVE) {
                stayBtn.isActivated = isPlayerTurn
                leaveBtn.isActivated = isPlayerTurn
                stayLeaveView.visibility = View.VISIBLE
            }
        }
    }

    fun renderPlayers() {
        middleCard.setVisibility(View.INVISIBLE)
        for (playerCard in playerCards) {
            val player = playerCard.getPlayer()!!
            if (player == gameViewModel.inCity.value) {
                middleCard.setPlayer(player)
                middleCard.setVisibility(View.VISIBLE)
                playerCard.setVisibility(View.INVISIBLE)
            } else {
                playerCard.setVisibility(View.VISIBLE)
                playerCard.render()
            }
        }
    }

    fun renderTurnPhase() {
        turnPhaseTextView.text = getString(
            R.string.game_phase_text,
            gameViewModel.currentPlayerTurnIndex.value,
            gameViewModel.currentPhase.value.toString()
        )
    }

    fun renderDice(buttons: Array<Button>, diceFaces: Array<DiceFace>) {
        for ((index, button) in buttons.withIndex()) {
            button.text = diceFaces[index].name.slice(0..1)
        }
    }
}