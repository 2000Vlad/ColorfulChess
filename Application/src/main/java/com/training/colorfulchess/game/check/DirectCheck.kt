package com.training.colorfulchess.game.check

import android.graphics.Point

class DirectCheck(override var king : Point,override var threat: Point) : Check(king, threat) {
    override fun filter(path: Sequence<Point>): Sequence<Point> {
        return path.filter {
            it.x == threat.x && it.y == threat.y
        }
    }
    constructor() : this(Point(0,0), Point(0,0)){

    }

    override fun equals(other: Any?): Boolean {
        if(!(other is DirectCheck)) return false
        val check = other as DirectCheck
        return check.threat == threat && check.king == king
    }

    override fun hashCode(): Int {
        var result = king.hashCode()
        result = 31 * result + threat.hashCode()
        return result
    }
}