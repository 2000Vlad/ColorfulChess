package com.training.colorfulchess.game.check

import android.graphics.Point

class VerticalCheck (override var king : Point, override var threat : Point) : Check(king, threat) {

    override fun filter(path : Sequence<Point>) : Sequence<Point> {
        return path.filter fil@{
            if(it.x != king.x)
                 return@fil false
             val result : Boolean = (it.y in minY..maxY)
             return@fil result
        }
    }
    constructor() : this(Point(0,0), Point(0,0)){

    }
    override fun equals(other: Any?): Boolean {
        if(!(other is SecondDiagonalCheck)) return false
        val check = other as SecondDiagonalCheck
        return check.threat == threat && check.king == king
    }

}