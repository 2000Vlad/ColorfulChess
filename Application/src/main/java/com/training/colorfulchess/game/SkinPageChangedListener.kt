package com.training.colorfulchess.game

import android.content.Context
import androidx.viewpager.widget.ViewPager

class SkinPageChangedListener(var provider: Skin.SkinProvider, var context: Context) : ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        when(position) {
            DEFAULT_SKIN_ID -> provider.skin = defaultSkin(context)
            DARK_SKIN_ID -> provider.skin = darkSkin(context)
        }

    }

}