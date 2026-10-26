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

        val characters = listOf(
            CharacterItem("Melanchon", "Jean Luc Melanchon", R.drawable.melanchon_1),
            CharacterItem("Obono", "Danièle Obono", R.drawable.obono_1),
            CharacterItem("Macron", "Emmanuel Macron", R.drawable.macron_1),
            CharacterItem("Pecresse", "Valérie Pécresse", R.drawable.pecresse_1),
            CharacterItem("Hollande", "Jean François Hollande", R.drawable.hollande_1),
            CharacterItem("Lepen", "Jean-Mari Lepen", R.drawable.lepen_1),
            CharacterItem("Durif", "Sylvain Durif", R.drawable.durif_1),
            CharacterItem("Lassalle", "Jean Lassalle", R.drawable.lassalle_1),
            CharacterItem("Royal", "Segolene Royal", R.drawable.royal_1)
        )

        val grid = view.findViewById<RecyclerView>(R.id.character_grid)

        grid.layoutManager = GridLayoutManager(requireContext(), 3)

        grid.adapter = CharacterAdapter(characters) { selectedCharacterId ->
            val playersCharacter = ArrayList<Int>()
            playersCharacter.add(selectedCharacterId)
            for (i in 0..2) {
                var iaCharacter = characters.random()
                while (playersCharacter.contains(iaCharacter.imageRes)) {
                    iaCharacter = characters.random()
                }
                playersCharacter.add(iaCharacter.imageRes)
            }


            val action =
                CharacterFragmentDirections.actionCharacterFragmentToGameFragment(
                    playersCharacter.toIntArray()
                )
            findNavController().navigate(action)
        }
    }
}