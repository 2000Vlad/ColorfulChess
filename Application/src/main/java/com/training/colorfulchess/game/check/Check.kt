package com.training.colorfulchess.game.check

import android.graphics.Point
import kotlin.math.max
import kotlin.math.min

abstract class Check(open var king : Point, open var threat : Point) {
    val minX : Int get() = min(king.x, threat.x)
    val maxX : Int get() = max(king.x, threat.x)
    val minY : Int get() = min(king.y, threat.y)
    val maxY : Int get() = max(king.y, threat.y)
    abstract fun filter(path : Sequence<Point>) : Sequence<Point>
}