package com.epita.republic.views.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epita.republic.R
import com.epita.republic.domain.Character

class CharacterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_character,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val grid = view.findViewById<RecyclerView>(R.id.character_grid)

        val layoutManager = GridLayoutManager(requireContext(), 5)
        grid.layoutManager = layoutManager

        val adapter = CharacterAdapter(Character.entries) { selectedCharacter ->
            val playersCharacter = ArrayList<Character>()
            playersCharacter.add(selectedCharacter)
            for (i in 0..2) {
                var iaCharacter = Character.entries.random()
                while (playersCharacter.contains(iaCharacter)) {
                    iaCharacter = Character.entries.random()
                }
                playersCharacter.add(iaCharacter)
            }

            val action =
                CharacterFragmentDirections.actionCharacterFragmentToGameFragment(
                    playersCharacter[0],
                    playersCharacter[1],
                    playersCharacter[2],
                    playersCharacter[3]
                )
            findNavController().navigate(action)
        }

        fun resizeCards() {
            val columns = if (grid.width > grid.height) 5 else 3
            layoutManager.spanCount = columns
            val rows = (Character.entries.size + columns - 1) / columns
            val availableHeight = grid.height - grid.paddingTop - grid.paddingBottom
            // Keep cards usable in unusually short windows; RecyclerView can scroll.
            val minimumHeight = (96 * resources.displayMetrics.density).toInt()
            adapter.updateCardHeight(maxOf(minimumHeight, availableHeight / rows))
        }

        grid.doOnLayout {
            grid.post {
                resizeCards()
                grid.adapter = adapter
            }
        }
        grid.addOnLayoutChangeListener { _, left, top, right, bottom,
                                        oldLeft, oldTop, oldRight, oldBottom ->
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                grid.post { resizeCards() }
            }
        }
    }
}