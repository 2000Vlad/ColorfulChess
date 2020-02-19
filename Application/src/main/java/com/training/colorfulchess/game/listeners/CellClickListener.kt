package com.training.colorfulchess.game.listeners

import android.view.View
import com.training.colorfulchess.game.modelvm2.ChessViewModel2

class CellClickListener(val viewModel : ChessViewModel2, val position: Int) : View.OnClickListener {
    override fun onClick(v: View?) {
        viewModel.press(position)
    }
}