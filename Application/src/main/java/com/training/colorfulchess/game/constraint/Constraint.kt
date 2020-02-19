package com.training.colorfulchess.game.constraint

import android.graphics.Point
import com.training.colorfulchess.game.check.Check
import com.training.colorfulchess.game.check.DiagonalCheck
import com.training.colorfulchess.game.check.LinearCheck
import kotlin.math.max
import kotlin.math.min

abstract class Constraint(open var king : Point, open var friend : Point) {
    val minX : Int get() = min(king.x, friend.x)
    val maxX : Int get() = max(king.x, friend.x)
    val minY : Int get() = min(king.y, friend.y)
    val maxY : Int get() = max(king.y, friend.y)
    abstract fun filter(path : Sequence<Point>) : Sequence<Point>

}