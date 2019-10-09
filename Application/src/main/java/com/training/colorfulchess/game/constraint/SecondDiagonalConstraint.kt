package com.training.colorfulchess.game.constraint

import android.graphics.Point

class SecondDiagonalConstraint(override var king : Point, override var friend : Point) : Constraint(king, friend) {
    val sum get() = king.x + king.y
    override fun filter(path : Sequence<Point>) : Sequence<Point> {
        return path.filter {
            it.x + it.y == sum
        }
    }
    override fun equals(other: Any?): Boolean {
        if (!(other is SecondDiagonalConstraint)) return false
        val constraint = other as SecondDiagonalConstraint
        return king.x == constraint.king.x &&
                king.y == constraint.king.y &&
                friend.x == constraint.friend.x &&
                friend.y == constraint.friend.y
    }

    override fun hashCode(): Int {
        var result = king.hashCode()
        result = 31 * result + friend.hashCode()
        return result
    }
}