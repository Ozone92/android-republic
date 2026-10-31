package com.epita.republic.views.game

import android.os.Bundle
import android.content.res.ColorStateList
import com.google.android.material.button.MaterialButton
import androidx.core.view.ViewCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.epita.republic.R
import com.epita.republic.domain.DiceFace
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epita.republic.views.ending.EndingFragmentParameter

class GameFragment : Fragment() {
    val gameViewModel: GameViewModel by viewModels()
    private val args: GameFragmentArgs by navArgs()
    private val characters by lazy {
        arrayOf(args.player1, args.player2, args.player3, args.player4)
    }

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
        view.findViewById<Button>(R.id.game_power_shop_btn).setOnClickListener {
            if (childFragmentManager.findFragmentByTag(PowerShopDialogFragment.TAG) == null) {
                PowerShopDialogFragment().show(childFragmentManager, PowerShopDialogFragment.TAG)
            }
        }


        turnPhaseTextView = view.findViewById(R.id.game_phase_text)

        playerCards = arrayOf(
            PlayerCard(
                requireContext(),
                0,
                gameViewModel.players[0],
                characters[0],
                view.findViewById(R.id.game_player1_card),
                view.findViewById(R.id.game_player1_image),
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
                characters[1],
                view.findViewById(R.id.game_player2_card),
                view.findViewById(R.id.game_player2_image),
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
                characters[2],
                view.findViewById(R.id.game_player3_card),
                view.findViewById(R.id.game_player3_image),
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
                characters[3],
                view.findViewById(R.id.game_player4_card),
                view.findViewById(R.id.game_player4_image),
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
            null,
            view.findViewById(R.id.game_middle_player_card),
            view.findViewById(R.id.game_middle_player_image),
            view.findViewById(R.id.game_middle_player_name_text),
            view.findViewById(R.id.game_middle_player_influence_text),
            view.findViewById(R.id.game_middle_player_popularity_text),
            view.findViewById(R.id.game_middle_player_money_text),
            view.findViewById(R.id.game_middle_player_vote_text),
            isMiddle = true
        )
        renderPlayers()
        renderTurnPhase()
        gameViewModel.currentPlayer.observe(viewLifecycleOwner) { renderPlayers() }


        view.findViewById<View>(R.id.game_dice_info).setOnClickListener {
            if (childFragmentManager.findFragmentByTag(DiceLegendDialogFragment.TAG) == null) {
                DiceLegendDialogFragment().show(childFragmentManager, DiceLegendDialogFragment.TAG)
            }
        }

        val diceView: CardView = view.findViewById(R.id.game_dice_card)
        val stayLeaveView: CardView = view.findViewById(R.id.game_stay_leave_card)
        val influenceView: CardView = view.findViewById(R.id.game_influence_choice_card)
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
                gameViewModel.diceButtonClick(index)
                renderDicesColor(diceButtons)
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

        val recyclerView: RecyclerView = view.findViewById(R.id.game_influence_choice_recycler)
        setupInfluenceRecycler(recyclerView)
        gameViewModel.availableInfluenceCards.observe(this.viewLifecycleOwner) { cards ->
            val adapter = InfluenceCardAdapter(requireContext(), cards, { cardIndex ->
                gameViewModel.influenceCardChoiceClick(cardIndex)
            })
            recyclerView.adapter = adapter
        }

        gameViewModel.currentPhase.observe(this.viewLifecycleOwner) { newPhase ->
            Log.d("GAME", newPhase.name)
            renderTurnPhase()
            renderPlayers()
            val isPlayerTurn = !gameViewModel.currentPlayer.value!!.isBot

            diceView.visibility = View.INVISIBLE
            stayLeaveView.visibility = View.INVISIBLE
            influenceView.visibility = View.INVISIBLE
            when (newPhase) {
                GamePhase.DICE, GamePhase.IMPROVE_DICE -> {
                    diceRerollBtn.isEnabled = isPlayerTurn
                    diceConfirmBtn.isEnabled = isPlayerTurn

                    renderDicesColor(diceButtons)
                    diceView.visibility = View.VISIBLE
                }

                GamePhase.ASK_LEAVE -> {
                    stayLeaveView.visibility = View.VISIBLE
                }

                GamePhase.GAME_FINISH -> {
                    val playersWinningOrder = gameViewModel.getWinningPlayerOrder()
                    val result = playersWinningOrder.map { player ->
                        EndingFragmentParameter(player.playerIndex, player.popularity, player.vote, player.isBot, characters[player.playerIndex])
                    }.toTypedArray()
                    val action = GameFragmentDirections.actionGameFragmentToEndingFragment(result)
                    findNavController().navigate(action)
                }


                GamePhase.INFLUENCE -> {
                    influenceView.visibility = View.VISIBLE
                }

                GamePhase.SHOP -> {} // TODO()
            }
        }
    }

    fun setupInfluenceRecycler(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    fun renderPlayers() {
        middleCard.setVisibility(View.INVISIBLE)
        for (playerCard in playerCards) {
            val player = playerCard.getPlayer()!!
            if (player == gameViewModel.inCity.value) {
                middleCard.changePlayer(player, playerCard.getIndex(), playerCard.getCharacter())
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
            gameViewModel.currentPlayerTurnIndex.value!! + 1,
            gameViewModel.currentPhase.value.toString()
        )
    }

    fun renderDice(buttons: Array<Button>, diceFaces: Array<DiceFace>) {
        for ((index, button) in buttons.withIndex()) {
            val face = diceFaces[index]
            val iconRes = when (face) {
                DiceFace.ONE -> R.drawable.dice_one
                DiceFace.TWO -> R.drawable.dice_two
                DiceFace.THREE -> R.drawable.dice_three
                DiceFace.MONEY -> R.drawable.dice_money
                DiceFace.DAMAGE -> R.drawable.dice_damage
                DiceFace.HEAL -> R.drawable.dice_health
            }
            (button as MaterialButton).apply {
                icon = androidx.appcompat.content.res.AppCompatResources.getDrawable(context, iconRes)
                iconTint = null
                text = ""
                contentDescription = getString(when (face) {
                    DiceFace.ONE -> R.string.dice_face_one
                    DiceFace.TWO -> R.string.dice_face_two
                    DiceFace.THREE -> R.string.dice_face_three
                    DiceFace.MONEY -> R.string.dice_face_money
                    DiceFace.DAMAGE -> R.string.dice_face_damage
                    DiceFace.HEAL -> R.string.dice_face_health
                })
            }
        }
        renderDicesColor(buttons)
    }

    fun renderDicesColor(diceButtons: Array<Button>) {
        diceButtons.withIndex().forEach { (index, button) ->
            val locked = gameViewModel.lockedDiceIndexes.contains(index)
            val color = resources.getColor(
                if (locked) R.color.dice_lock else R.color.dice_unlock, null
            )
            button.setTextColor(color)
            (button as MaterialButton).strokeColor = ColorStateList.valueOf(color)
            ViewCompat.setStateDescription(button, getString(
                if (locked) R.string.dice_locked else R.string.dice_unlocked
            ))
        }
    }
}
