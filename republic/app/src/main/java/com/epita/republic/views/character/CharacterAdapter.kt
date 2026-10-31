package com.epita.republic.views.character

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.epita.republic.R
import com.epita.republic.domain.Character

class CharacterAdapter(
    private val characters: List<Character>,
    private val onCharacterClick: (Character) -> Unit
) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    private var cardHeight = 0

    fun updateCardHeight(height: Int) {
        if (height == cardHeight) return
        cardHeight = height
        notifyItemRangeChanged(0, itemCount)
    }

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
        if (cardHeight > 0) {
            holder.itemView.layoutParams = holder.itemView.layoutParams.apply {
                height = cardHeight
            }
        }

        holder.image.setImageResource(character.defaultImageRes)
        holder.image.contentDescription = character.name
        holder.name.text = character.name

        holder.image.setOnClickListener {
            onCharacterClick(character)
        }
    }

    override fun getItemCount(): Int = characters.size
}