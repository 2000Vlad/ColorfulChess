package com.training.colorfulchess.game.di.dagger

import com.training.colorfulchess.game.modelvm2.ChessViewModel2
import com.training.colorfulchess.game.modelvm2.ChessViewModelFactory
import com.training.colorfulchess.game.modelvm2.GameConfiguration2
import com.training.colorfulchess.game.modelvm2.GameController2
import dagger.Component

@Component(modules = [ChessModule::class])
interface ChessComponent {
    val gameConfiguration : GameConfiguration2
    val gameController : GameController2
    val viewModelFactory : ChessViewModelFactory
    val viewModel : ChessViewModel2
}