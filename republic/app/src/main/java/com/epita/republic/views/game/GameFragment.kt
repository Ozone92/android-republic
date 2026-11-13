package com.epita.republic.views.game

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.epita.republic.audio.GameSoundPlayer
import kotlinx.coroutines.launch
import android.content.res.ColorStateList
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.epita.republic.views.help.HowToPlayDialogFragment

class GameFragment : Fragment() {
    val gameViewModel: GameViewModel by viewModels()
    private val args: GameFragmentArgs by navArgs()
    private val characters by lazy {
        arrayOf(args.player1, args.player2, args.player3, args.player4)
    }

    lateinit var playerCards: Array<PlayerCard>
    lateinit var middleCard: PlayerCard
    lateinit var turnPhaseTextView: TextView
    private var shownDiceFaces: Array<DiceFace>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onResume() {
        super.onResume()
        setSystemBarsHidden(true)
    }

    override fun onPause() {
        setSystemBarsHidden(false)
        super.onPause()
    }

    private fun setSystemBarsHidden(hidden: Boolean) {
        val window = requireActivity().window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (hidden) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val soundPlayer = GameSoundPlayer(requireContext(), viewLifecycleOwner)
        viewLifecycleOwner.lifecycleScope.launch {
            gameViewModel.sounds.collect { soundPlayer.play(it) }
        }
        view.findViewById<Button>(R.id.game_power_shop_btn).setOnClickListener {
            if (childFragmentManager.findFragmentByTag(PowerShopDialogFragment.TAG) == null) {
                PowerShopDialogFragment().show(childFragmentManager, PowerShopDialogFragment.TAG)
            }
        }


        turnPhaseTextView = view.findViewById(R.id.game_phase_text)

        view.findViewById<View>(R.id.game_help_btn).setOnClickListener {
            HowToPlayDialogFragment.show(childFragmentManager)
        }
        HowToPlayDialogFragment.showOnce(requireContext(), childFragmentManager)

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
                awayTextView = view.findViewById(R.id.game_player1_away_text),
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
                awayTextView = view.findViewById(R.id.game_player2_away_text),
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
                awayTextView = view.findViewById(R.id.game_player3_away_text),
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
                awayTextView = view.findViewById(R.id.game_player4_away_text),
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
        (playerCards.toList() + middleCard).forEach { card ->
            card.setOnPortraitClick { clickedCard ->
                val character = clickedCard.getCharacter() ?: return@setOnPortraitClick
                if (childFragmentManager.findFragmentByTag(PlayerDetailsDialogFragment.TAG) == null) {
                    PlayerDetailsDialogFragment.newInstance(clickedCard.getIndex(), character)
                        .show(childFragmentManager, PlayerDetailsDialogFragment.TAG)
                }
            }
        }
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
                renderDiceHint()
            }
        }

        val diceRerollBtn: Button = view.findViewById(R.id.game_dice_reroll)
        diceRerollBtn.setOnClickListener { gameViewModel.rollDice() }
        gameViewModel.rerollLeft.observe(this.viewLifecycleOwner) { rerollLeft ->
            diceRerollBtn.text = getString(R.string.game_reroll_text, rerollLeft)
            renderDiceHint()
        }

        val diceConfirmBtn: Button = view.findViewById(R.id.game_dice_confirm)
        diceConfirmBtn.setOnClickListener { gameViewModel.signalController.sendNotify() }

        gameViewModel.currentPlayerTurnIndex.observe(viewLifecycleOwner) {
            renderTurnPhase()
            renderPlayers()
        }
        gameViewModel.inCity.observe(this.viewLifecycleOwner) { renderPlayers() }
        gameViewModel.dices.observe(this.viewLifecycleOwner) { newDices ->
            renderDice(diceButtons, newDices)
            renderDiceHint()
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
            renderDiceHint()
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
        view?.findViewById<Button>(R.id.game_power_shop_btn)?.text =
            getString(R.string.power_shop_open, gameViewModel.players[0].money)
        middleCard.setVisibility(View.INVISIBLE)
        val elyseeLabel = view?.findViewById<View>(R.id.game_elysee_label)
        elyseeLabel?.visibility = View.INVISIBLE
        val activePlayerIndex = gameViewModel.currentPlayerTurnIndex.value
        for (playerCard in playerCards) {
            val player = playerCard.getPlayer()!!
            val isActive = playerCard.getIndex() == activePlayerIndex
            if (player == gameViewModel.inCity.value) {
                middleCard.changePlayer(player, playerCard.getIndex(), playerCard.getCharacter(), isActive)
                middleCard.setVisibility(View.VISIBLE)
                elyseeLabel?.visibility = View.VISIBLE
                playerCard.setAway(true)
            } else {
                playerCard.setAway(false)
                playerCard.render(isActive)
            }
        }
    }

    fun renderTurnPhase() {
        val playerIndex = gameViewModel.currentPlayerTurnIndex.value!!
        val name = getString(characters[playerIndex].displayName)
        val isHumanTurn = playerIndex == 0
        turnPhaseTextView.text = when (gameViewModel.currentPhase.value!!) {
            GamePhase.DICE ->
                if (isHumanTurn) getString(R.string.turn_dice_you) else getString(R.string.turn_dice_bot, name)
            GamePhase.IMPROVE_DICE ->
                if (isHumanTurn) getString(R.string.turn_improve_you) else getString(R.string.turn_improve_bot, name)
            GamePhase.INFLUENCE ->
                if (isHumanTurn) getString(R.string.turn_influence_you) else getString(R.string.turn_influence_bot, name)
            GamePhase.SHOP ->
                if (isHumanTurn) getString(R.string.turn_shop_you) else getString(R.string.turn_shop_bot, name)
            GamePhase.ASK_LEAVE -> getString(R.string.turn_ask_leave)
            GamePhase.GAME_FINISH -> getString(R.string.turn_game_finish)
        }
    }

    private fun renderDiceHint() {
        val hint = view?.findViewById<TextView>(R.id.game_dice_hint) ?: return
        val player = gameViewModel.currentPlayer.value ?: return
        val dice = gameViewModel.dices.value ?: return
        val hasTriple = listOf(DiceFace.ONE, DiceFace.TWO, DiceFace.THREE)
            .any { face -> dice.count { it == face } >= 3 }
        hint.text = when {
            player.isBot -> getString(
                R.string.hint_bot_dice,
                getString(characters[gameViewModel.currentPlayerTurnIndex.value!!].displayName)
            )
            gameViewModel.currentPhase.value == GamePhase.IMPROVE_DICE -> getString(R.string.hint_improve)
            (gameViewModel.rerollLeft.value ?: 0) == 0 -> getString(R.string.hint_no_reroll)
            hasTriple -> getString(R.string.hint_triple)
            player == gameViewModel.inCity.value -> getString(R.string.hint_elysee)
            gameViewModel.lockedDiceIndexes.isEmpty() -> getString(R.string.hint_keep_dice)
            else -> getString(R.string.hint_reroll_or_confirm)
        }
    }

    fun renderDice(buttons: Array<Button>, diceFaces: Array<DiceFace>) {
        val previousFaces = shownDiceFaces
        val rolled = previousFaces == null || !previousFaces.contentEquals(diceFaces)
        shownDiceFaces = diceFaces.copyOf()
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
        if (rolled && previousFaces != null) {
            buttons.withIndex()
                .filter { (index, _) -> !gameViewModel.lockedDiceIndexes.contains(index) }
                .forEach { (index, button) -> animateRoll(button, index) }
        }
        renderDicesColor(buttons)
    }

    private fun animateRoll(button: Button, index: Int) {
        button.animate().cancel()
        button.rotation = 0f
        button.scaleX = DICE_ROLL_START_SCALE
        button.scaleY = DICE_ROLL_START_SCALE
        button.animate()
            .rotation(360f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(index * DICE_ROLL_STAGGER_MS)
            .setDuration(DICE_ROLL_DURATION_MS)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { button.rotation = 0f }
            .start()
    }

    private fun lockBadge(): Drawable {
        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_lock)!!
        val size = resources.getDimensionPixelSize(R.dimen.size_dice_lock)
        val inset = resources.getDimensionPixelSize(R.dimen.space_s)
        return LayerDrawable(arrayOf(icon)).apply {
            setLayerSize(0, size, size)
            setLayerGravity(0, Gravity.TOP or Gravity.END)
            setLayerInsetTop(0, inset)
            setLayerInsetEnd(0, inset / 2)
        }
    }

    fun renderDicesColor(diceButtons: Array<Button>) {
        diceButtons.withIndex().forEach { (index, button) ->
            val locked = gameViewModel.lockedDiceIndexes.contains(index)
            val color = MaterialColors.getColor(
                button,
                if (locked) R.attr.colorDiceLocked else com.google.android.material.R.attr.colorOutline
            )
            button.setTextColor(color)
            (button as MaterialButton).apply {
                strokeColor = ColorStateList.valueOf(color)
                strokeWidth = resources.getDimensionPixelSize(
                    if (locked) R.dimen.border_emphasis else R.dimen.border_dice
                )
            }
            if (locked && button.foreground == null) {
                button.foreground = lockBadge()
            } else if (!locked) {
                button.foreground = null
            }
            val lift = if (locked) -resources.getDimension(R.dimen.size_dice_lift) else 0f
            if (button.translationY != lift) {
                button.animate().translationY(lift).setDuration(DICE_LIFT_DURATION_MS).start()
            }
            ViewCompat.setStateDescription(button, getString(
                if (locked) R.string.dice_locked else R.string.dice_unlocked
            ))
        }
    }

    companion object {
        private const val DICE_ROLL_START_SCALE = 0.7f
        private const val DICE_ROLL_STAGGER_MS = 40L
        private const val DICE_ROLL_DURATION_MS = 380L
        private const val DICE_LIFT_DURATION_MS = 150L
    }
}
