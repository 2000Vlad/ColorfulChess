package com.training.colorfulchess.game.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.training.colorfulchess.R
import com.training.colorfulchess.game.Skin

class SkinAdapter : RecyclerView.Adapter<SkinAdapter.SkinViewHolder>() {
    var skins: List<Skin> = emptyList<Skin>()
    lateinit var listener: SkinAdapter.OnSkinSelectedListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkinViewHolder {
        return SkinViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.skin_layout,
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int {
        return skins.size
    }

    override fun onBindViewHolder(holder: SkinViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listener.onSkinChanged(skins[position])
        }
        holder.bind(skins[position])
    }

    interface OnSkinSelectedListener {
        fun onSkinChanged(skin: Skin)
    }

    class SkinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val skinBackground: ImageView by lazy {
            itemView.findViewById<ImageView>(R.id.skin_background)
        }
        val skinForeground: ImageView by lazy {
            itemView.findViewById<ImageView>(R.id.skin_foreground)
        }

        fun bind(skin : Skin) {
            skinBackground.setImageBitmap(skin.blackCell)
            skinForeground.setImageBitmap(skin.whitePawn)
        }

    }
}