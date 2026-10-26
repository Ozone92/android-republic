package com.epita.republic.views.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
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

        grid.layoutManager = GridLayoutManager(requireContext(), 3)

        grid.adapter = CharacterAdapter(Character.entries) { selectedCharacter ->
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
    }
}