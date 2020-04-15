package com.training.colorfulchess.game.tabular

import android.graphics.Point
import com.training.colorfulchess.game.modelvm2.Piece
import com.training.colorfulchess.game.modelvm2.PieceColor
import java.lang.Exception
import java.util.*
import kotlin.Comparator

class TabularLayout {
    var lines: List<TreeSet<TabularElement>> = initialize(SIZE, xComparator)

    var columns: List<TreeSet<TabularElement>> = initialize(SIZE, yComparator)

    var mainDiagonals: List<TreeSet<TabularElement>> = initialize(SIZE * 2 - 1, defaultComparator)

    var secondDiagonals: List<TreeSet<TabularElement>> = initialize(SIZE * 2 - 1, defaultComparator)

    fun insert(element: TabularElement) {
        val line = element.position.y - 1
        val column = element.position.x - 1
        val mainDiagonal = element.position.mainDiagonal
        val secondDiagonal = element.position.secondDiagonal

        lines[line].add(element)
        columns[column].add(element)
        mainDiagonals[mainDiagonal].add(element)
        secondDiagonals[secondDiagonal].add(element)

    }

    fun remove(element: TabularElement) {
        val line = element.position.y - 1
        val column = element.position.x - 1
        val mainDiagonal = element.position.mainDiagonal
        val secondDiagonal = element.position.secondDiagonal

        lines[line].remove(element)
        columns[column].remove(element)
        mainDiagonals[mainDiagonal].remove(element)
        secondDiagonals[secondDiagonal].remove(element)
    }

    fun entry(point: Point): TabularEntry {

        val lineIndex = point.y - 1
        val columnIndex = point.x - 1
        val mainDiagonalIndex = point.mainDiagonal
        val secondDiagonalIndex = point.secondDiagonal
        val element: TabularElement?

        val line = lines[lineIndex]
        element = line.firstOrNull { it.position == point }
        if (element == null) throw TabularCoordinateException()

        val column = columns[columnIndex]
        if (element != column.firstOrNull { it.position == point }) throw TabularCoordinateException()

        val mainDiagonal = mainDiagonals[mainDiagonalIndex]
        if (element != mainDiagonal.firstOrNull { it.position == point }) throw TabularCoordinateException()

        val secondDiagonal = secondDiagonals[secondDiagonalIndex]
        if (element != secondDiagonal.firstOrNull { it.position == point }) throw TabularCoordinateException()

        return TabularEntry(line, column, mainDiagonal, secondDiagonal, element)

    }

}

class TabularEntry(
    val line: TreeSet<TabularElement>,
    val column: TreeSet<TabularElement>,
    val mainDiagonal: TreeSet<TabularElement>,
    val secondDiagonal: TreeSet<TabularElement>,
    val element: TabularElement
) {

}

data class TabularElement(val position: Point, @Piece val piece: Int, @PieceColor val color: Int)

//region Comparators
object xComparator : Comparator<TabularElement> {
    override fun compare(o1: TabularElement?, o2: TabularElement?): Int {
        if (o1 == null || o2 == null) throw KotlinNullPointerException()
        return o1.position.x - o2.position.x
    }
}

object yComparator : Comparator<TabularElement> {
    override fun compare(o1: TabularElement?, o2: TabularElement?): Int {
        if (o1 == null || o2 == null) throw KotlinNullPointerException()
        return o1.position.y - o2.position.y
    }
}

val defaultComparator = xComparator
//endregion

//region Helpers
private fun initialize(
    size: Int,
    comparator: Comparator<TabularElement>
): List<TreeSet<TabularElement>> {
    val list = mutableListOf<TreeSet<TabularElement>>()
    for (i in 1..size) {
        list.add(sortedSetOf(comparator))
    }
    return list
}

private const val SIZE = 8

val Point.secondDiagonal: Int get() = this.x + this.y - 2
val Point.mainDiagonal: Int get() = (SIZE - this.x) + this.y - 1
//endregion

//region Exceptions
class TabularCoordinateException(msg: String) : Exception(msg) {
    constructor() : this("A tabular entry must have four coordinates: [LINE, COLUMN, MAIN_DIAGONAL, SECOND_DIAGONAL]")
}
//