package com.training.colorfulchess.game.check

import android.graphics.Point
import androidx.annotation.IntDef
import java.lang.Exception

class LinearCheck(override var king: Point, override var threat: Point) : Check(king, threat) {

    val line: Int
        get() =
            when (lineType) {
                HORIZONTAL -> king.y
                VERTICAL -> king.x
                else -> throw LineTypeException()
            }
    @Linear
    private val lineType: Int
        get() {
            if (king.y == threat.y) return HORIZONTAL
            if (king.x == threat.x) return VERTICAL
            throw LinearException()
        }

    override fun filter(path: Sequence<Point>): Sequence<Point> =
        path.filter filt@{
            when (lineType) {
                VERTICAL -> it.x == line && it.y in minY..maxY
                HORIZONTAL -> it.y == line && it.x in minX..maxX
                else -> throw LineTypeException()
            }
        }
}

const val HORIZONTAL = -3
const val VERTICAL = -4

class LinearException(msg: String) : Exception(msg) {
    constructor() : this("King and threat are not on the same line")
}

class LineTypeException(msg: String) : Exception(msg) {
    constructor() : this("Line type must be one of [HORIZONTAL, VERTICAL]")

}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY
)
@IntDef(HORIZONTAL, VERTICAL)
annotation class Linear