package com.training.colorfulchess.game.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.training.colorfulchess.R
import com.training.colorfulchess.game.Skin

class SkinFragment(var skin : Skin) : Fragment() {

    private lateinit var backgroundImageView : ImageView
    private lateinit var foregroundImageView : ImageView
    private lateinit var skinCard : CardView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.skin_layout,container, false)
        backgroundImageView = view.findViewById(R.id.skin_background)
        foregroundImageView = view.findViewById(R.id.skin_foreground)
        skinCard = view.findViewById(R.id.skin_card)
        backgroundImageView.background = skin.blackCell
        foregroundImageView.background = skin.whitePawn
        skinCard.viewTreeObserver.addOnPreDrawListener (object : ViewTreeObserver.OnPreDrawListener{
            override fun onPreDraw(): Boolean {
                skinCard.radius = (skinCard.measuredWidth / 2).toFloat()
                return true
            }
        })
        return view
    }


}
