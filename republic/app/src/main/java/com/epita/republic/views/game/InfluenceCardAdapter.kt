package com.epita.republic.views.game

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.epita.republic.R
import com.epita.republic.repository.InfluenceCard

class InfluenceCardAdapter(
    val context: Context,
    val cards: Array<InfluenceCard>,
    val onCardClickListener: (Int) -> Unit
) : RecyclerView.Adapter<InfluenceCardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val rowView = LayoutInflater
            .from(context)
            .inflate(R.layout.influence_card_item, parent, false)
        val viewHolder = ViewHolder(rowView)
        return viewHolder
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val card = cards[position]

        holder.itemView.setOnClickListener { onCardClickListener(position) }
        holder.titleTextView.text = context.getString(card.title)
        holder.descriptionTextView.text = context.getString(card.description)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.influence_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.influence_description)
    }
}