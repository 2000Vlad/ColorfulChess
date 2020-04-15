package com.training.colorfulchess.game.check

import android.graphics.Point
import androidx.annotation.IntDef
import java.lang.IllegalArgumentException
import kotlin.math.max
import kotlin.math.min

class DiagonalCheck(override var king: Point, override var threat: Point) : Check(king, threat) {

    private val diagonal: Int
        get() {
            return when(diagonalType) {
                MAIN_DIAGONAL -> king.x - king.y
                SECOND_DIAGONAL -> king.x + king.y
                else -> throw DiagonalException()
            }
        }
    @Diagonal
    val diagonalType: Int
        get() {
            if (king.x - king.y == threat.x - threat.y) return MAIN_DIAGONAL
            if (king.x + king.y == threat.x + threat.y) return SECOND_DIAGONAL
            throw DiagonalException()
        }

    override fun filter(path: Sequence<Point>): Sequence<Point> {
        return path.filter filt@{
            if (it == threat) return@filt true
            return@filt when (diagonalType) {
                MAIN_DIAGONAL -> it.x - it.y == diagonal && it.x in minX..maxX
                SECOND_DIAGONAL -> it.x + it.y == diagonal && it.y in minY..maxY
                else -> throw DiagonalTypeException()
            }

        }
    }

}

const val MAIN_DIAGONAL = -1
const val SECOND_DIAGONAL = -2

class DiagonalException(msg : String) : Exception(msg) {
    constructor() : this("King and threat are not on the same diagonal"){

    }
}

class DiagonalTypeException(msg : String) : Exception(msg) {
    constructor() : this("Diagonal type must me one of [MAIN_DIAGONAL, SECOND_DIAGONAL]")
}
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY
)
@IntDef(MAIN_DIAGONAL, SECOND_DIAGONAL)
annotation class Diagonal {

}