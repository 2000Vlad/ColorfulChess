package com.training.colorfulchess.game.constraint

import android.graphics.Point
import com.training.colorfulchess.game.check.*


class DiagonalConstraint(override var king: Point, override var friend: Point) :
    Constraint(king, friend) {

    val diagonal: Int
        get() {
            return when(diagonalType) {
                MAIN_DIAGONAL -> king.x - king.y
                SECOND_DIAGONAL -> king.x + king.y
                else -> throw DiagonalTypeException()
            }
        }
    @Diagonal
    val diagonalType: Int
        get() {
            if(king.x - king.y == friend.x - friend.y) return MAIN_DIAGONAL

            if(king.x + king.y == friend.x + friend.y) return SECOND_DIAGONAL

            throw DiagonalException("King and friend are not on the same diagonal")
        }

    override fun filter(path: Sequence<Point>): Sequence<Point> {
       return path.filter filt@{
           when(diagonalType) {
               MAIN_DIAGONAL -> it.x - it.y == diagonal
               SECOND_DIAGONAL -> it.x + it.y == diagonal
               else -> throw DiagonalTypeException()
           }
       }
    }


    override fun equals(other: Any?): Boolean {
        if(other !is DiagonalConstraint)
            return false

        return king == other.king && friend == other.friend
    }

    override fun hashCode(): Int {
        var result = king.hashCode()
        result = 31 * result + friend.hashCode()
        return result
    }

}