package com.training.colorfulchess.game.constraint

import android.graphics.Point

class VerticalConstraint(override var king : Point, override var friend : Point) : Constraint(king, friend) {
    val x = king.x
    override fun filter(path : Sequence<Point>) : Sequence<Point> {
        return  path.filter {
            it.x == x
        }
    }
    override fun equals(other: Any?): Boolean {
        if (!(other is VerticalConstraint)) return false
        val constraint = other as VerticalConstraint
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