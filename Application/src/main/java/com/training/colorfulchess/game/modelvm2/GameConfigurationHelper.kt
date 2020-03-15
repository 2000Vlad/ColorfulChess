package com.training.colorfulchess.game.modelvm2

import android.graphics.Point
import androidx.annotation.IntDef
import com.training.colorfulchess.game.*
import java.lang.Exception

fun Point.forward(): Point {
    return Point(x, y - 1)
}

fun Point.backward(): Point {
    return Point(x, y + 1)
}

fun Point.left(): Point {
    return Point(x - 1, y)
}

fun Point.right(): Point {
    return Point(x + 1, y)
}

fun Point.forwardLeft(): Point {
    return Point(x - 1, y - 1)
}

fun Point.forwardRight(): Point {
    return Point(x + 1, y - 1)
}

fun Point.backwardRight(): Point {
    return Point(x + 1, y + 1)
}

fun Point.backwardLeft(): Point {
    return Point(x - 1, y + 1)
}

fun Point.toIndex(): Int {
    var index = (y - 1) * 8
    index += x - 1
    return index
}

fun Int.toPoint(): Point {
    val x = this % 8 + 1
    val y = this / 8 + 1
    return Point(x, y)
}

fun Point.copy(): Point {
    return Point(x, y)
}

fun Point.inBounds(): Boolean {
    return x in 1..8 && y in 1..8
}

const val PLAYER_1 = 11
const val PLAYER_2 = 12

@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY
)
@IntDef(
    WHITE,
    BLACK
)
annotation class PieceColor

@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD
)
@IntDef(
    PAWN,
    BISHOP,
    KNIGHT,
    ROOK,
    QUEEN,
    KING
)
annotation class Piece

@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY
)
@IntDef(
    CHECKMATE,
    STALEMATE,
    NONE
)
annotation class GameState

@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY
)
@IntDef(
    PLAYER_1,
    PLAYER_2
)
annotation class Player
//endregion

data class GameCell(var piece: Int, var color: Int)

//region Exceptions
class PieceException(msg: String) : Exception(msg) {
    constructor() : this("Piece must be one of [PAWN, BISHOP, KNIGHT, ROOK, KING, QUEEN]")

}

class ColorException(msg: String) : Exception(msg) {
    constructor() : this("Color must be one of [WHITE, BLACK]")
}

class PlayerException(msg: String) : Exception(msg) {
    constructor() : this("Player must be one of [PLAYER_1, PLAYER_2]")
}