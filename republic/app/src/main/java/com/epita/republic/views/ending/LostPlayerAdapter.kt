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

        holder.characterImage.setImageResource(player.character.defaultImageRes)
        holder.playerText.text = context.getString(R.string.game_player_card_name, player.playerIndex)
        holder.playerLostText.text = context.getString(player.endPlayerState.text)
    }

    override fun getItemCount(): Int {
        return players.size;
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val characterImage: ImageView = itemView.findViewById(R.id.ending_item_player_image)
        val playerText: TextView = itemView.findViewById(R.id.ending_item_player)
        val playerLostText: TextView = itemView.findViewById(R.id.ending_item_lost_text)
    }
}