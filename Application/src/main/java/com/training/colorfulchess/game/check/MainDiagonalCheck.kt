package com.training.colorfulchess.game.check

import android.graphics.Point

class MainDiagonalCheck(override var king: Point, override var threat: Point) : Check(king, threat) {

    val diff: Int get() = king.x - king.y
    constructor() : this(Point(0,0), Point(0,0)){

    }
    override fun filter(path: Sequence<Point>): Sequence<Point> {
        return path.filter fil@{
            it.run {
                if(x > maxX || x < minX) return@fil false
                if(y > maxY || y < minY) return@fil false
                 return@fil it.x - it.y == diff
            }
        }
    }
    override fun equals(other: Any?): Boolean {
        if(!(other is MainDiagonalCheck)) return false
        val check = other as MainDiagonalCheck
        return check.threat == threat && check.king == king
    }
}