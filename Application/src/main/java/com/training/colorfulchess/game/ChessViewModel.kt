package com.training.colorfulchess.game

import android.app.Application
import android.content.Context
import android.graphics.Point
import androidx.lifecycle.AndroidViewModel

class ChessViewModel(application: Application) :
    AndroidViewModel(application), GameActivity.ActionProvider, CellDragListener.DragStateProvider {
    var skinId: Int = 0
    lateinit var provider: ActionProvider
    lateinit var selectedPathProvider: PathProvider
    var previousAction = noAction
    override fun getTransformations(position: Int): List<Transformation> {
        val action = provider.doAction(position)
        previousAction = action
        return action.transformations
    }

    override fun getActionData(): ActionData? = previousAction.actionData

    fun switchTurns(): List<Transformation> {
        return provider.doAction(-1).transformations
    }

    override fun getDragState(pos: Int): Int {
        val path = selectedPathProvider.selectedPath
        return if (
            pos in path.map { it.toIndex() }
        )
            ACCEPT_DRAG
        else REJECT_DRAG


    }

    interface ActionProvider {
        fun doAction(position: Int): Action
    }

    interface PathProvider {
        val selectedPath: Collection<Point>
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

const val ACTION_MOVED = 1
const val ACTION_SELECTED = 2
const val ACTION_DESELECTED = 3
const val ACTION_ENEMY_SELECTED = 4
const val ACTION_NONE = 5
const val ACTION_ENEMY_DESELECTED = 6
const val ACTION_TURN_CHANGED = 7

val noAction = Action(ACTION_NONE, null)