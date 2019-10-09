package com.training.colorfulchess.game

import android.app.Application
import android.content.Context
import android.graphics.Point
import androidx.lifecycle.AndroidViewModel

class ChessViewModel(application: Application) :
    AndroidViewModel(application), GameActivity.ActionProvider {
    var skinId: Int = 0
    lateinit var provider : ActionProvider
    override fun getTransformations(position: Int): List<Transformation> {
        return provider.doAction(position).transformations
    }

    interface ActionProvider {
        fun doAction(position: Int): Action
    }
}

class Action(var actionCode: Int, var actionData: ActionData?) {
    var transformations: ArrayList<Transformation> = arrayListOf()

}

abstract class ActionData {

}

open class PieceData {
    var pieceType = NONE
    var pieceColor = NONE
}

open class ActionMovedData : ActionData() {
    var from = Point()
    var to = Point()
    var piece = PieceData()
}

class ActionSelectedData : ActionData() {
    var selectedPosition = Point()
    var piece = PieceData()
}

class ActionDeselectedData : ActionData() {
    var deselectedPiece = PieceData()
    var deselectedPosition = Point()
}

class ActionEnemySelectedData : ActionData() {
    var selectedPosition = Point()
    var enemyPiece = PieceData()
}

class ActionEnemyDeselectedData : ActionData() {
    var enemyPiece = PieceData()
    var deselectedPosition = Point()
}

val ACTION_MOVED = 1
val ACTION_TAKEN = 2
val ACTION_SELECTED = 3
val ACTION_DESELECTED = 4
val ACTION_ENEMY_SELECTED = 5
val ACTION_NONE = 6
val ACTION_ENEMY_DESELECTED = 7

val noAction = Action(ACTION_NONE, null)