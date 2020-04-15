package com.training.colorfulchess.game.modelvm2

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.Observer
import com.training.colorfulchess.R
import com.training.colorfulchess.game.*
import com.training.colorfulchess.game.GameActivity.Companion.GAME_SAVED
import com.training.colorfulchess.game.timer.GameTimer
import com.training.colorfulchess.game.timer.RadioTimer
import java.lang.IllegalArgumentException

/**
 * This object will observe for transformations in ViewModel
 */
class TableTransformationObserver(
    val cells: List<TableCell>,
    skin: Skin


) :
    Observer<Collection<Transformation>> {
    private val properties = Array<CellProperties>(64){ CellProperties(NONE, NONE, DEFAULT, false) }
    var texture: Skin = skin
    set(value) {
        field = value
        for (t in properties.withIndex().map { Transformation(it.index, it.value) })
            render(t)
    }
    override fun onChanged(transformations: Collection<Transformation>?) {
        if (!transformations.isNullOrEmpty())
            for (t in transformations) {
                render(t)
                properties[t.position] = t.newProperties
            }
    }

    private fun getDefaultBackgroundColor(position: Int): Bitmap {
        return if ((position / 8) % 2 == 0)
            if (position % 2 == 0) texture.whiteCell
            else texture.blackCell
        else
            if (position % 2 == 0) texture.blackCell
            else texture.whiteCell

    }

    private fun getPieceDrawable(@Piece piece: Int, @PieceColor color: Int, reverse: Boolean): Bitmap? {
        val pieceDrawable = when (piece) {
            PAWN ->
                when (color) {
                    WHITE -> texture.whitePawn
                    BLACK -> texture.blackPawn
                    else -> throw ColorException()
                }
            BISHOP ->
                when (color) {
                    WHITE -> texture.whiteBishop
                    BLACK -> texture.blackBishop
                    else -> throw ColorException()
                }
            ROOK ->
                when (color) {
                    WHITE -> texture.whiteRook
                    BLACK -> texture.blackRook
                    else -> throw ColorException()
                }
            KNIGHT ->
                when (color) {
                    WHITE -> texture.whiteKnight
                    BLACK -> texture.blackKnight
                    else -> throw ColorException()
                }
            QUEEN ->
                when (color) {
                    WHITE -> texture.whiteQueen
                    BLACK -> texture.blackQueen
                    else -> throw ColorException()
                }
            KING ->
                when (color) {
                    WHITE -> texture.whiteKing
                    BLACK -> texture.blackKing
                    else -> throw ColorException()
                }
            NONE -> return null

            else -> throw PieceException()
        }
        return if (reverse)
            reverseDrawable(texture.context, pieceDrawable)
        else pieceDrawable
    }

    private fun getBackgroundDrawable(background: Int, position: Int): Bitmap {
        Log.e("BACKGROUND", background.toString())

        return when (background) {
            com.training.colorfulchess.game.SELECTED -> texture.selectedCell
            ENEMY_SELECTED -> texture.enemyCell
            DEFAULT -> getDefaultBackgroundColor(position)
            else -> throw IllegalArgumentException("Background is incorrect") //TODO("Replace with custom exception")
        }
    }

    private fun render(transformation: Transformation) {

        val position = transformation.position
        val piece = transformation.newProperties.piece
        val pieceColor = transformation.newProperties.color
        val backgroundColor = transformation.newProperties.background
        val reverse = transformation.newProperties.reverse
        val cellView = cells[position]

        val pieceDrawable = getPieceDrawable(piece, pieceColor, reverse)
        val backgroundDrawable = getBackgroundDrawable(backgroundColor, position)

        cellView.pieceDrawable = pieceDrawable
        cellView.backgroundTexture = backgroundDrawable

    }
}

class TurnChangedObserver(
    val timer1: RadioTimer,
    val timer2: RadioTimer,
    val playerText1: TextView,
    val playerText2: TextView
) : Observer<Int> {
    override fun onChanged(player: Int?) {
        when (player) {
            PLAYER_1 -> {

                timer1.start()
                timer2.stop()

                playerText1.text = playerText1.context.getString(R.string.your_turn)
                playerText2.text = playerText2.context.getString(R.string.enemy_turn)
            }
            PLAYER_2 -> {
                timer2.start()
                timer1.stop()

                playerText2.text = playerText2.context.getString(R.string.your_turn)
                playerText1.text = playerText1.context.getString(R.string.enemy_turn)
            }
            else -> throw PlayerException()
        }

    }
}

class GameEndObserver(
    val playerText1: TextView,
    val playerText2: TextView,
    val preferences: SharedPreferences,
    val timer1: RadioTimer,
    val timer2: RadioTimer,
    val cells: Collection<TableCell>
) : Observer<Pair<Int, Int>> {
    override fun onChanged(end: Pair<Int, Int>?) {
        if (end != null) {
            when (end.first) {
                CHECKMATE ->
                    when (end.second) {
                        PLAYER_1 -> {
                            playerText1.text = playerText1.context.getString(R.string.enemy_wins)
                            playerText2.text = playerText2.context.getString(R.string.you_win)
                        }
                        PLAYER_2 -> {
                            playerText2.text = playerText2.context.getString(R.string.enemy_wins)
                            playerText1.text = playerText1.context.getString(R.string.you_win)
                        }
                        else -> throw PlayerException()

                    }
                STALEMATE -> {
                    playerText1.text = playerText1.context.getString(R.string.stalemate)
                    playerText2.text = playerText2.context.getString(R.string.stalemate)
                }
            }
            timer1.stop()
            timer2.stop()
            for(cell in cells)
                cell.setOnClickListener(null)
            preferences
                .edit()
                .putBoolean(GAME_SAVED, false)
                .apply()
        }
    }
}