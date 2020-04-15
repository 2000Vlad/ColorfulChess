package com.training.colorfulchess.game.modelvm2

import android.app.Application
import android.graphics.Point
import androidx.annotation.IntDef
import androidx.lifecycle.*
import com.training.colorfulchess.ConfigurationInputStream
import com.training.colorfulchess.LogUtil.messageViewModel
import com.training.colorfulchess.game.*
import javax.inject.Inject

class ChessViewModel2(val app: Application, val controller: GameController2) :
    AndroidViewModel(app), Observer<ActionResult> {


    private val _tableChanges: MutableLiveData<Collection<Transformation>> = MutableLiveData()
    val tableChanges: LiveData<Collection<Transformation>> = _tableChanges

    private val _pawnReplacement: MutableLiveData<ActionPawnReplacement> = MutableLiveData()
    val pawnReplacement: LiveData<ActionPawnReplacement> = _pawnReplacement

    private val _gameEnd: MutableLiveData<Pair<Int,Int>> = MutableLiveData()
    val gameEnd: LiveData<Pair<Int,Int>> = _gameEnd

    private val _turnChange: MutableLiveData<Int> = MutableLiveData()
    val turnChange: LiveData<Int> = _turnChange

    val selected: LiveData<Point> = controller.selected


    val path: LiveData<Collection<Point>> = controller.path

    //ViewModel will add one observer to GameController and will update its multiple LiveData objects from it

    fun press(position: Int) {
        messageViewModel("CALLING CONTROLLER TO PRESS ${position.toPoint()}")
        controller.performAction(Action(ACTION_PRESS, ActionPress(position.toPoint())))
    }

    fun switchTurn() {
        messageViewModel("CALLING CONTROLLER TO SWITCH TURNS")
        controller.performAction(Action(ACTION_SWITCH_TURN, null))

    }

    fun serialize(stream: com.training.colorfulchess.ConfigurationOutputStream) {
        messageViewModel("CALLING CONTROLLER TO SERIALIZE")
        controller.performAction(Action(ACTION_SERIALIZE, ActionSerialize(stream)))
    }

    fun deserialize(stream: ConfigurationInputStream, beginning: Boolean) {
        messageViewModel("CALLING CONTROLLER TO DESERIALIZE")
        controller.performAction(Action(ACTION_DESERIALIZE, ActionDeserialize(stream, beginning)))
        val transformations = stream.transformations.toList()
        _tableChanges.value = transformations
        _turnChange.value = controller.configuration.player
        //Send initial transformations
    }

    fun put(pos: Int, @Piece piece: Int, @PieceColor color: Int) {
        messageViewModel("CALLING CONTROLLER TO PUT")
        controller.performAction(Action(ACTION_PUT, ActionPut(pos.toPoint(), piece, color)))
    }




    override fun onChanged(t: ActionResult) {
        messageViewModel("DISPATCHING ${t.javaClass.simpleName} FROM CONTROLLER")
        when (t) {
            is ActionSelect -> actionSelect(t)
            is ActionTurnChanged -> actionTurnChanged(t)
            is ActionPawnReplacement -> actionPawnReplacement(t)
            is ActionMove -> actionMove(t)
            is ActionGameEnd -> actionGameEnd(t)
        }
    }

    private fun actionSelect(result: ActionSelect) {
        messageViewModel("TREATING AcrionSelect FROM CONTROLLER")
        val transformations = mutableListOf<Transformation>()
        val clean = deselect(result.clean)
        val pos: Transformation? =
            if (result.selected == null)
                null
            else select(result.selected)
        val path =
            if(result.path == null)
                null
            else select(result.path)
        transformations.addAll(clean)
        if(pos != null)
            transformations.add(pos)
        if(!path.isNullOrEmpty())
            transformations.addAll(path)
        _tableChanges.value = transformations


    }

    private fun actionTurnChanged(result: ActionTurnChanged) {
        _turnChange.value = result.player
    }

    private fun actionPawnReplacement(result: ActionPawnReplacement) {
        _pawnReplacement.value = result
    }

    private fun actionGameEnd(result: ActionGameEnd) {
        _gameEnd.value = Pair(result.end, result.player)
    }

    private fun actionMove(result: ActionMove) {
        val transformations = mutableListOf<Transformation>()
        transformations.addAll(deselect(path.value))
        transformations.addAll(move(result.from, result.to))
        _tableChanges.value = transformations
    }
}

//region Action
class Action(val actionCode: Int, val actionData: ActionData?)

abstract class ActionData

class ActionPress(val position: Point) : ActionData()
class ActionSerialize(val stream: com.training.colorfulchess.ConfigurationOutputStream) : ActionData()
class ActionDeserialize(val stream: com.training.colorfulchess.ConfigurationInputStream, val beginning: Boolean) : ActionData()
class ActionPut(val pos: Point, @Piece val piece: Int, @PieceColor val color: Int) : ActionData()

//endregion


//region Constants
const val ACTION_PRESS = 30
const val ACTION_SWITCH_TURN = 31
const val ACTION_SERIALIZE = 32
const val ACTION_DESERIALIZE = 33
const val ACTION_PUT = 34

const val AGREEMENT = 40
//endregion

//region Annotations
@IntDef(PLAYER_1, PLAYER_2, STALEMATE, AGREEMENT)
annotation class GameEnd
//endregion

//region Factory
class ChessViewModelFactory @Inject constructor(
    val app: Application,
    val controller: GameController2
) :
    ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return modelClass.getConstructor(Application::class.java, GameController2::class.java)
            .newInstance(app, controller)
    }

}

//endregion