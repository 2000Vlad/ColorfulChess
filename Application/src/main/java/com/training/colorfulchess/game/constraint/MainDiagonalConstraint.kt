package com.training.colorfulchess.game.constraint

import android.graphics.Point

class MainDiagonalConstraint(override var king: Point, override var friend: Point) : Constraint(king, friend) {
    val diff get() = king.x - king.y
    override fun filter(path: Sequence<Point>): Sequence<Point> {
        return path.filter {
            it.x - it.y == diff
        }
    }

    override fun equals(other: Any?): Boolean {
        if (!(other is MainDiagonalConstraint)) return false
        val constraint = other as MainDiagonalConstraint
        return king.x == constraint.king.x &&
                king.y == constraint.king.y &&
                friend.x == constraint.friend.x &&
                friend.y == constraint.friend.y
    }
}