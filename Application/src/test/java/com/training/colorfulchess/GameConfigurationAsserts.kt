package com.training.colorfulchess

import android.graphics.Point
import com.training.colorfulchess.game.constraint.Constraint
import com.training.colorfulchess.game.constraint.HorizontalConstraint
import com.training.colorfulchess.game.constraint.MainDiagonalConstraint
import com.training.colorfulchess.game.constraint.SecondDiagonalConstraint
import com.google.common.truth.Truth.*

fun assertPoints(actual: Point, expected: Point) {
    assert(expected.x == actual.x)
    assert(expected.y == actual.y)
}

fun assert_King_Path_Works_Correct_Case1(path: Sequence<Point>) {
    assert(path.toList().size == 0)
}

fun assert_King_Path_Works_Correct_Case2(path: Sequence<Point>) {
    assert(path.toList().size == 4)
    assert(path.toList().contains(Point(6, 3)))
    assert(path.toList().contains(Point(5, 5)))
    assert(path.toList().contains(Point(4, 5)))
    assert(path.toList().contains(Point(4, 4)))
}

fun assert_King_Path_Works_Correct_Case3(path: Sequence<Point>) {
    assert(path.toList().size == 2)
    assert(path.toList().contains(Point(4, 6)))
    assert(path.toList().contains(Point(5, 5)))
}

fun assert_getConstraints_Case1(constraints: ArrayList<Constraint>) {
    assert(constraints.size == 3)
    assert(constraints.contains(HorizontalConstraint(Point(5, 5), Point(6, 5))))
    assert(constraints.contains(MainDiagonalConstraint(Point(5, 5), Point(6, 6))))
    assert(constraints.contains(HorizontalConstraint(Point(5, 5), Point(4, 5))))


}

fun assert_getConstraints_Case2(constraints: ArrayList<Constraint>) {
    assert(constraints.size == 4)
    assert(constraints.contains(HorizontalConstraint(Point(5, 5), Point(6, 5))))
    assert(constraints.contains(MainDiagonalConstraint(Point(5, 5), Point(6, 6))))
    assert(constraints.contains(HorizontalConstraint(Point(5, 5), Point(4, 5))))
    assert(constraints.contains(MainDiagonalConstraint(Point(5, 5), Point(4, 4))))


}

fun assert_getConstraints_Case3(constraints: ArrayList<Constraint>) {
    assert(constraints.size == 5)
    assert(constraints.contains(HorizontalConstraint(Point(5, 5), Point(6, 5))))
    assert(constraints.contains(MainDiagonalConstraint(Point(5, 5), Point(6, 6))))
    assert(constraints.contains(HorizontalConstraint(Point(5, 5), Point(4, 5))))
    assert(constraints.contains(MainDiagonalConstraint(Point(5, 5), Point(4, 4))))
    assert(constraints.contains(SecondDiagonalConstraint(Point(5, 5), Point(3, 7))))


}

fun assert_getAvailablaPositions_Pawn_Case1_0(positions: List<Point>) {
    assert(positions.size == 2)
    assert(positions[0] == Point(4, 6))
    assert(positions[1] == Point(5, 6))
}

fun assert_getAvailablaPositions_Pawn_Case1_1(positions: List<Point>) {
    assert(positions.size == 1)
    assert(positions[0] == Point(4, 8))
}

fun assert_getAvailablePositions_Pawn_Case2_0(positions: List<Point>) {
    assert(positions.size == 1)
    assert(positions[0] == Point(4,6))
}

fun assert_getAvailablePositions_Pawn_Case2_1(positions: List<Point>) {
    assert(positions.isEmpty())

}

fun assert_getAvailablePositions_Pawn_Case3_0(positions: List<Point>) {
    assertThat(positions).isEmpty()
}

fun assert_getAvailablePositions_Pawn_Case3_1(positions: List<Point>) {
    assertThat(positions).isEmpty()
}

fun assert_getAvailablePositions_Bishop_Case1_0(positions: List<Point>) {
    assertThat(positions).containsExactly(
        Point(3,3),
        Point(2,2),
        Point(5,3),
        Point(5,5),
        Point(3,5),
        Point(2,6),
        Point(1,7)
    )
}

fun assert_getAvailablePositions_Bishop_Case1_1(positions: List<Point>) {
    assertThat(positions).containsExactly(
        Point(3,3),
        Point(2,2),
        Point(5,3),
        Point(5,5),
        Point(3,5),
        Point(2,6),
        Point(1,7)
    )
}

fun assert_getAvailablePositions_Bishop_Case2_0(positions: List<Point>) {
    assertThat(positions).containsExactly(
        Point(3,3),
        Point(2,2)
    )
}

fun assert_getAvailablePositions_Bishop_Case3_0(positions: List<Point>) {
    assertThat(positions).containsExactly(
        Point(5,3),
        Point(6,2)
    )
}

fun assert_getAvailablePositions_Bishop_Case4_0(positions: List<Point>) {
    assertThat(positions).containsExactly(
        Point(2,6)
    )
}