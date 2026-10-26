package com.epita.republic.views.character

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.epita.republic.R

class CharacterAdapter(
    private val characters: List<CharacterItem>,
    private val onCharacterClick: (Int) -> Unit
) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    class CharacterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.character_image)
        val name: TextView = view.findViewById(R.id.character_name)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CharacterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character, parent, false)

        return CharacterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CharacterViewHolder,
        position: Int
    ) {
        val character = characters[position]

        holder.image.setImageResource(character.imageRes)
        holder.image.contentDescription = character.name
        holder.name.text = character.name

        holder.image.setOnClickListener {
            onCharacterClick(character.imageRes)
        }
    }

    override fun getItemCount(): Int = characters.size
}