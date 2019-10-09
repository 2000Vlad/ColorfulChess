package com.training.colorfulchess.game

import android.graphics.Point
import java.io.*
import java.lang.IllegalArgumentException

class GameController(var configuration: GameConfiguration) : ChessViewModel.ActionProvider, GameActivity.GameSerializer {
    var currentAction = noAction
    var player = PLAYER_1
    var currentPiecePath: MutableList<Point> =
        mutableListOf() //In order to implement DRY principle, updated in performSelectAction
    var state = NONE
    lateinit var listener : GameStateListener

    override fun doAction(position: Int): Action {
        if (state != NONE) return noAction
        val actionType = getActionType(position)
        val pos = position.toPoint()
        when (actionType) {
            ACTION_SELECTED -> {
                currentAction = performSelectAction(pos)
                return currentAction
            }
            ACTION_ENEMY_SELECTED -> {
                currentAction = performEnemySelectAction(pos)
                return currentAction
            }
            ACTION_DESELECTED -> {
                val action = performDeselectAction(pos)
                currentAction = noAction
                return action
            }
            ACTION_ENEMY_DESELECTED -> {
                val action = performEnemyDeselectAction(pos)
                currentAction = noAction
                return action
            }
            ACTION_MOVED -> {
                val from = (currentAction.actionData as ActionSelectedData).selectedPosition
                val action = performMoveAction(from, pos)
                configuration.prepareForNextTurn()


                currentAction = noAction
                switchPlayers()
                listener.onTurnChanged(player)
                state = configuration.gameState
                if(state != NONE)
                    listener.onGameStateChanged(state)
                return action
            }
            ACTION_NONE -> {
                currentAction = noAction
                return noAction
            }
        }
        throw IllegalArgumentException("Action incorrect")
    }


    fun getActionType(position: Int): Int {

        if (isSelectedAction(position.toPoint())) return ACTION_SELECTED
        if (isEnemySelectedAction(position.toPoint())) return ACTION_ENEMY_SELECTED
        if (isDeselectedAction(position.toPoint())) return ACTION_DESELECTED
        if (isEnemyDeselectedAction(position.toPoint())) return ACTION_ENEMY_DESELECTED
        if (isMovedAction(position.toPoint())) return ACTION_MOVED
        return ACTION_NONE
    }

    fun performSelectAction(position: Point): Action {
        val actionData = ActionSelectedData()
        val action = Action(ACTION_SELECTED, null)
        if (currentAction.actionCode == ACTION_SELECTED) {
            val data = currentAction.actionData as ActionSelectedData
            action.transformations.addAll(
                getDeselectingTransformations(data.selectedPosition, currentPiecePath)
            )
        }
        if (currentAction.actionCode == ACTION_ENEMY_SELECTED) {
            val data = currentAction.actionData as ActionEnemySelectedData
            action.transformations.addAll(
                getDeselectingTransformations(data.selectedPosition)
            )
        }
        val path = configuration.getAvailablePositions(position).toList()
        currentPiecePath = path.toMutableList()
        action.transformations.addAll(
            getSelectingTransformations(position, currentPiecePath, false)
        )
        actionData.selectedPosition = position
        actionData.piece.pieceColor = configuration.getColor(position)
        actionData.piece.pieceType = configuration.getPiece(position)
        action.actionData = actionData

        return action

    }

    fun performEnemySelectAction(position: Point): Action {
        val action = Action(ACTION_ENEMY_SELECTED, null)
        val transformations = arrayListOf<Transformation>()
        val actionCode = currentAction.actionCode
        if (actionCode == ACTION_SELECTED) {
            val actionData = currentAction.actionData as ActionSelectedData
            transformations.addAll(
                getDeselectingTransformations(
                    actionData.selectedPosition,
                    currentPiecePath
                )//deselecteaza piesa selectata, nu cea curenta, foloseste actionData
            )
            currentPiecePath = emptyList<Point>().toMutableList()
        }
        if (actionCode == ACTION_ENEMY_SELECTED) {
            val actionData = currentAction.actionData as ActionEnemySelectedData
            transformations.addAll(
                getDeselectingTransformations(actionData.selectedPosition) //deselecteaza piesa selectata, nu cea curenta, foloseste actionData
            )
        }
        transformations.addAll(
            getSelectingTransformations(position = position, enemy = true)
        )
        action.actionData = ActionEnemySelectedData().apply {
            selectedPosition = position
            enemyPiece.pieceColor = configuration.getColor(position)
            enemyPiece.pieceType = configuration.getPiece(position)
        }
        action.transformations = transformations
        return action

    }

    fun performDeselectAction(position: Point): Action {
        val action = Action(ACTION_NONE, null)
        val data = currentAction.actionData as ActionSelectedData
        action.transformations.addAll(
            getDeselectingTransformations(data.selectedPosition, currentPiecePath)
        )
        currentPiecePath = emptyList<Point>().toMutableList()
        action.actionData = ActionDeselectedData().apply {
            deselectedPiece.pieceType = configuration.getPiece(position)
            deselectedPiece.pieceColor = configuration.getColor(position)
            deselectedPosition = position
        }

        return action
    }

    fun performEnemyDeselectAction(position: Point): Action {
        val action = Action(ACTION_NONE, null)
        val data = currentAction.actionData as ActionEnemySelectedData
        action.transformations.addAll(
            getDeselectingTransformations(data.selectedPosition, emptyList())
        )
        action.actionData = ActionEnemyDeselectedData().apply {
            enemyPiece.pieceType = configuration.getPiece(position)
            enemyPiece.pieceColor = configuration.getColor(position)
            deselectedPosition = position
        }
        return action
    }

    fun performMoveAction(from: Point, to: Point): Action {
        val action = Action(ACTION_NONE, null)

        action.transformations.addAll(
            getDeselectingTransformations(from, currentPiecePath)
        )

        action.transformations.addAll(
            getMovingTransformations(from, to)
        )
        configuration.move(from, to)
        action.actionData = ActionMovedData().apply {
            this.from = from
            this.to = to
            this.piece.pieceType = configuration.getPiece(from)
            this.piece.pieceColor = configuration.getColor(to)
        }


        //TODO("Update configuration")
        return action
    }

    fun isSelectedAction(position: Point): Boolean {
        val actionCode = currentAction.actionCode
        if (actionCode == ACTION_NONE)
            if (configuration.isFriendPiece(position))
                return true
        if (actionCode == ACTION_SELECTED) {
            val data = currentAction.actionData as ActionSelectedData
            if (data.selectedPosition != position)
                if (configuration.isFriendPiece(position))
                    return true
        }
        if (actionCode == ACTION_ENEMY_SELECTED)
            if (configuration.isFriendPiece(position))
                return true
        return false
    }

    fun isDeselectedAction(position: Point): Boolean {
        val actionCode = currentAction.actionCode
        if (actionCode != ACTION_SELECTED)
            return false
        if (configuration.isEmpty(position))
            if (position !in currentPiecePath)
                return true
        val data = currentAction.actionData as ActionSelectedData
        if (position == data.selectedPosition)
            return true
        return false
    }

    fun isEnemySelectedAction(position: Point): Boolean {
        if (configuration.isEnemyPiece(position)) {
            if (currentAction.actionCode == ACTION_ENEMY_SELECTED) {

                val data = currentAction.actionData as ActionEnemySelectedData
                return data.selectedPosition != position
            } else
                if(currentAction.actionCode == ACTION_SELECTED)
                    return position !in currentPiecePath
                else return true
        }
        else return false
        // return configuration.isEnemyPiece(position)
    }

    fun isEnemyDeselectedAction(position: Point): Boolean {
        val actionCode = currentAction.actionCode
        if (actionCode != ACTION_ENEMY_SELECTED) return false
        val data = currentAction.actionData as ActionEnemySelectedData
        return configuration.isEmpty(position) || data.selectedPosition == position

    }

    fun isMovedAction(position: Point): Boolean {
        val actionCode = currentAction.actionCode
        if (actionCode != ACTION_SELECTED)
            return false
        if (position in currentPiecePath) {
            return true
        }
        return false

    }

    fun getSelectingTransformations(
        position: Point,
        path: List<Point> = emptyList(),
        enemy: Boolean = false
    ): List<Transformation> {
        val result = mutableListOf<Transformation>()
        result.add(
            Transformation(
                position.toIndex(),
                CellProperties(
                    piece = configuration.getPiece(position),
                    background = if (enemy) ENEMY_SELECTED else SELECTED,
                    color = configuration.getColor(position),
                    reverse = configuration.getColor(position) == BLACK
                )
            )
        )
        for (pos in path) {
            result.add(
                Transformation(
                    pos.toIndex(),
                    CellProperties(
                        piece = configuration.getPiece(pos),
                        background = if (enemy) ENEMY_SELECTED else SELECTED,
                        color = configuration.getColor(pos),
                        reverse = configuration.getColor(pos) == BLACK
                    )
                )
            )
        }
        return result
    }

    fun getDeselectingTransformations(
        position: Point,
        path: List<Point> = emptyList()
    ): List<Transformation> {
        val result = mutableListOf<Transformation>()
        for (pos in path) {
            result.add(
                Transformation(
                    pos.toIndex(),
                    CellProperties(
                        configuration.getPiece(pos),
                        configuration.getColor(pos),
                        DEFAULT,
                        configuration.getColor(pos) == BLACK
                    )
                )
            )
        }
        result.add(
            Transformation(
                position.toIndex(),
                CellProperties(
                    configuration.getPiece(position),
                    configuration.getColor(position),
                    DEFAULT,
                    configuration.getColor(position) == BLACK
                )
            )
        )
        return result
    }

    fun getMovingTransformations(from: Point, to: Point): List<Transformation> {
        val result = mutableListOf<Transformation>()
        result.add(
            Transformation(
                from.toIndex(),
                CellProperties(
                    piece = NONE,
                    background = DEFAULT,
                    reverse = false,
                    color = NONE
                )
            )
        )
        result.add(
            Transformation(
                to.toIndex(),
                CellProperties(
                    piece = configuration.getPiece(from),
                    background = DEFAULT,
                    color = configuration.getColor(from),
                    reverse = configuration.getColor(from) == BLACK
                )
            )
        )
        return result
    }

    fun switchPlayers() {
        if (player == PLAYER_1) {
            player = PLAYER_2
        } else
            if (player == PLAYER_2)
                player = PLAYER_1
    }

    fun GameConfiguration.isFriendPiece(position: Point): Boolean {
        if (player == PLAYER_1) {
            val color = getColor(position)
            return color == WHITE
        }
        if (player == PLAYER_2) {
            val color = getColor(position)
            return color == BLACK
        }
        throw IllegalArgumentException("Player not set correct")
    }

    fun GameConfiguration.isEnemyPiece(position: Point): Boolean {
        if (player == PLAYER_1)
            return getColor(position) == BLACK
        if (player == PLAYER_2)
            return getColor(position) == WHITE
        throw IllegalArgumentException("Player not set correct")
    }

    fun deserialize(stream : FileInputStream) {
        for(i in 0..63) {
            val piece = stream.read()
            val color = stream.read()
            configuration.table[i] = GameCell(
                piece = piece,
                color =  color
            )
            if(piece == KING) {
                if (color == WHITE) configuration.whiteKingPosition = i.toPoint()
                if (color == BLACK) configuration.blackKingPosition = i.toPoint()
            }
        }
        player = stream.read()
        configuration.prepareForNextTurn()
        stream.close()
    }
    override fun serialize(stream : FileOutputStream) {
       for(cell in configuration.table.withIndex()) {
           stream.write(cell.value!!.piece)
           stream.write(cell.value!!.color)

       }
        stream.write(player)
        stream.close()

    }

}
interface GameStateListener {
    fun onGameStateChanged(state : Int)
    fun onTurnChanged(player : Int)
}



