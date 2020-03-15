package com.training.colorfulchess.game.modelvm2

import android.graphics.Point
import androidx.annotation.IntDef
import com.training.colorfulchess.ConfigurationInputStream
import com.training.colorfulchess.ConfigurationOutputStream
import com.training.colorfulchess.game.*
import com.training.colorfulchess.game.check.Check
import com.training.colorfulchess.game.check.DiagonalCheck
import com.training.colorfulchess.game.check.DirectCheck
import com.training.colorfulchess.game.check.LinearCheck
import com.training.colorfulchess.game.constraint.Constraint
import com.training.colorfulchess.game.constraint.DiagonalConstraint
import com.training.colorfulchess.game.constraint.LinearConstraint
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import javax.inject.Inject
import kotlin.math.abs

/**
 * This class encapsulate all the chess-specific functionalities that we need in order to
 * implement chess rules
 */
class GameConfiguration2 @Inject constructor() {


    var table = arrayOfNulls<GameCell>(64).apply {
        for (i in 0..63) {
            this[i] = GameCell(
                NONE,
                NONE
            )
        }

    }

    @Player
    var player: Int = 0

    var whitePieces: MutableMap<Point, Int> = mutableMapOf()
    //private set in use version

    var blackPieces: MutableMap<Point, Int> = mutableMapOf()
    //private set in use version

    var whiteKingPosition: Point = Point(0, 0)
    //private set in use version

    var blackKingPosition: Point = Point(0, 0)
    //private set in use version

    var whiteKingChecks: MutableList<Check> = mutableListOf()

    var blackKingChecks: MutableList<Check> = mutableListOf()

    var whiteKingConstraints: MutableList<Constraint> = mutableListOf()

    var blackKingConstraints: MutableList<Constraint> = mutableListOf()

    var whiteKingPositions: List<Point> = emptyList()

    var blackKingPositions: List<Point> = emptyList()

    fun move(from: Point, to: Point) {

        when (getColor(to)) {
            BLACK -> blackPieces.remove(to)
            WHITE -> whitePieces.remove(to)
        }
        when (getColor(from)) {
            WHITE -> {
                whitePieces.remove(from)
                whitePieces[to] = getPiece(from)
                setColor(to, WHITE)
                setPiece(to, getPiece(from) )
            }
            BLACK -> {
                blackPieces.remove(from)
                blackPieces[to] = getPiece(from)
                setColor(to, BLACK)
                setPiece(to, getPiece(from) )
            }
            else -> throw ColorException()
        }

        setPiece(from, NONE)
        setColor(from, NONE)

        if (getPiece(to) == KING)
            when (getColor(to)) {
                WHITE -> whiteKingPosition = to
                BLACK -> blackKingPosition = to
                else -> throw ColorException()
            }

        player = when (player) {
            PLAYER_1 -> PLAYER_2
            PLAYER_2 -> PLAYER_1
            else -> throw PlayerException()
        }

        when (player) {
            PLAYER_1 -> {
                whiteKingPositions = getKingPositions(whiteKingPosition).toList()
                whiteKingConstraints = getKingConstraints(whiteKingPosition).toMutableList()
                whiteKingChecks = getKingChecks(whiteKingPosition).toMutableList()
            }
            PLAYER_2 -> {
                blackKingPositions = getKingPositions(blackKingPosition).toList()
                blackKingConstraints = getKingConstraints(blackKingPosition).toMutableList()
                blackKingChecks = getKingChecks(blackKingPosition).toMutableList()
            }
            else -> throw PlayerException()
        }
    }

    fun move2(from: Point, to: Point) {
        val fromColor = getColor(from)
        val fromPiece = getPiece(from)
        val toColor = getColor(to)
        val toPiece = getPiece(to)

        when(fromColor) {
            BLACK -> blackPieces.remove(from)
            WHITE -> whitePieces.remove(from)
            else -> throw ColorException()
        }

        when(toColor) {
            BLACK -> fromColor //
        }



    }

    fun serialize(stream: ConfigurationOutputStream) {
        stream.writeHeader(whitePieces.size, blackPieces.size, player)

        for (entry in whitePieces) {
            stream.writeSlot(entry.key, entry.value)
        }

        for (entry in blackPieces) {
            stream.writeSlot(entry.key, entry.value)
        }

    }

    fun deserialize(stream: ConfigurationInputStream, beginning: Boolean) {
        val header = stream.readHeader()
        val wcount = header.whites
        val bcount = header.blacks
        val player = header.player
        val whites = mutableMapOf<Point, Int>()
        val blacks = mutableMapOf<Point, Int>()


        for (i in 1..wcount) {
            val slot = stream.readSlot()
            val pos = slot.pos
            val piece = slot.piece
            whites[pos] = piece
        }
        for (i in 1..bcount) {
            val slot = stream.readSlot()
            val pos = slot.pos
            val piece = slot.piece
            blacks[pos] = piece
        }
        init(whites, blacks, player, beginning)
    }

    interface GameConfigurationListener {
        fun onStateChanged(state: Int)
    }

    /**
     * Puts a piece in a specified position
     */
    fun put(pos: Point, @Piece piece: Int, @PieceColor color: Int) {
        setPiece(pos, piece)
        setColor(pos, color)

        if (piece == KING) throw KingException()

        whiteKingChecks = getKingChecks(whiteKingPosition).toMutableList()
        blackKingChecks = getKingChecks(blackKingPosition).toMutableList()

        whiteKingConstraints = getKingConstraints(whiteKingPosition).toMutableList()
        blackKingConstraints = getKingConstraints(blackKingPosition).toMutableList()


    }

    fun switchTurn() {
        player = when (player) {
            PLAYER_1 -> PLAYER_2
            PLAYER_2 -> PLAYER_1
            else -> throw PlayerException()
        }
    }
}

//region Primary functions
/**
 * Gets all the available positions where a piece can move in accordance to actual chess rules
 * @param piece the respective piece
 */
fun GameConfiguration2.getAvailablePositions(piece: Point): Sequence<Point> {

    var positions: Sequence<Point> = when (getPiece(piece)) {
        KING -> return when (getColor(piece)) {
            WHITE -> whiteKingPositions.asSequence()
            BLACK -> blackKingPositions.asSequence()
            else -> throw ColorException()
        }
        PAWN, BISHOP, QUEEN, KNIGHT, ROOK -> getAbsolutePositions(piece)
        else -> throw PieceException()
    }
    val checks: List<Check>
    val constraints: List<Constraint>
    when (player) {
        PLAYER_1 -> {
            checks = whiteKingChecks
            constraints = whiteKingConstraints
        }
        PLAYER_2 -> {
            checks = blackKingChecks
            constraints = blackKingConstraints
        }
        else -> throw PlayerException()
    }

    if (checks.size > 1)
        return emptySequence()

    for (check in checks)
        positions = check.filter(positions)

    for (constraint in constraints)
        if (constraint.friend == piece)
            positions = constraint.filter(positions)

    return positions
}

/**
 * Gets the absolute positions for a piece
 * @param piece the respective piece
 */
private fun GameConfiguration2.getAbsolutePositions(piece: Point): Sequence<Point> {
    return when (getPiece(piece)) {
        PAWN -> getPawnAbsolutePositions(piece)
        BISHOP -> getBishopAbsolutePositions(piece)
        KNIGHT -> getKnightAbsolutePositions(piece)
        ROOK -> getRookAbsolutePositions(piece)
        QUEEN -> getQueenAbsolutePositions(piece)
        KING -> getKingAbsolutePositions(piece)
        else -> throw PieceException()
    }
}

/**
 *Gets [constraints][Constraint] for a King
 *@param king The position of the king you want to get constraints for
 *@return The constraints for specified King
 *
 */
internal fun GameConfiguration2.getKingConstraints(king: Point): List<Constraint> {

    val result = mutableListOf<Constraint>()
    val linearDirections =
        listOf<Point.() -> Point>(
            Point::forward, Point::right, Point::backward, Point::left
        )
    val diagonalDirections = listOf<Point.() -> Point>(
        Point::forwardLeft,
        Point::forwardRight,
        Point::backwardRight,
        Point::backwardLeft
    )

    for (direction in linearDirections) {
        val piece = hasPieceInDirection(king, direction)
        if (piece != Point(0, 0) && isFriend(piece, getColor(king))) {
            val threat = hasPieceInDirection(piece, direction)
            if (threat != Point(0, 0) && isLinearThreat(threat, getColor(king)))
                result.add(LinearConstraint(king, piece))
        }
    }

    for (direction in diagonalDirections) {
        val piece = hasPieceInDirection(king, direction)
        if (piece != Point(0, 0) && isFriend(piece, getColor(king))) {
            val threat = hasPieceInDirection(piece, direction)
            if (threat != Point(0, 0) && isDiagonalThreat(threat, getColor(king)))
                result.add(DiagonalConstraint(king, piece))
        }
    }

    return result.toList()
}


/**
 * Gets [checks][Check] for a King
 * @param king The king you want to get checks for
 * @return The checks for specified king
 */
internal fun GameConfiguration2.getKingChecks(king: Point): List<Check> {

    val result = mutableListOf<Check>()
    val count =
        when (getColor(king)) {
            WHITE -> 1
            BLACK -> -1
            else -> throw ColorException()
        }

    if (king.forwardLeft(count).inBounds() && getPiece(king.forwardLeft(count)) == PAWN && isEnemy(
            king.forwardLeft(count),
            getColor(king)
        )
    )
        result.add(DirectCheck(king, king.forwardLeft(count)))

    if (king.forwardRight(count).inBounds() && getPiece(king.forwardRight(count)) == PAWN && isEnemy(
            king.forwardRight(count),
            getColor(king)
        )
    )
        result.add(DirectCheck(king, king.forwardRight(count)))

    val knightPath = getKnightAbsolutePositions(king).toList()
    for (pos in knightPath)
        if (getPiece(pos) == KNIGHT && isEnemy(pos, getColor(king)))
            result.add(DirectCheck(king, pos))

    val linearDirections: List<Point.() -> Point> =
        listOf(Point::forward, Point::right, Point::backward, Point::left)

    val diagonalDirections: List<Point.() -> Point> =
        listOf(Point::forwardRight, Point::backwardRight, Point::backwardLeft, Point::forwardLeft)

    for (direction in linearDirections) {
        val piece = hasPieceInDirection(king, direction)
        if (piece != Point(0, 0))
            if (isLinearThreat(piece, getColor(king)))
                result.add(LinearCheck(king, piece))
    }

    for (direction in diagonalDirections) {
        val piece = hasPieceInDirection(king, direction)
        if (piece != Point(0, 0))
            if (isDiagonalThreat(piece, getColor(king)))
                result.add(DiagonalCheck(king, piece))
    }


    return result.toList()
}

/**
 * @return The king positions taking [Checks][Check] into account
 */
internal fun GameConfiguration2.getKingPositions(king: Point): Sequence<Point> {
    val kingColor = getColor(king)
    val positions = getKingAbsolutePositions(king)

    val linearDirecttions =
        listOf<Point.() -> Point>(Point::forward, Point::backward, Point::left, Point::right)
    val diagonalDirections = listOf<Point.() -> Point>(
        Point::forwardLeft,
        Point::backwardRight,
        Point::backwardLeft,
        Point::forwardRight
    )
    setColor(king, NONE)
    setPiece(king, NONE)
    val result = positions.filter {



        if(isFriend(it, kingColor))
            return@filter false

        for (direction in linearDirecttions) {
            val piece = hasPieceInDirection(it, direction)
            if (piece != Point(0, 0) && isEnemy(piece, kingColor) && isLinearThreat(
                    piece,
                    kingColor
                )
            )
                return@filter false
        }
        for (direction in diagonalDirections) {
            val piece = hasPieceInDirection(it, direction)
            if (piece != Point(0, 0) && isEnemy(piece, kingColor) && isDiagonalThreat(
                    piece,
                    kingColor
                )
            )
                return@filter false
        }

        val knightPath = getKnightAbsolutePositions(it, true)
        for (pos in knightPath.toList()) {
            if (getPiece(pos) == KNIGHT && isEnemy(pos, kingColor))
                return@filter false
        }

        val count =
            when (kingColor) {
                WHITE -> 1
                BLACK -> -1
                else -> throw ColorException()
            }
        val fl = it.forwardLeft(count)
        val fr = it.forwardRight(count)

        if (fl.inBounds() && getPiece(fl) == PAWN && isEnemy(fl, kingColor)) return@filter false
        if (fr.inBounds() && getPiece(fr) == PAWN && isEnemy(fr, kingColor)) return@filter false
        return@filter true
    }.toList().asSequence() //This line enforces immediate computation because then the position of the king will be restored and
                            //and the algorithm would not work

    setColor(king, kingColor)
    setPiece(king, KING)
    return result


}
//endregion

//region Absolute positions
/**
 * @return The path of a specified rook without taking into account [Checks][Check] or [Constraints][Constraint]
 */
private fun GameConfiguration2.getRookAbsolutePositions(rook: Point) =
    getPathInDirection(
        getColor(rook),
        rook,
        Point::forward
    ) + getPathInDirection(
        getColor(rook),
        rook,
        Point::right
    ) + getPathInDirection(
        getColor(rook),
        rook,
        Point::left
    ) + getPathInDirection(
        getColor(rook),
        rook,
        Point::backward
    )

/**
 * @return The path of a specified bishop without taking into account [Checks][Check] or [Constraints][Constraint]
 */
private fun GameConfiguration2.getBishopAbsolutePositions(bishop: Point) =
    getPathInDirection(
        getColor(bishop),
        bishop,
        Point::forwardLeft
    ) + getPathInDirection(
        getColor(bishop),
        bishop,
        Point::forwardRight
    ) + getPathInDirection(
        getColor(bishop),
        bishop,
        Point::backwardLeft
    ) + getPathInDirection(
        getColor(bishop),
        bishop,
        Point::backwardRight
    )

/**
 * @return The path of a specified queen without taking into account [Checks][Check] or [Constraints][Constraint]
 */
private fun GameConfiguration2.getQueenAbsolutePositions(queen: Point) =
    getPathInDirection(
        getColor(queen),
        queen,
        Point::forward
    ) + getPathInDirection(
        getColor(queen),
        queen,
        Point::forwardRight
    ) + getPathInDirection(
        getColor(queen),
        queen,
        Point::right
    ) + getPathInDirection(
        getColor(queen),
        queen,
        Point::backwardRight
    ) + getPathInDirection(
        getColor(queen),
        queen,
        Point::backward
    ) + getPathInDirection(
        getColor(queen),
        queen,
        Point::backwardLeft
    ) + getPathInDirection(
        getColor(queen),
        queen,
        Point::left
    ) + getPathInDirection(
        getColor(queen),
        queen,
        Point::forwardLeft
    )

/**
 * @return The path of a specified knight without taking into account [Checks][Check] or [Constraints][Constraint]
 */
/*private fun GameConfiguration2.getKnightAbsolutePositions(knight: Point): Sequence<Point> {

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
            val pass = it.inBounds() && (isEnemyOrEmpty(it, getColor(knight)))
            pass
        }
}*/

private fun GameConfiguration2.getKnightAbsolutePositions(knight: Point, includeFriends: Boolean = false): Sequence<Point> {
    val totalPath = sequenceOf(
        knight.forward(2).left(),
        knight.forward(2).right(),
        knight.right(2).forward(),
        knight.right(2).backward(),
        knight.backward(2).left(),
        knight.backward(2).right(),
        knight.left(2).forward(),
        knight.left(2).backward()
    )
    return totalPath.filter {
        return@filter it.inBounds() && (includeFriends || isEnemyOrEmpty(it, getColor(knight)))
    }
}

/**
 * @return The path of a specified pawn without taking into account [Checks][Check] or [Constraints][Constraint]
 */
private fun GameConfiguration2.getPawnAbsolutePositions2(pawn: Point): Sequence<Point> {
    var path = emptySequence<Point>()
    var count = 0
    var pawnStart = 0
    count = if (getColor(pawn) == WHITE) 1
    else -1
    pawnStart = if (getColor(pawn) == WHITE) 7
    else 2

    if (pawn.forward(count).inBounds() && isEmpty(pawn.forward(count))) {
        path += pawn.forward(count)
        if (pawn.y == pawnStart && pawn.forward(count).forward(count).inBounds() && isEmpty(
                pawn.forward(
                    count
                ).forward(count)
            )
        )
            path += pawn.forward(count).forward(count)
    }
    if (pawn.forwardLeft(count).inBounds() && isEnemy(pawn.forwardLeft(count), getColor(pawn)))
        path += pawn.forwardLeft()

    if (pawn.forwardRight(count).inBounds() && isEnemy(pawn.forwardRight(count), getColor(pawn)))
        path += pawn.forwardRight(count)

    return path

}

private fun GameConfiguration2.getPawnAbsolutePositions(pawn: Point): Sequence<Point> {

    var result = emptySequence<Point>()
    var dir: Point.(Int) -> Point =
        if (getColor(pawn) == WHITE)
            Point::forward
        else
            Point::backward

    val start =
        if (getColor(pawn) == WHITE)
            7
        else
            2


    val first = pawn.dir(1)
    val second = pawn.dir(2)

    if (first.inBounds())
        if (isEmpty(first)) {
            result += first

            if (pawn.y == start)
                if (isEmpty(second))
                    result += second
        }
    val diag1: Point.(Int) -> Point =
        if (getColor(pawn) == WHITE)
            Point::forwardLeft
        else Point::backwardLeft
    val diag2: Point.(Int) -> Point =
        if (getColor(pawn) == WHITE)
            Point::forwardRight
        else Point::backwardRight

    val d1 = pawn.diag1(1)
    val d2 = pawn.diag2(1)

    if(d1.inBounds())
        if(isEnemy(d1, getColor(pawn)))
            result += d1
    if(d2.inBounds())
        if(isEnemy(d2, getColor(pawn)))
            result += d2

    return result
}

/**
 * @return The path of a specified pawn without taking into account [Checks][Check]
 */
private fun GameConfiguration2.getKingAbsolutePositions(king: Point): Sequence<Point> {
    val seq: Sequence<Point> = sequenceOf(
        king.forward(),
        king.forwardRight(),
        king.right(),
        king.backwardRight(),
        king.backward(),
        king.backwardLeft(),
        king.left(),
        king.forwardLeft()
    )
    return seq.filter {
        it.inBounds() && isEnemyOrEmpty(it, getColor(king))
    }.asSequence()


}
//endregion

//region Path helpers
/**
 * @param start The point to start from
 * @param next The direction which you want to get path in
 * @param friendColor The supposed friendly color (which is used to detect
 * @return The path starting from [start] in direction [next]
 * until it's reaching the edge of the table or a piece
 * with of color enemy to [friendColor]
 *
 */
private fun GameConfiguration2.getPathInDirection(
    @PieceColor friendColor: Int,
    start: Point,
    next: Point.() -> Point
): Sequence<Point> {

    val seed = if (start.next().inBounds() && isEnemyOrEmpty(
            start.next(),
            friendColor
        )
    ) start.next() else null
    val seq = generateSequence(seed) {
        return@generateSequence when {
            !it.next().inBounds() -> null
            !isEmpty(it) -> null
            isEnemyOrEmpty(it.next(), friendColor) -> it.next()
            else -> null
        }
    }

    return seq
}

/**
 * Detects if a specified piece has a piece in a specified direction
 * @param piece
 * @param direction
 * @return Returns the one-based coordinates of the encountered piece
 * If there was not detected any piece a [Point] (0,0) will be returned
 */
private fun GameConfiguration2.hasPieceInDirection(
    piece: Point,
    direction: Point.() -> Point
): Point {

    var currentPoint = piece.copy().direction()

    while (currentPoint.inBounds() && isEmpty(currentPoint))
        currentPoint = currentPoint.direction()

    if (!currentPoint.inBounds()) return Point(0, 0)

    return currentPoint

}
//endregion

//region Table helpers
@Piece
fun GameConfiguration2.getPiece(pos: Point): Int = table[pos.toIndex()]!!.piece

@PieceColor
fun GameConfiguration2.getColor(pos: Point): Int = table[pos.toIndex()]!!.color

fun GameConfiguration2.isEmpty(pos: Point) = table[pos.toIndex()]!!.piece == NONE

fun GameConfiguration2.isEnemyOrEmpty(pos: Point, @PieceColor friendColor: Int) =
    getPiece(pos) == NONE || (getColor(pos) != friendColor)

fun GameConfiguration2.isEnemy(pos: Point, @PieceColor friendColor: Int) =
    getColor(pos) != friendColor && getColor(pos) != NONE

fun GameConfiguration2.isFriend(pos: Point, @PieceColor friendColor: Int) =
    getColor(pos) == friendColor

fun GameConfiguration2.setPiece(pos: Point, piece: Int) {
    table[pos.toIndex()]!!.piece = piece
}

fun GameConfiguration2.setColor(pos: Point, color: Int) {
    table[pos.toIndex()]!!.color = color
}

fun GameConfiguration2.setPiece(x: Int, y: Int, @Piece piece: Int) {
    setPiece(Point(x, y), piece)
}

fun GameConfiguration2.setColor(x: Int, y: Int, @PieceColor color: Int) {
    setColor(Point(x, y), color)
}

/**
 * Detects if a piece is a diagonal threat (Queen or Bishop) for another piece
 * @param piece The loction you want to check if it is a diagonal threat
 * @param friendColor This is a friendly color which will be compared against the color of parameter piece
 * ```
 *  if(getColor(piece) != friendColor)
 *      if(getPiece(piece) == BISHOP || getPiece(piece) == QUEEN)
 *          return true
 *      else return false
 *  else return false
 * ```
 */
fun GameConfiguration2.isDiagonalThreat(piece: Point, @PieceColor friendColor: Int): Boolean {
    return if (getColor(piece) != friendColor)
        getPiece(piece) == BISHOP || getPiece(piece) == QUEEN
    else false
}

/**
 * Detects if a piece is a linear threat (Queen or Rook) for another piece
 * @param piece The loction you want to check if it is a linear threat
 * @param friendColor This is a friendly color which will be compared against the color of parameter piece
 * ```
 *  if(getColor(piece) != friendColor)
 *      if(getPiece(piece) == ROOK || getPiece(piece) == QUEEN)
 *          return true
 *      else return false
 *  else return false
 * ```
 */
fun GameConfiguration2.isLinearThreat(piece: Point, @PieceColor friendColor: Int): Boolean {
    return if (getColor(piece) != friendColor)
        getPiece(piece) == ROOK || getPiece(piece) == QUEEN
    else false

}
//endregion

//region Point Directions
fun Point.forward(count: Int): Point = Point(this.x, this.y - count)

fun Point.backward(count: Int): Point = Point(this.x, this.y + count)

fun Point.left(count: Int): Point = Point(this.x - count, this.y)

fun Point.right(count: Int): Point = Point(this.x + count, this.y)

fun Point.forwardLeft(count: Int): Point = Point(this.x - count, this.y - count)

fun Point.forwardRight(count: Int): Point = Point(this.x + count, this.y - count)

fun Point.backwardLeft(count: Int): Point = Point(this.x - count, this.y + count)

fun Point.backwardRight(count: Int): Point = Point(this.x + count, this.y + count)
//endregion

//region Game state helpers
@GameState
val GameConfiguration2.gameState: Int
    get() {

        val kingPosition: Point
        val checks: MutableList<Check>
        val kingPath: List<Point>
        val pieces: Map<Point, Int>
        when (player) {
            PLAYER_1 -> {
                kingPosition = whiteKingPosition
                checks = whiteKingChecks
                kingPath = getKingPositions(kingPosition).toList()
                pieces = whitePieces
            }
            PLAYER_2 -> {
                kingPosition = blackKingPosition
                checks = blackKingChecks
                kingPath = getKingPositions(kingPosition).toList()
                pieces = blackPieces
            }
            else -> throw PlayerException()
        }

        if (kingPath.isEmpty()) {
            if (checks.isNotEmpty()) {
                if (checks.size > 1)
                    return CHECKMATE

                if (filtersCheck(pieces, checks[0]))
                    return NONE
                else return CHECKMATE
            }
            if (isCoverageEmpty(pieces))
                return STALEMATE
        }

        return NONE
    }

/**
 * Detects if there is a piece that can cancel a check
 * This method is usually used when you want to verify if there is a friendly piece of a king that
 * can cancel a check
 * @param pieces The set of pieces to verify (usually the friendly pieces of a king)
 * @param check The check to match the pieces against
 */
fun GameConfiguration2.filtersCheck(pieces: Map<Point, Int>, check: Check): Boolean {
    for (piece in pieces.keys) {
        if (getPiece(piece) == KING) continue
        val path = getAvailablePositions(piece)
        val p: Point?
        p = check.filter(path).firstOrNull()
        if (p != null)
            return true
    }
    return false
}

/**
 * Detects if the total [available][GameConfiguration2.getAvailablePositions] coverage is empty or not
 * @param pieces The collection of pieces to check coverage for
 */
fun GameConfiguration2.isCoverageEmpty(pieces: Map<Point, Int>): Boolean {

    for (piece in pieces.keys) {
        val path = getAvailablePositions(piece)
        if (path.firstOrNull() != null) return false
    }
    return true
}

/*private fun GameConfiguration2.isInCheckMate(king: Point) : Boolean {
    val path = getKingPositions(king).toList()
    val (checks, pieces) = when(getColor(king)) {
        BLACK -> Pair(blackKingChecks, blackPieces)
        WHITE -> Pair(whiteKingChecks, whitePieces)
        else -> throw ColorException()
    }

    if(path.isEmpty())
        if(checks.size > 1)
            return true
    return false
}*/
//endregion

//region Constants
const val CHECKMATE = 9
const val STALEMATE = 10
//endregion

//region Annotations





//endregion