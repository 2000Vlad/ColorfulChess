package com.training.colorfulchess.game.check

import android.app.Application
import android.content.res.XmlResourceParser
import android.graphics.Point

class SecondDiagonalCheck(override var king: Point, override var threat: Point) : Check(king, threat) {

    val sum: Int get() = king.x + king.y

    override fun filter(path: Sequence<Point>): Sequence<Point> {
        return path.filter fil@{
            it.run {
                if(x > maxX || x < minX)  return@fil false
                if(y > maxY || y < minY)  return@fil false
                 return@fil it.x + it.y == sum

            }
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