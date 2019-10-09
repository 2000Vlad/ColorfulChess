package com.training.colorfulchess.game.constraint

import android.graphics.Point

abstract class Constraint(open var king : Point, open var friend : Point) {
    abstract fun filter(path : Sequence<Point>) : Sequence<Point>;
}