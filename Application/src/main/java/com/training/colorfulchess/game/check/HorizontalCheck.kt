package com.training.colorfulchess.game.check

import android.graphics.Point

class HorizontalCheck(override var king: Point, override var threat: Point) : Check(king, threat) {

    override fun filter(path : Sequence<Point>) : Sequence<Point> {
        return path.filter fil@{
             it.run {
                if(y != king.y)  return@fil false
                 return@fil x in minX..maxX
            }
        }

    }
    constructor() : this(Point(0,0), Point(0,0)){

    }
    override fun equals(other: Any?): Boolean {
        if(!(other is HorizontalCheck)) return false
        val check = other as HorizontalCheck
        return check.threat == threat && check.king == king
    }
}