package com.training.colorfulchess.game.constraint

import android.graphics.Point
import com.training.colorfulchess.game.check.*

class LinearConstraint(override var king: Point, override var friend: Point) : Constraint(king,friend){
    val line : Int get() {
        return when(lineType) {
            HORIZONTAL -> king.y
            VERTICAL -> king.x
            else -> throw LineTypeException()
        }
    }
    @Linear
    val lineType : Int get() {
        if(king.x == friend.x) return VERTICAL
        if(king.y == friend.y) return HORIZONTAL
        throw LinearException("King and friend are not on the same line")
    }
    override fun filter(path: Sequence<Point>): Sequence<Point> {
        return path.filter filt@{
           when(lineType) {
               HORIZONTAL -> it.y == line && it.x in minX..maxX
               VERTICAL -> it.x == line && it.y in minY..maxY
               else -> throw LineTypeException()
           }
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other !is LinearConstraint)
            return false

        return king == other.king && friend == other.friend
    }

}