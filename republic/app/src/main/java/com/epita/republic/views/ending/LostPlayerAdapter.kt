package com.epita.republic.views.ending

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.epita.republic.R
import com.epita.republic.repository.EndingPlayerInfo
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class LostPlayerAdapter(val context: Context, val players: Array<EndingPlayerInfo>): RecyclerView.Adapter<LostPlayerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val rowView = LayoutInflater
            .from(context)
            .inflate(R.layout.ending_player_item, parent, false)
        val viewHolder = ViewHolder(rowView)
        return viewHolder
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val player = players[position]

        if (player.isBot) {
            holder.card.strokeColor = MaterialColors.getColor(holder.card, R.attr.colorPlayerBot)
        } else {
            holder.card.strokeColor = MaterialColors.getColor(holder.card, R.attr.colorPlayerHuman)
        }
        holder.characterImage.setImageResource(player.character.defaultImageRes)
        holder.playerText.text = context.getString(R.string.game_player_card_name, player.playerIndex + 1)
        holder.playerLostText.text = context.getString(player.endPlayerState.text)
    }

    override fun getItemCount(): Int {
        return players.size;
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.ending_item_card)
        val characterImage: ImageView = itemView.findViewById(R.id.ending_item_player_image)
        val playerText: TextView = itemView.findViewById(R.id.ending_item_player)
        val playerLostText: TextView = itemView.findViewById(R.id.ending_item_lost_text)
    }
}