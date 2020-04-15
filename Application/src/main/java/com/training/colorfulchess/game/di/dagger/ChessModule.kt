package com.training.colorfulchess.game.di.dagger

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.training.colorfulchess.game.modelvm2.ChessViewModel2
import com.training.colorfulchess.game.modelvm2.ChessViewModelFactory
import com.training.colorfulchess.game.modelvm2.GameController2
import dagger.Module
import dagger.Provides

@Module
open class ChessModule {
    var app: Application
    var activity: FragmentActivity

    constructor(application: Application, factivity: FragmentActivity) {
        app = application
        activity = factivity
    }



    @Provides
    fun provideViewModelFactory(controller: GameController2) : ChessViewModelFactory {
        return ChessViewModelFactory(
            app,
            controller
        )
    }

    @Provides
    fun provideChessViewModel(factory: ChessViewModelFactory ) : ChessViewModel2 {
        return ViewModelProviders.of(activity, factory).get(ChessViewModel2::class.java)
            .apply {
                factory.controller.actionResult.observe(activity, this)
            }
    }
}