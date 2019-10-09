package com.training.colorfulchess.game

import android.graphics.Point
import com.training.colorfulchess.game.check.*
import com.training.colorfulchess.game.constraint.*
import java.lang.IllegalArgumentException
import kotlin.math.abs


open class GameConfiguration {
    //de adaugat pentru fiecare piesa verificare sa nu lase regele in sah
    var table = arrayOfNulls<GameCell>(64).apply {
        for (i in 0..63) {
            this[i] = GameCell(NONE, NONE)
        }
    }
    var whiteKingPosition: Point = Point(1, 1)
    var blackKingPosition: Point = Point(1, 1)

    var blackKingPath: Sequence<Point> = emptySequence()
    var whiteKingPath: Sequence<Point> = emptySequence()

    var whiteKingChecks = arrayListOf<Check>()
    var blackKingChecks = arrayListOf<Check>()

    var blackKingConstraints = arrayListOf<Constraint>()
    var whiteKingConstraints = arrayListOf<Constraint>()


    fun prepareForNextTurn() {

        blackKingPath = getKingPositions(blackKingPosition)
        whiteKingPath = getKingPositions(whiteKingPosition)

        blackKingChecks = getChecks(blackKingPosition)
        whiteKingChecks = getChecks(whiteKingPosition)

        blackKingConstraints = getConstraints(blackKingPosition)
        whiteKingConstraints = getConstraints(whiteKingPosition)

    }

    //Case : there's only one check and there's a piece that can block it
    val gameState: Int
        get() {
            if (blackKingChecks.isNotEmpty())
                if (blackKingPath.toList().isEmpty()) {
                    if (blackKingChecks.size > 1)
                        return PLAYER_1
                    for (cell in table.withIndex()) {
                        if (cell.value!!.color == BLACK && cell.value!!.piece != KING) {
                            val absolutePositions = getAvailablePositions(cell.index.toPoint())
                            val positions = blackKingChecks[0].filter(absolutePositions)
                            if (positions.toList().isNotEmpty())
                                return NONE
                        }
                    }
                    return PLAYER_1
                }

            if (whiteKingChecks.isNotEmpty())
                if (whiteKingPath.toList().isEmpty()) {
                    if (whiteKingChecks.size > 1)
                        return PLAYER_2
                    for (cell in table.withIndex()) {
                        if (cell.value!!.color == WHITE && cell.value!!.piece != KING) {
                            val absolutePositions = getAvailablePositions(cell.index.toPoint())
                            val positions = whiteKingChecks[0].filter(absolutePositions)
                            if (positions.toList().isNotEmpty())
                                return NONE
                        }
                    }
                    return PLAYER_2
                }
            return NONE
        }

    fun move(from: Point, to: Point) {
        if (getPiece(from) == KING)
            when (getColor(from)) {
                BLACK -> blackKingPosition = to.copy()
                WHITE -> whiteKingPosition = to.copy()
                else -> throw IllegalArgumentException("King color incorrect")
            }
        val piece = getPiece(from)
        val color = getColor(from)
        setPiece(to, piece)
        setColor(to, color)
        setPiece(from, NONE)
        setColor(from, NONE)
    }
}

val PLAYER_1 = 1
val PLAYER_2 = 2

data class GameCell(var piece: Int, var color: Int) {

}

//Cea mai importanta metoda
fun GameConfiguration.getAvailablePositions(piece: Point): Sequence<Point> {
    var positions: Sequence<Point> = emptySequence()

    val absolutePositions =
        when (getPiece(piece)) {
            KING -> if (getColor(piece) == WHITE) whiteKingPath else blackKingPath
            QUEEN -> getAbsoluteQueenMovingPositions(piece)
            KNIGHT -> getAbsoluteKnightMovingPositions(piece)
            ROOK -> getAbsoluteRookMovingPositions(piece)
            BISHOP -> getAbsoluteBishopMovingPositions(piece)
            PAWN -> getAbsolutePawnMovingPositions(piece)
            else -> throw IllegalArgumentException("Piece set incorrect")
        }
    val checks = when (getColor(piece)) {
        WHITE -> whiteKingChecks
        BLACK -> blackKingChecks
        else -> throw  IllegalArgumentException("color set incorrect")
    }
    val constraints = when (getColor(piece)) {
        WHITE -> whiteKingConstraints
        BLACK -> blackKingConstraints
        else -> throw  IllegalArgumentException("color set incorrect")
    }
    positions = absolutePositions
    if (checks.size > 1 && getPiece(piece) != KING)
        return emptySequence()
    if (checks.isNotEmpty())
        if (getPiece(piece) != KING)
            positions = checks[0].filter(absolutePositions)
    for (constraint in constraints)
        if (constraint.friend == piece)
            positions = constraint.filter(positions)
    return positions
}

fun GameConfiguration.getChecks(king: Point): ArrayList<Check> {
    val list = arrayListOf<Check>()
    val forward = getCheckInDirection(king, Point::forward, THREAT_VERTICAL)
    val forwardRight = getCheckInDirection(king, Point::forwardRight, THREAT_SECOND_DIAGONAL)
    val right = getCheckInDirection(king, Point::right, THREAT_HORIZONTAL)
    val backwardRight = getCheckInDirection(king, Point::backwardRight, THREAT_MAIN_DIAGONAL)
    val backward = getCheckInDirection(king, Point::backward, THREAT_VERTICAL)
    val backwardLeft = getCheckInDirection(king, Point::backwardLeft, THREAT_SECOND_DIAGONAL)
    val left = getCheckInDirection(king, Point::left, THREAT_HORIZONTAL)
    val forwardLeft = getCheckInDirection(king, Point::forwardLeft, THREAT_MAIN_DIAGONAL)
    if (forward != null)
        list.add(forward)
    if (forwardRight != null)
        list.add(forwardRight)
    if (right != null)
        list.add(right)
    if (backwardRight != null)
        list.add(backwardRight)
    if (backward != null)
        list.add(backward)
    if (backwardLeft != null)
        list.add(backwardLeft)
    if (left != null)
        list.add(left)
    if (forwardLeft != null)
        list.add(forwardLeft)
    list.addAll(getPawnChecks(king))
    list.addAll(getKnightChecks(king))
    return list
}

fun GameConfiguration.getCheckInDirection(
    king: Point,
    nextPoint: Point.() -> Point,
    threatType: Int
): Check? {
    var current = king.nextPoint()
    while (current.inBounds() && isEmpty(current)) current = current.nextPoint()
    if (!current.inBounds()) return null
    if (getColor(current) == getColor(king)) return null
    return when (threatType) {
        THREAT_HORIZONTAL ->
            if (isLinearThreat(current, getColor(king))) HorizontalCheck(king, current)
            else null

        THREAT_VERTICAL ->
            if (isLinearThreat(current, getColor(king))) VerticalCheck(king, current)
            else null

        THREAT_MAIN_DIAGONAL ->
            if (isDiagonalThreat(current, getColor(king))) MainDiagonalCheck(king, current)
            else null

        THREAT_SECOND_DIAGONAL ->
            if (isDiagonalThreat(current, getColor(king))) SecondDiagonalCheck(king, current)
            else null


        else -> throw IllegalArgumentException("threatType incorrect")
    }
}

fun GameConfiguration.isLinearThreat(piece: Point, friendColor: Int): Boolean {
    if (getColor(piece) == friendColor) return false
    return isRook(piece) || isQueen(piece)
}

fun GameConfiguration.isDiagonalThreat(piece: Point, friendColor: Int): Boolean {
    if (getColor(piece) == friendColor) return false
    return isBishop(piece) || isQueen(piece)
}

fun GameConfiguration.getKnightChecks(king: Point): ArrayList<DirectCheck> {
    val seed: Triple<Point, Point, Point> =
        Triple(king.forward().forward().left(), Point(2, 0), Point(1, 1))
    val positions: Sequence<Point> =
        generateSequence(seed) {
            if (it.second.x == 1 && it.second.y == -1)
                return@generateSequence null
            val newPoint = Point(it.first.x + it.second.x, it.first.y + it.second.y)
            val newDelta = it.second
            val deltaVariation = it.third
            if (abs(newDelta.x) == 2)
                deltaVariation.x *= -1
            if (abs(newDelta.y) == 2)
                deltaVariation.y *= -1
            newDelta.x += deltaVariation.x
            newDelta.y += deltaVariation.y
            Triple(newPoint, newDelta, deltaVariation)
        }.map {
            it.first
        }.filter {
            it.inBounds()
        }.filter {
            getPiece(it) == KNIGHT && getColor(it) != getColor(king)
        }
    val checks = arrayListOf<DirectCheck>()
    for (pos in positions) {
        checks.add(DirectCheck(king, pos))
    }
    return checks
}

fun GameConfiguration.getPawnChecks(king: Point): ArrayList<DirectCheck> {
    var positions: Sequence<Point> = emptySequence()
    if (isWhite(king)) {
        val fLeft = king.forwardLeft()
        val fRight = king.forwardRight()
        if (fLeft.inBounds() && isPawn(fLeft) && isBlack(fLeft)) positions += sequenceOf(fLeft)
        if (fRight.inBounds() && isPawn(fRight) && isBlack(fRight)) positions += sequenceOf(fRight)
    }
    if (isBlack(king)) {
        val bRight = king.backwardRight()
        val bLeft = king.backwardLeft()
        if (bLeft.inBounds() && isPawn(bLeft) && isWhite(bLeft)) positions += sequenceOf(bLeft)
        if (bRight.inBounds() && isPawn(bRight) && isWhite(bRight)) positions += sequenceOf(bRight)
    }
    val checks = arrayListOf<DirectCheck>()
    for (pos in positions) {
        checks.add(DirectCheck(king, pos))
    }
    return checks
}

fun GameConfiguration.getConstraints(king: Point): ArrayList<Constraint> {
    val constraints = arrayListOf<Constraint>()
    constraints.addAll(getHorizontalConstraints(king))
    constraints.addAll(getVerticalConstraints(king))
    constraints.addAll(getMainDiagonalConstraints(king))
    constraints.addAll(getSecondDiagonalConstraints(king))
    return constraints
}


fun GameConfiguration.getHorizontalConstraints(king: Point): ArrayList<HorizontalConstraint> {
    val list = arrayListOf<HorizontalConstraint>()

    var current = king

    while (current.left().inBounds() && isEmpty(current.left())) {

        current = current.left()
    }
    val lBound = current.left()

    if (lBound.inBounds() && areFriends(king, lBound)) {

        current = lBound.copy() //To copy

        while (current.left().inBounds() && isEmpty(current.left())) {

            current = current.left()
        }
        if (current.left().inBounds() && areEnemies(king, current.left()))

            if (isRook(current.left()) || isQueen(current.left()))

                list.add(HorizontalConstraint(king, lBound))
    }
    current = king.copy()

    while (current.right().inBounds() && isEmpty(current.right())) {

        current = current.right()
    }
    val rBound = current.right()
    if (rBound.inBounds() && areFriends(king, rBound)) {

        current = rBound.copy()//To copy
        while (current.right().inBounds() && isEmpty(current.right())) {

            current = current.right()
        }
        if (current.right().inBounds() && areEnemies(king, current.right()))

            if (isRook(current.right()) || isQueen(current.right()))

                list.add(HorizontalConstraint(king, rBound))
    }
    return list
}

fun GameConfiguration.getVerticalConstraints(king: Point): ArrayList<VerticalConstraint> {

    val list = arrayListOf<VerticalConstraint>()

    var current = king

    while (current.forward().inBounds() && isEmpty(current.forward())) {

        current = current.forward()
    }
    val fBound = current.forward()

    if (fBound.inBounds() && areFriends(king, fBound)) {

        current = fBound.copy()

        while (current.forward().inBounds() && isEmpty(current.forward()))

            current = current.forward()

        if (current.forward().inBounds() && areEnemies(current.forward(), king))

            if (isRook(current.forward()) || isQueen(current.forward()))

                list.add(VerticalConstraint(king, fBound))
    }
    current = king.copy()

    while (current.backward().inBounds() && isEmpty(current.backward())) {

        current = current.backward()
    }
    val bBound = current.backward()

    if (bBound.inBounds() && areFriends(king, bBound)) {

        current = bBound.copy()

        while (current.backward().inBounds() && isEmpty(current.backward()))

            current = current.backward()

        if (current.backward().inBounds() && areEnemies(current.backward(), king))

            if (isRook(current.backward()) || isQueen(current.backward()))

                list.add(VerticalConstraint(king, bBound))
    }
    return list
}

fun GameConfiguration.getMainDiagonalConstraints(king: Point): ArrayList<MainDiagonalConstraint> {
    val list = arrayListOf<MainDiagonalConstraint>()

    var current = king

    while (current.forwardLeft().inBounds() && isEmpty(current.forwardLeft())) {

        current = current.forwardLeft()
    }
    val flBound = current.forwardLeft()

    if (flBound.inBounds() && areFriends(king, flBound)) {

        current = flBound.copy()
        while (current.forwardLeft().inBounds() && isEmpty(current.forwardLeft())) {

            current = current.forwardLeft()
        }

        if (current.forwardLeft().inBounds() && areEnemies(king, current.forwardLeft()))

            if (isBishop(current.forwardLeft()) || isQueen(current.forwardLeft()))

                list.add(MainDiagonalConstraint(king, flBound))
    }
    current = king.copy()

    while (current.backwardRight().inBounds() && isEmpty(current.backwardRight())) {

        current = current.backwardRight()
    }
    val brBound = current.backwardRight()

    if (brBound.inBounds() && areFriends(king, brBound)) {
        current = brBound.copy()

        while (current.backwardRight().inBounds() && isEmpty(current.backwardRight())) {

            current = current.backwardRight()
        }

        if (current.backwardRight().inBounds() && areEnemies(king, current.backwardRight()))

            if (isBishop(current.backwardRight()) || isQueen(current.backwardRight()))

                list.add(MainDiagonalConstraint(king, brBound))
    }
    return list

}

fun GameConfiguration.getSecondDiagonalConstraints(king: Point): ArrayList<SecondDiagonalConstraint> {
    val list = arrayListOf<SecondDiagonalConstraint>()
    var current = king.copy()

    while (current.forwardRight().inBounds() && isEmpty(current.forwardRight())) {

        current = current.forwardRight()
    }
    val frBound = current.forwardRight()

    if (frBound.inBounds() && areFriends(king, frBound)) {

        current = frBound.copy()

        while (current.forwardRight().inBounds() && isEmpty(current.forwardRight())) {

            current = current.forwardRight()
        }

        if (current.forwardRight().inBounds() && areEnemies(king, current.forwardRight()))

            if (isBishop(current.forwardRight()) || isQueen(current.forwardRight()))

                list.add(SecondDiagonalConstraint(king, frBound))
    }
    current = king.copy()
    while (current.backwardLeft().inBounds() && isEmpty(current.backwardLeft())) {

        current = current.backwardLeft()
    }
    val blBound = current.backwardLeft()

    if (blBound.inBounds() && areFriends(king, blBound)) {

        current = blBound.copy()

        while (current.backwardLeft().inBounds() && isEmpty(current.backwardLeft())) {

            current = current.backwardLeft()
        }

        if (current.backwardLeft().inBounds() && areEnemies(king, current.backwardLeft()))

            if (isBishop(current.backwardLeft()) || isQueen(current.backwardLeft()))
                list.add(SecondDiagonalConstraint(king, blBound))
    }
    return list
}

fun GameConfiguration.getAbsoluteMovingPositions(piece: Point): Sequence<Point> =
    when (getPiece(piece)) {
        PAWN -> getAbsolutePawnMovingPositions(piece)
        BISHOP -> getAbsoluteBishopMovingPositions(piece)
        ROOK -> getAbsoluteRookMovingPositions(piece)
        KNIGHT -> getAbsoluteKnightMovingPositions(piece)
        QUEEN -> getAbsoluteQueenMovingPositions(piece)
        KING ->
            when (getColor(piece)) {
                WHITE -> whiteKingPath
                BLACK -> blackKingPath
                else -> throw IllegalArgumentException("King color incorrect")
            }
        else -> throw IllegalArgumentException("Piece is incorrect")
    }


fun GameConfiguration.getAbsolutePawnMovingPositions(pawn: Point): Sequence<Point> {
    var path: Sequence<Point> = emptySequence()
    if (isWhite(pawn)) {

        if (pawn.forward().inBounds() && isEmpty(pawn.forward())) {
            path += sequenceOf(pawn.forward())

            if (pawn.y == 7 && pawn.forward().forward().inBounds() && isEmpty(pawn.forward().forward()))
                path += sequenceOf(pawn.forward().forward())
        }

        if (pawn.forwardLeft().inBounds() && areEnemies(pawn, pawn.forwardLeft()))
            path += sequenceOf(pawn.forwardLeft())

        if (pawn.forwardRight().inBounds() && areEnemies(pawn, pawn.forwardRight()))
            path += sequenceOf(pawn.forwardRight())
    }
    if (isBlack(pawn)) {

        if (pawn.backward().inBounds() && isEmpty(pawn.backward())) {
            path += sequenceOf(pawn.backward())

            if (pawn.y == 2 && pawn.backward().backward().inBounds() && isEmpty(pawn.backward().backward()))
                path += sequenceOf(pawn.backward().backward())
        }

        if (pawn.backwardLeft().inBounds() && areEnemies(pawn, pawn.backwardLeft()))
            path += sequenceOf(pawn.backwardLeft())

        if (pawn.backwardRight().inBounds() && areEnemies(pawn, pawn.backwardRight()))
            path += sequenceOf(pawn.backwardRight())
    }

    return path
}


fun GameConfiguration.getAbsoluteRookMovingPositions(rook: Point): Sequence<Point> =
    getPointsTowardDirection(rook, Point::forward) +
            getPointsTowardDirection(rook, Point::right) +
            getPointsTowardDirection(rook, Point::backward) +
            getPointsTowardDirection(rook, Point::left)


fun GameConfiguration.getAbsoluteBishopMovingPositions(bishop: Point): Sequence<Point> =
    getPointsTowardDirection(bishop, Point::forwardLeft) +
            getPointsTowardDirection(bishop, Point::forwardRight) +
            getPointsTowardDirection(bishop, Point::backwardRight) +
            getPointsTowardDirection(bishop, Point::backwardLeft)

fun GameConfiguration.getAbsoluteQueenMovingPositions(queen: Point): Sequence<Point> =
    getPointsTowardDirection(queen, Point::forward) +
            getPointsTowardDirection(queen, Point::forwardRight) +
            getPointsTowardDirection(queen, Point::right) +
            getPointsTowardDirection(queen, Point::backwardRight) +
            getPointsTowardDirection(queen, Point::backward) +
            getPointsTowardDirection(queen, Point::backwardLeft) +
            getPointsTowardDirection(queen, Point::left) +
            getPointsTowardDirection(queen, Point::forwardLeft)


fun GameConfiguration.getAbsoluteKnightMovingPositions(knight: Point): Sequence<Point> {
    val seed: Triple<Point, Point, Point> =
        Triple(knight.forward().forward().left(), Point(2, 0), Point(1, 1))
    val positions: Sequence<Triple<Point, Point, Point>> =
        generateSequence(seed) {
            if (it.second.x == 1 && it.second.y == -1)
                return@generateSequence null
            val newPoint = Point(it.first.x + it.second.x, it.first.y + it.second.y)
            val newDelta = it.second
            val deltaVariation = it.third
            if (abs(newDelta.x) == 2)
                deltaVariation.x *= -1
            if (abs(newDelta.y) == 2)
                deltaVariation.y *= -1
            newDelta.x += deltaVariation.x
            newDelta.y += deltaVariation.y
            Triple(newPoint, newDelta, deltaVariation)
        }
    return positions.map {
        it.first
    }
        .filter {
            val pass = it.inBounds() && (isEmpty(it) || areEnemies(knight, it))
            pass
        }
}


fun GameConfiguration.getKingPositions(king: Point): Sequence<Point> {
    var result = emptySequence<Point>()
    val kingColor = getColor(king)
    val surrounding = listOf(
        king.forwardLeft(),
        king.forward(),
        king.forwardRight(),
        king.right(),
        king.backwardRight(),
        king.backward(),
        king.backwardLeft(),
        king.left()
    )
    setPiece(king, NONE)
    setColor(king, NONE)
    val threatColor = if (kingColor == WHITE) BLACK else WHITE
    for (pos in surrounding) {
        if (pos.inBounds())
            if (getColor(pos) != kingColor) {
                val threats = hasThreat(pos, kingColor)
                if (threats.isEmpty()) result += pos
            }
    }
    setPiece(king, KING)
    setColor(king, kingColor)
    return result
}

fun GameConfiguration.hasThreat(
    pos: Point,
    friendColor: Int
): Set<Int> {
    val list = mutableListOf<Int>()
    if (hasPawnThreat(pos, friendColor)) list.add(THREAT_DIRECT)
    if (hasKnightThreat(pos, friendColor)) list.add(THREAT_DIRECT)
    if (hasKingThreat(pos, friendColor)) list.add(THREAT_DIRECT)

    if (hasLinearThreatOnDirection(pos, friendColor, Point::left)) list.add(THREAT_HORIZONTAL)

    if (hasLinearThreatOnDirection(pos, friendColor, Point::right)) list.add(THREAT_HORIZONTAL)

    if (hasLinearThreatOnDirection(pos, friendColor, Point::forward)) list.add(THREAT_VERTICAL)

    if (hasLinearThreatOnDirection(pos, friendColor, Point::backward)) list.add(THREAT_VERTICAL)

    if (hasDiagonalThreatOnDirection(pos, friendColor, Point::forwardLeft)) list.add(
        THREAT_MAIN_DIAGONAL
    )
    if (hasDiagonalThreatOnDirection(
            pos,
            friendColor,
            Point::forwardRight
        )
    ) list.add(THREAT_SECOND_DIAGONAL)
    if (hasDiagonalThreatOnDirection(
            pos,
            friendColor,
            Point::backwardLeft
        )
    ) list.add(THREAT_MAIN_DIAGONAL)
    if (hasDiagonalThreatOnDirection(
            pos,
            friendColor,
            Point::backwardRight
        )
    ) list.add(THREAT_SECOND_DIAGONAL)

    return list.toSet()
    //TODO(Make this method return int : LINEAR, DIAGONAL, DIRECT or NONE
}

const val THREAT_HORIZONTAL = 1
const val THREAT_MAIN_DIAGONAL = 2
const val THREAT_DIRECT = 3
const val THREAT_SECOND_DIAGONAL = 4
const val THREAT_VERTICAL = 5


fun GameConfiguration.hasKnightThreat(pivot: Point, friendColor: Int): Boolean {
    val oldPiece = getPiece(pivot)
    val oldColor = getColor(pivot)
    setPiece(pivot, KNIGHT)
    setColor(pivot, friendColor)
    val positions = getAbsoluteKnightMovingPositions(pivot)
    for (pos in positions) {
        if (getPiece(pos) == KNIGHT && getColor(pos) != friendColor) {
            setPiece(pivot, oldPiece)
            setColor(pivot, oldColor)
            return true
        }
    }
    setPiece(pivot, oldPiece)
    setColor(pivot, oldColor)
    return false
}


fun GameConfiguration.hasPawnThreat(pivot: Point, friendColor: Int): Boolean {
    var positions: Sequence<Point> = emptySequence()
    if (friendColor == BLACK) {
        if (pivot.backwardLeft().inBounds())
            positions += listOf(pivot.backwardLeft())
        if (pivot.backwardRight().inBounds())
            positions += listOf(pivot.backwardRight())
    }
    if (friendColor == WHITE) {
        if (pivot.forwardLeft().inBounds())
            positions += listOf(pivot.forwardLeft())
        if (pivot.forwardRight().inBounds())
            positions += listOf(pivot.forwardRight())
    }
    for (pos in positions) {
        if (getPiece(pos) == PAWN && getColor(pos) != friendColor) return true
    }

    return false
}


fun GameConfiguration.hasLinearThreatOnDirection(
    pivot: Point,
    friendColor: Int,
    nextPoint: Point.() -> Point
): Boolean {
    var currentPoint = pivot.nextPoint()
    while (currentPoint.inBounds() && isEmpty(currentPoint))
        currentPoint = currentPoint.nextPoint()
    if (!currentPoint.inBounds())
        return false

    return (getColor(currentPoint) != friendColor) && (isRook(currentPoint) || isQueen(currentPoint))
}

fun GameConfiguration.hasDiagonalThreatOnDirection(
    pivot: Point,
    friendColor: Int,
    nextPoint: Point.() -> Point
): Boolean {
    var currentPoint = pivot.nextPoint()
    while (currentPoint.inBounds() && isEmpty(currentPoint))
        currentPoint = currentPoint.nextPoint()
    if (!currentPoint.inBounds())
        return false
    return (getColor(currentPoint) != friendColor) && (isBishop(currentPoint) || isQueen(
        currentPoint
    ))
}

fun GameConfiguration.hasKingThreat(pivot: Point, friendColor: Int): Boolean {
    if (pivot.forwardLeft().inBounds())
        if (getPiece(pivot.forwardLeft()) == KING)
            if (getColor(pivot.forwardLeft()) != friendColor)
                return true
    if (pivot.forward().inBounds())
        if (getPiece(pivot.forward()) == KING)
            if (getColor(pivot.forward()) != friendColor)
                return true
    if (pivot.forwardRight().inBounds())
        if (getPiece(pivot.forwardRight()) == KING)
            if (getColor(pivot.forwardRight()) != friendColor)
                return true
    if (pivot.right().inBounds())
        if (getPiece(pivot.right()) == KING)
            if (getColor(pivot.right()) != friendColor)
                return true
    if (pivot.backwardRight().inBounds())
        if (getPiece(pivot.backwardRight()) == KING)
            if (getColor(pivot.backwardRight()) != friendColor)
                return true
    if (pivot.backward().inBounds())
        if (getPiece(pivot.backward()) == KING)
            if (getColor(pivot.backward()) != friendColor)
                return true
    if (pivot.backwardLeft().inBounds())
        if (getPiece(pivot.backwardLeft()) == KING)
            if (getColor(pivot.backwardLeft()) != friendColor)
                return true
    if (pivot.left().inBounds())
        if (getPiece(pivot.left()) == KING)
            if (getColor(pivot.left()) != friendColor)
                return true
    return false

}

fun GameConfiguration.setPiece(point: Point, piece: Int) {
    table[point.toIndex()]!!.piece = piece
}

fun GameConfiguration.setColor(point: Point, color: Int) {
    table[point.toIndex()]!!.color = color
}

fun GameConfiguration.getColor(point: Point): Int = table[point.toIndex()]!!.color

fun GameConfiguration.getPiece(point: Point): Int = table[point.toIndex()]!!.piece

fun GameConfiguration.isPawn(pawn: Point) = table[pawn.toIndex()]!!.piece == PAWN

fun GameConfiguration.isBishop(bishop: Point) = table[bishop.toIndex()]!!.piece == BISHOP

fun GameConfiguration.isRook(rook: Point) = table[rook.toIndex()]!!.piece == ROOK

fun GameConfiguration.isKnight(knight: Point) = table[knight.toIndex()]!!.piece == KNIGHT

fun GameConfiguration.isKing(king: Point) = table[king.toIndex()]!!.piece == KING

fun GameConfiguration.isQueen(queen: Point) = table[queen.toIndex()]!!.piece == QUEEN


fun GameConfiguration.getPointsTowardDirection(
    piece: Point,
    nextPoint: Point.() -> Point
): Sequence<Point> {
    val seed =
        if (piece.nextPoint().inBounds())
            if (isEmpty(piece.nextPoint()) || areEnemies(piece, piece.nextPoint()))
                piece.nextPoint()
            else null
        else null
    return generateSequence(seed) {
        when {
            !it.nextPoint().inBounds() -> null
            areFriends(piece, it.nextPoint()) -> null
            areEnemies(piece, it) -> null
            else -> it.nextPoint()
        }
    }

}

//x is horizontal dimension
//y is vertical dimension

fun GameConfiguration.isEmpty(pos: Point): Boolean =
    table[pos.toIndex()]!!.piece == NONE

fun GameConfiguration.isWhite(pos: Point) =
    table[pos.toIndex()]!!.color == WHITE

fun GameConfiguration.isBlack(pos: Point) =
    table[pos.toIndex()]!!.color == BLACK

fun GameConfiguration.areFriends(pos1: Point, pos2: Point) =
    when {
        isEmpty(pos1) -> false
        isEmpty(pos2) -> false
        table[pos1.toIndex()]!!.color != table[pos2.toIndex()]!!.color -> false
        else -> true
    }

fun GameConfiguration.areEnemies(pos1: Point, pos2: Point) =
    when {
        isEmpty(pos1) -> false
        isEmpty(pos2) -> false
        table[pos1.toIndex()]!!.color == table[pos2.toIndex()]!!.color -> false
        else -> true
    }

fun Point.toIndex(): Int {
    var index = 0
    index = 8 * (this.y - 1)
    index += ((this.x - 1) % 8)

    return index
}

fun Point.copy(): Point = Point(this.x, this.y)

fun Point.forward(): Point = Point(this.x, this.y - 1)

fun Point.backward(): Point = Point(this.x, this.y + 1)

fun Point.left(): Point = Point(this.x - 1, this.y)

fun Point.right(): Point = Point(this.x + 1, this.y)

fun Point.forwardRight() = Point(this.x + 1, this.y - 1)

fun Point.backwardRight() = Point(this.x + 1, this.y + 1)

fun Point.backwardLeft() = Point(this.x - 1, this.y + 1)

fun Point.forwardLeft() = Point(this.x - 1, this.y - 1)

fun Point.inBounds(): Boolean {
    val inbounds = this.x >= 1 && this.x <= 8 && this.y >= 1 && this.y <= 8
    return inbounds
}

fun GameConfiguration.setPiece(x: Int, y: Int, piece: Int) = setPiece(Point(x, y), piece)
fun GameConfiguration.setColor(x: Int, y: Int, color: Int) = setColor(Point(x, y), color)
fun Int.toPoint(): Point {
    val x = (this % 8) + 1
    val y = (this / 8) + 1

    return Point(x, y)
}

class MutablePair<First, Second>(var first: First, var second: Second) {

}

val defaultGameConfiguration: GameConfiguration
    get() {
        val configuration = GameConfiguration()
        configuration.apply {
            setPiece(Point(1, 1), ROOK); setColor(Point(1, 1), BLACK)
            setPiece(Point(2, 1), KNIGHT); setColor(Point(2, 1), BLACK)
            setPiece(Point(3, 1), BISHOP); setColor(Point(3, 1), BLACK)
            setPiece(Point(4, 1), QUEEN); setColor(Point(4, 1), BLACK)
            setPiece(Point(5, 1), KING); setColor(Point(5, 1), BLACK)
            setPiece(Point(6, 1), BISHOP); setColor(Point(6, 1), BLACK)
            setPiece(Point(7, 1), KNIGHT); setColor(Point(7, 1), BLACK)
            setPiece(Point(8, 1), ROOK); setColor(Point(8, 1), BLACK)
        }
        configuration.apply {
            setPiece(Point(1, 2), PAWN); setColor(Point(1, 2), BLACK)
            setPiece(Point(2, 2), PAWN); setColor(Point(2, 2), BLACK)
            setPiece(Point(3, 2), PAWN); setColor(Point(3, 2), BLACK)
            setPiece(Point(4, 2), PAWN); setColor(Point(4, 2), BLACK)
            setPiece(Point(5, 2), PAWN); setColor(Point(5, 2), BLACK)
            setPiece(Point(6, 2), PAWN); setColor(Point(6, 2), BLACK)
            setPiece(Point(7, 2), PAWN); setColor(Point(7, 2), BLACK)
            setPiece(Point(8, 2), PAWN); setColor(Point(8, 2), BLACK)
        }
        configuration.apply {
            setPiece(Point(1, 8), ROOK); setColor(Point(1, 8), WHITE)
            setPiece(Point(2, 8), KNIGHT); setColor(Point(2, 8), WHITE)
            setPiece(Point(3, 8), BISHOP); setColor(Point(3, 8), WHITE)
            setPiece(Point(4, 8), QUEEN); setColor(Point(4, 8), WHITE)
            setPiece(Point(5, 8), KING); setColor(Point(5, 8), WHITE)
            setPiece(Point(6, 8), BISHOP); setColor(Point(6, 8), WHITE)
            setPiece(Point(7, 8), KNIGHT); setColor(Point(7, 8), WHITE)
            setPiece(Point(8, 8), ROOK); setColor(Point(8, 8), WHITE)
        }
        configuration.apply {
            setPiece(Point(1, 7), PAWN); setColor(Point(1, 7), WHITE)
            setPiece(Point(2, 7), PAWN); setColor(Point(2, 7), WHITE)
            setPiece(Point(3, 7), PAWN); setColor(Point(3, 7), WHITE)
            setPiece(Point(4, 7), PAWN); setColor(Point(4, 7), WHITE)
            setPiece(Point(5, 7), PAWN); setColor(Point(5, 7), WHITE)
            setPiece(Point(6, 7), PAWN); setColor(Point(6, 7), WHITE)
            setPiece(Point(7, 7), PAWN); setColor(Point(7, 7), WHITE)
            setPiece(Point(8, 7), PAWN); setColor(Point(8, 7), WHITE)
        }
        configuration.apply {
            whiteKingPosition = Point(5, 8)
            blackKingPosition = Point(5, 1)
            whiteKingChecks = arrayListOf()
            blackKingChecks = arrayListOf()

        }
        return configuration
    }
val debugGameConfiguration: GameConfiguration
    get() {
        val configuration = GameConfiguration()
        configuration.apply {
            setPiece(3, 1, KNIGHT); setColor(3, 1, WHITE)
            setPiece(6, 1, BISHOP); setColor(6, 1, BLACK)
            setPiece(7, 1, KNIGHT); setColor(7, 1, BLACK)
            setPiece(8, 1, ROOK); setColor(7, 1, BLACK)
            setPiece(2, 2, KING); setColor(2, 2, BLACK)
            setPiece(3, 2, PAWN); setColor(3, 2, WHITE)
            setPiece(7, 2, PAWN); setColor(7, 2, BLACK)
            setPiece(8, 2, PAWN); setColor(8, 2, BLACK)
            setPiece(1, 3, PAWN); setColor(1, 3, WHITE)
            setPiece(3, 3, ROOK); setColor(3, 3, WHITE)
            setPiece(1, 4, PAWN); setColor(1, 4, WHITE)
            setPiece(5, 4, PAWN); setColor(5, 4, BLACK)
            setPiece(7, 4, KNIGHT); setColor(7, 4, WHITE)
            setPiece(2, 5, ROOK); setColor(2, 5, WHITE)
            setPiece(5, 5, PAWN); setColor(5, 5, WHITE)
            setPiece(6, 5, PAWN); setColor(6, 5, BLACK)
            setPiece(5, 6, BISHOP); setColor(5, 6, WHITE)
            setPiece(5, 7, KING); setColor(5, 7, WHITE)
            setPiece(6, 7, PAWN); setColor(6, 7, WHITE)
            setPiece(7, 7, PAWN); setColor(7, 7, WHITE)
            setPiece(8, 7, PAWN); setColor(8, 7, WHITE)
            whiteKingPosition = Point(5, 7)
            blackKingPosition = Point(2, 2)
            prepareForNextTurn()
        }
        return configuration
    }


