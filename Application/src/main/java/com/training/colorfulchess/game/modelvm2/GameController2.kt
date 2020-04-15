package com.training.colorfulchess.game.modelvm2

import android.graphics.Point
import androidx.annotation.IntDef
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.training.colorfulchess.ConfigurationInputStream
import com.training.colorfulchess.ConfigurationOutputStream
import com.training.colorfulchess.LogUtil.*
import com.training.colorfulchess.game.*
import javax.inject.Inject

class GameController2 @Inject constructor(val configuration: GameConfiguration2) {

    private val _actionResult: MutableLiveData<ActionResult> = MutableLiveData()
    /**
     * This property encapsulates every result that an action can produce
     */
    val actionResult: LiveData<ActionResult> = _actionResult

    private val _path: MutableLiveData<Collection<Point>> = MutableLiveData(emptyList())
    /**
     * This is the property which holds the currently selected piece's path, null if there is none
     * It's updated alongside with [selected]
     */
    val path: LiveData<Collection<Point>> = _path

    private val _selected: MutableLiveData<Point> = MutableLiveData()
    /**
     * This property holds the currently selected piece, null if there is none
     * It's updated every time a new piece is selected
     * Don't use this property to listen for selection events, use [actionResult] instead
     */
    val selected: LiveData<Point> = _selected

    @ControllerState
    var state: Int = IDLE


    /**
     * this property receives changes that happen in [configuration] caused by [performAction]
     */


    fun performAction(action: Action) {
        when (action.actionCode) {
            ACTION_PRESS -> press((action.actionData!! as ActionPress).position)
            ACTION_PUT -> {
                val actionData = action.actionData as ActionPut
                put(actionData.pos, actionData.piece, actionData.color)
            }
            ACTION_SERIALIZE -> {
                val actionData = action.actionData as ActionSerialize
                serialize(actionData.stream)
            }
            ACTION_DESERIALIZE -> {
                val actionData = action.actionData as ActionDeserialize
                deserialize(actionData.stream, actionData.beginning)
            }
            ACTION_SWITCH_TURN -> {
                switchTurn()
            }
        }

    }

    private fun serialize(stream: ConfigurationOutputStream) {
        configuration.serialize(stream)
    }

    private fun deserialize(stream: ConfigurationInputStream, beginning: Boolean) {
        configuration.deserialize(stream, beginning)
    }

    private fun press(pos: Point) {
        when (state) {
            IDLE -> {
                messageController("THE USER PRESSED A CELL AND THERE IS NOTHING GOING ON")
                if (isFriendPiece(pos)) {
                    state = SELECTED
                    _path.value = configuration.getAvailablePositions(pos).toList()
                    _selected.value = pos
                    val result = ActionSelect(pos, path.value)
                    _actionResult.value = result

                } else
                    if (isEnemyPiece(pos)) {
                        state = SELECTED
                        _path.value = emptyList()
                        _selected.value = pos
                        val result = ActionSelect(pos, path.value)
                        _actionResult.value = result

                    }
            }
            SELECTED -> {
                messageViewModel("THE USER PRESSED A CELL AND THERE IS ANOTHER ONE SELECTED")
                if (_path.value!!.contains(pos)) {
                    messageController("THE USER PRESSED THE SELECTED CELL")
                    state = IDLE
                    configuration.move(_selected.value!!, pos)
                    val moveResult = ActionMove(_selected.value!!, pos)
                    _actionResult.value = moveResult
                    _path.value = emptyList()
                    _selected.value = null
                    _actionResult.value = ActionTurnChanged(configuration.player)
                    val gState = configuration.gameState
                    if (gState == CHECKMATE || gState == STALEMATE) {
                        val gameResult = ActionGameEnd(gState, configuration.player)
                        _actionResult.value = gameResult
                    }
                    val replacement = pawnReplacement()
                    if (replacement.first != null)  //If one is non-null then all are non-null
                        _actionResult.value =
                            ActionPawnReplacement(replacement.first!!, replacement.second!!)


                } else {
                    when {
                        pos == _selected.value -> {
                            messageController("THE USER IS DESELECTING THE PIECE")
                            state = IDLE
                            val clean = clean(pos, _path.value)
                            val result = ActionSelect(null, null, clean)
                            _path.value = emptyList()
                            _selected.value = null
                            _actionResult.value = result
                        }

                        isFriendPiece(pos) -> {
                            //State will stay selected
                            messageController("THE USER PRESSED ON A FRIENDLY PIECE WHILE ANOTHER PIECE IS SELECTED")
                            val clean = clean(_selected.value, _path.value)
                            _path.value = configuration.getAvailablePositions(pos).toList()
                            _selected.value = pos
                            val result = ActionSelect(pos, _path.value, clean)
                            _actionResult.value = result

                        }
                        isEnemyPiece(pos) -> {
                            //State will stay selected
                            messageController("THE USER PRESSED ON AN ENEMY PIECE WHILE ANOTHER PIECE IS SELECTED")
                            val clean = clean(_selected.value, _path.value)
                            _path.value = emptyList()
                            _selected.value = pos
                            val result = ActionSelect(pos, null, clean)
                            _actionResult.value = result
                        }
                        else -> {
                            //There is a selected piece and the user pressed on empty space
                            messageController("THE USER PRESSED ON AN EMPTY SPACE WHILE THERE IS A SELECTED PIECE")
                            state = IDLE
                            val clean = clean(_selected.value, _path.value)
                            val result = ActionSelect(null, null, clean)
                            _selected.value = null
                            _path.value = emptyList()
                            _actionResult.value = result
                        }
                    }
                }
            }

        }
    }

    private fun put(pos: Point, @Piece piece: Int, @PieceColor color: Int) {
        configuration.put(pos, piece, color)
    }

    private fun switchTurn() {

        val isInCheck = when (configuration.player) {
            PLAYER_1 -> {
                configuration.whiteKingChecks.isNotEmpty()
            }
            PLAYER_2 -> {
                configuration.blackKingChecks.isNotEmpty()
            }
            else -> throw PlayerException()
        }


        val state = configuration.gameState

        val stalemate = state == STALEMATE


        configuration.switchTurn()
        if (_selected.value != null) {
            this.press(_selected.value!!)
        }
        _actionResult.value = ActionTurnChanged(configuration.player)


        if(isInCheck || stalemate) {
            val gameEnd = if(isInCheck) CHECKMATE else STALEMATE
            configuration.switchTurn()
            _actionResult.value = ActionGameEnd(gameEnd, configuration.player)
        }
    }

    private fun checkEndGame(): Boolean {
        return false
    }


}

//region ActionResult
abstract class ActionResult

class ActionPawnReplacement(val pos: Point, @PieceColor color: Int) : ActionResult()
class ActionGameEnd(@GameEnd val end: Int, @Player val player: Int) : ActionResult()
class ActionSelect(
    val selected: Point?,
    val path: Collection<Point>?,
    val clean: Collection<Point>? = null
) : ActionResult()

class ActionMove(val from: Point, val to: Point) : ActionResult()
class ActionTurnChanged(val player: Int) : ActionResult()

//endregion

//region Constants
const val IDLE = 20
const val SELECTED = 21


@IntDef(IDLE, SELECTED)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION
)
annotation class ControllerState
//endregion

//region Helpers
/**
 * Retrives the current pawn that needs to be replaced, if there is none all values will be null
 * @return (position, piece, color) where 'position' is the 1-based coordinates on the table,
 * 'piece' is the piece type and 'color' is the color of the piece
 */
//Bug
fun GameController2.pawnReplacement(): Pair<Point?, Int?> {
    for ((pos, piece) in configuration.whitePieces) {
        if (piece == PAWN)
            if (pos.y == 1)
                return Pair(pos, WHITE)

    }
    for ((pos, piece) in configuration.blackPieces) {
        if (piece == PAWN)
            if (pos.y == 8)
                return Pair(pos, BLACK)

    }
    return Pair(null, null)
}

//endregion