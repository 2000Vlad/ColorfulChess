package com.training.colorfulchess.game

import android.app.Application
import android.graphics.Point
import java.io.DataInputStream
import java.io.InputStream

open class Configuration {
    var cells = arrayOfNulls<CellProperties>(64).apply {
        for(i in 0..63)
            this[i] = CellProperties(NONE, NONE, DEFAULT, false)
    }
    fun cellAt(line: Int, column: Int) = cells[8 * (line - 1) + column]
    fun cellAt(n: Int) = cells[n]
    fun change(vararg changes: Transformation) {
        changes.forEach {
            it.apply {
                cells[position] = newProperties
            }
        }
    }
    fun fromFile(stream : InputStream) {
        for(i in 0..63) {
            val piece = stream.read()
            val color = stream.read()
            cells[i] = CellProperties(
                piece = piece,
                color = color,
                background = DEFAULT,
                reverse = color == BLACK
            )
        }
        stream.close()
    }
    //Debug
    fun setPiece(x : Int, y : Int, piece : Int) {
        cells[Point(x,y).toIndex()]!!.piece = piece

    }
    //debug
    fun setColor(x : Int, y : Int, color : Int) {
        cells[Point(x,y).toIndex()]!!.color = color
        cells[Point(x,y).toIndex()]!!.reverse = (color == BLACK)
    }
}

data class CellProperties(var piece: Int, var color: Int, var background: Int, var reverse: Boolean)
data class Transformation(var position: Int, var newProperties: CellProperties)

val defaultConfiguration : Configuration get() {
    val config = Configuration()
    for (position in 0..63) {
        var color = 0
        var reverse = false
        var piece : Int
        var background : Int
        if (position in 0..15) {
            color = BLACK
            reverse = true
        }
        background = DEFAULT

        piece =
            if ((position / 8) in 0..1 || (position / 8) in 6..7)
                if (position / 8 == 0 || position / 8 == 7)
                    when {
                        position % 8 == 0 -> ROOK
                        position % 8 == 1 -> KNIGHT
                        position % 8 == 2 -> BISHOP
                        position % 8 == 3 -> QUEEN
                        position % 8 == 4 -> KING
                        position % 8 == 5 -> BISHOP
                        position % 8 == 6 -> KNIGHT
                        position % 8 == 7 -> ROOK
                        else -> throw ArithmeticException("Execution of the code will never reach this point")

                    }
                else PAWN
            else NONE

        val properties = CellProperties(piece, color, background, reverse)
        val transformation = Transformation(position, properties)
        config.change(transformation)
    }
    return config
}
val debugConfiguration : Configuration get() {
    val configuration = Configuration()
    configuration.apply {
        setPiece(3,1,KNIGHT); setColor(3,1,WHITE)
        setPiece(6,1, BISHOP); setColor(6,1, BLACK)
        setPiece(7,1,KNIGHT); setColor(7,1,BLACK)
        setPiece(8,1,ROOK); setColor(8,1, BLACK)
        setPiece(2,2, KING); setColor(2,2,BLACK)
        setPiece(3,2,PAWN); setColor(3,2,WHITE)
        setPiece(7,2,PAWN); setColor(7,2, BLACK)
        setPiece(8,2,PAWN); setColor(8,2,BLACK)
        setPiece(1,3, PAWN); setColor(1,3, WHITE)
        setPiece(3,3,ROOK); setColor(3,3, WHITE)
        setPiece(1,4,PAWN); setColor(1,4,WHITE)
        setPiece(5,4, PAWN); setColor(5,4, BLACK)
        setPiece(7,4, KNIGHT); setColor(7,4,WHITE)
        setPiece(2,5,ROOK); setColor(2,5, WHITE)
        setPiece(5,5,PAWN); setColor(5,5,WHITE)
        setPiece(6,5,PAWN); setColor(6,5,BLACK)
        setPiece(5,6,BISHOP); setColor(5,6,WHITE)
        setPiece(5,7,KING); setColor(5,7,WHITE)
        setPiece(6,7,PAWN); setColor(6,7,WHITE)
        setPiece(7,7,PAWN); setColor(7,7,WHITE)
        setPiece(8,7,PAWN); setColor(8,7,WHITE)
    }
    return configuration
}