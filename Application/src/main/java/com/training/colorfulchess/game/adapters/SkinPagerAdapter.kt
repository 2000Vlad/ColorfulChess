package com.training.colorfulchess.game.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.training.colorfulchess.game.Skin
import com.training.colorfulchess.game.fragments.SkinFragment
import java.io.File


class SkinPagerAdapter(manager: FragmentManager,vararg skins : Skin) : FragmentPagerAdapter(
    manager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    val skins = skins
    override fun getItem(position: Int): Fragment {
        return SkinFragment(skins[position])
    }

    override fun getCount(): Int {
        return 2
    }


}