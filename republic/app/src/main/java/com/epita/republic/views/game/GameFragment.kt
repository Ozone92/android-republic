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
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import com.epita.republic.R
import com.epita.republic.domain.DiceFace
import com.epita.republic.domain.GamePhase
import com.epita.republic.domain.SHOP_REFRESH_COST
import androidx.navigation.fragment.navArgs

class GameFragment : Fragment() {
    val gameViewModel: GameViewModel by viewModels()
    private val args: GameFragmentArgs by navArgs()

    lateinit var playerCards: Array<PlayerCard>
    lateinit var middleCard: PlayerCard
    lateinit var turnPhaseTextView: TextView
    lateinit var shopView: CardView
    lateinit var shopTitleText: TextView
    lateinit var shopOwnedText: TextView
    lateinit var shopCardButtons: Array<Button>
    lateinit var shopRefreshBtn: Button
    lateinit var diceSpecialLayout: LinearLayout
    lateinit var talkingPointsBtn: Button
    lateinit var spinDoctorBtn: Button

    // Éléments de langage / Spin doctor : le prochain clic sur un dé déclenche l'action
    private var pendingDieAction: ((Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val characterId = args.characterId
        Log.d("CharacterSelection", "Personnage reçu : $characterId")
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
                val action = pendingDieAction
                if (action != null) {
                    pendingDieAction = null
                    action(index)
                } else if (gameViewModel.diceButtonClick(index)) {
                    diceButton.setTextColor(resources.getColor(R.color.dice_unlock, null))
                } else {
                    diceButton.setTextColor(resources.getColor(R.color.dice_lock, null))
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
            gameViewModel.playerStayClick()
        }

        diceSpecialLayout = view.findViewById(R.id.game_dice_special_layout)
        talkingPointsBtn = view.findViewById(R.id.game_talking_points_btn)
        talkingPointsBtn.setOnClickListener {
            turnPhaseTextView.text = getString(R.string.game_pick_die_hint)
            pendingDieAction = { index -> gameViewModel.useTalkingPoints(index) }
        }
        spinDoctorBtn = view.findViewById(R.id.game_spin_doctor_btn)
        spinDoctorBtn.setOnClickListener {
            turnPhaseTextView.text = getString(R.string.game_pick_die_hint)
            pendingDieAction = { index -> askSpinDoctorFace(index) }
        }

        shopView = view.findViewById(R.id.game_shop_card)
        shopTitleText = view.findViewById(R.id.game_shop_title)
        shopOwnedText = view.findViewById(R.id.game_shop_owned_text)
        shopCardButtons = arrayOf(
            view.findViewById(R.id.game_shop_card1_btn),
            view.findViewById(R.id.game_shop_card2_btn),
            view.findViewById(R.id.game_shop_card3_btn)
        )
        for ((slot, cardButton) in shopCardButtons.withIndex()) {
            cardButton.setOnClickListener { buyCard(slot) }
        }
        shopRefreshBtn = view.findViewById(R.id.game_shop_refresh_btn)
        shopRefreshBtn.setOnClickListener { gameViewModel.refreshShop() }
        val shopEndBtn: Button = view.findViewById(R.id.game_shop_end_btn)
        shopEndBtn.setOnClickListener { gameViewModel.signalController.sendNotify() }

        gameViewModel.shopCards.observe(this.viewLifecycleOwner) {
            renderTurnPhase()
            renderPlayers()
            renderShop()
            renderDiceSpecials()
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
            shopView.visibility = View.INVISIBLE
            pendingDieAction = null
            renderDiceSpecials()
            if (newPhase == GamePhase.DICE) {
                diceRerollBtn.isEnabled = isPlayerTurn
                diceConfirmBtn.isEnabled = isPlayerTurn

                diceButtons.forEach { btn ->
                    btn.setTextColor(
                        resources.getColor(
                            R.color.dice_unlock,
                            null
                        )
                    )
                }
                diceView.visibility = View.VISIBLE
            } else if (newPhase == GamePhase.ASK_LEAVE) {
                stayLeaveView.visibility = View.VISIBLE
            } else if (newPhase == GamePhase.SHOP && isPlayerTurn) {
                renderShop()
                shopView.visibility = View.VISIBLE
            }
        }
    }

    fun buyCard(slot: Int) {
        val card = gameViewModel.shopCards.value?.getOrNull(slot) ?: return
        if (!card.needsTarget) {
            gameViewModel.buyCard(slot)
            return
        }
        val targets = gameViewModel.engine.opponents()
        val labels = targets.map { target ->
            getString(R.string.game_target_item, gameViewModel.players.indexOf(target) + 1, target.vote)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.game_shop_target_title)
            .setItems(labels.toTypedArray()) { _, which ->
                gameViewModel.buyCard(slot, gameViewModel.players.indexOf(targets[which]))
            }
            .show()
    }

    fun askSpinDoctorFace(index: Int) {
        val faces = DiceFace.entries
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.game_spin_face_title)
            .setItems(faces.map { it.name }.toTypedArray()) { _, which ->
                gameViewModel.useSpinDoctor(index, faces[which])
            }
            .show()
    }

    fun renderShop() {
        val engine = gameViewModel.engine
        val player = engine.current
        shopTitleText.text = getString(R.string.game_shop_title, player.money)
        for ((slot, cardButton) in shopCardButtons.withIndex()) {
            val card = engine.shop.visible[slot]
            if (card == null) {
                cardButton.visibility = View.INVISIBLE
            } else {
                cardButton.visibility = View.VISIBLE
                cardButton.text = getString(
                    R.string.game_shop_card,
                    getString(card.title),
                    card.cost,
                    getString(card.description)
                )
                cardButton.isEnabled = engine.canBuy(slot)
            }
        }
        shopRefreshBtn.isEnabled = player.money >= SHOP_REFRESH_COST
        shopOwnedText.text = getString(
            R.string.game_shop_owned,
            player.cards.joinToString(", ") { getString(it.title) }
        )
    }

    fun renderDiceSpecials() {
        val engine = gameViewModel.engine
        val isDicePhase = gameViewModel.currentPhase.value == GamePhase.DICE && !engine.current.isBot
        talkingPointsBtn.visibility = if (engine.canUseTalkingPoints()) View.VISIBLE else View.GONE
        spinDoctorBtn.visibility = if (engine.canUseSpinDoctor()) View.VISIBLE else View.GONE
        diceSpecialLayout.visibility =
            if (isDicePhase && (engine.canUseTalkingPoints() || engine.canUseSpinDoctor())) View.VISIBLE else View.GONE
    }

    fun renderPlayers() {
        middleCard.setVisibility(View.INVISIBLE)
        for (playerCard in playerCards) {
            val player = playerCard.getPlayer()!!
            if (player === gameViewModel.inCity.value) {
                middleCard.changePlayer(player, playerCard.getIndex())
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
            button.text = diceFaces[index].name.slice(0..1)
        }
    }
}