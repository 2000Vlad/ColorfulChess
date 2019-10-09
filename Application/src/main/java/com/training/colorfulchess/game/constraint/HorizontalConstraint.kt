package com.training.colorfulchess.game.constraint

import android.graphics.Point

class HorizontalConstraint(override var king : Point, override var friend : Point) : Constraint(king, friend) {
    val y = king.y
    override fun filter(path : Sequence<Point>) : Sequence<Point> {
        return  path.filter {
            it.y == y
        }
    }
    override fun equals(other: Any?): Boolean {
        if (!(other is HorizontalConstraint)) return false
        val constraint = other as HorizontalConstraint
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