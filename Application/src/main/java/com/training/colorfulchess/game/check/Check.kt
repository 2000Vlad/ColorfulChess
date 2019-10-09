package com.training.colorfulchess.game.check

import android.graphics.Point

abstract class Check(open var king : Point, open var threat : Point) {
    abstract fun filter(path : Sequence<Point>) : Sequence<Point>
}