package com.training.colorfulchess.game

import android.graphics.Point
import com.training.colorfulchess.ConfigurationInputStream
import com.training.colorfulchess.game.modelvm2.*
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import kotlin.math.abs

/**
 * Initializes a [GameConfiguration2] object
 * @param white The white pieces, key:position value:piece_code
 * @param black The black pieces, key:position value:piece_code
 * @param firstTurn The player which will make the first move
 * @param beginning True if game begins now, or false if it does not begin.
 */
fun GameConfiguration2.init(
    white: Map<Point, Int>,
    black: Map<Point, Int>, @Player firstTurn: Int,
    beginning: Boolean = true
) {
    var whiteKings = 0
    var blackKings = 0
    for ((pos, piece) in white) {
        setPiece(pos, piece)
        setColor(pos, WHITE)
        if (whitePieces.containsKey(pos))
            throw PositionOverlapException(pos)
        whitePieces[pos] = piece

        if (piece == KING) {
            whiteKings++
            if (whiteKings > 1)
                throw MultipleKingsException(WHITE)
            whiteKingPosition = pos
        }

        if (beginning)
            if (piece == PAWN && pos.y == 0)
                throw PawnReplacementException()
    }
    for ((pos, piece) in black) {
        setPiece(pos, piece)
        setColor(pos, BLACK)
        if (blackPieces.containsKey(pos) || whitePieces.containsKey(pos))
            throw PositionOverlapException(pos)
        blackPieces[pos] = piece

        if (piece == KING) {
            blackKings++
            blackKingPosition = pos
        if (blackKings > 1)
            throw MultipleKingsException(BLACK)
        }

        if (piece == PAWN && pos.y == 8)
            throw PawnReplacementException()

    }
    if (whiteKings == 0) throw KingNotFoundException(WHITE)
    if (blackKings == 0) throw KingNotFoundException(BLACK)
    player = firstTurn

    if (abs(whiteKingPosition.x - blackKingPosition.x) < 1 &&
        abs(whiteKingPosition.y - blackKingPosition.y) < 1
    ) //if kings are adiacent
        throw AdiacentKingsException()

    whiteKingChecks = getKingChecks(whiteKingPosition).toMutableList()
    if (player == PLAYER_2 && whiteKingChecks.isNotEmpty())
        throw CheckException()

    blackKingChecks = getKingChecks(blackKingPosition).toMutableList()
    if (player == PLAYER_1 && blackKingChecks.isNotEmpty())
        throw CheckException()

    whiteKingConstraints = getKingConstraints(whiteKingPosition).toMutableList()
    blackKingConstraints = getKingConstraints(blackKingPosition).toMutableList()

    val state = gameState
    if (state == STALEMATE && beginning)
        throw StalemateException()
    if (state == CHECKMATE && beginning)
        throw CheckmateException()

}

//region Exceptions
open class InitException(msg: String) : Exception(msg)

class AdiacentKingsException(msg: String) : InitException(msg) {
    constructor() : this("Kings are not allowed to be next each other")
}

class PawnReplacementException(msg: String) : InitException(msg) {
    constructor() : this("You can't position a pawn in replacement position at the beginning of the game")
}

class StalemateException(msg: String) : InitException(msg) {
    constructor() : this("You can't position pieces in a stalemate position at the beginning of the game")
}

class CheckException(msg: String) : InitException(msg) {
    constructor() : this("You have to give the first turn to the player threated by check")
}

class CheckmateException(msg: String) : InitException(msg) {
    constructor() : this("The loaded game is in checkmate state thus it cannot be played")
}

open class KingException(msg: String) : InitException(msg) {
    constructor() : this("There must be one and only one white king and one and only one black king")
}

class MultipleKingsException(msg: String, @PieceColor val color: Int) : KingException(msg) {
    constructor(color: Int) : this(formatMultipleKingsMessage(color), color)
}

class KingNotFoundException(msg: String, @PieceColor val color: Int) : KingException(msg) {
    constructor(color: Int) : this(formatKingNotFoundMessage(color), color)
}

class PositionOverlapException(msg: String) : InitException(msg) {
    constructor(position: Point) : this(formatPositionOverloadException(position))
}
//endregion

//region Format Helpers
private fun formatKingNotFoundMessage(color: Int): String =
    when (color) {
        WHITE -> "There is no white king"
        BLACK -> "There is no black king"
        else -> throw ColorException()
    }

private fun formatMultipleKingsMessage(color: Int): String =
    when (color) {
        WHITE -> "There are multiple white kings"
        BLACK -> "There are multiple black kings"
        else -> throw ColorException()
    }

private fun formatPositionOverloadException(position: Point): String =
    "There are multiple pieces at (${position.x},${position.y})"
//endregion

//region Default Configuration
object DefaultConfiguration {
    val whites: List<Pair<Point, Int>> = listOf(
        Point(1, 7) to PAWN,
        Point(2, 7) to PAWN,
        Point(3, 7) to PAWN,
        Point(4, 7) to PAWN,
        Point(5, 7) to PAWN,
        Point(6, 7) to PAWN,
        Point(7, 7) to PAWN,
        Point(8, 7) to PAWN,
        Point(1, 8) to ROOK,
        Point(8, 8) to ROOK,
        Point(2, 8) to KNIGHT,
        Point(7, 8) to KNIGHT,
        Point(3, 8) to BISHOP,
        Point(6, 8) to BISHOP,
        Point(4, 8) to QUEEN,
        Point(5, 8) to KING
    )
    val blacks: List<Pair<Point, Int>> = listOf(
        Point(1, 2) to PAWN,
        Point(2, 2) to PAWN,
        Point(3, 2) to PAWN,
        Point(4, 2) to PAWN,
        Point(5, 2) to PAWN,
        Point(6, 2) to PAWN,
        Point(7, 2) to PAWN,
        Point(8, 2) to PAWN,
        Point(1, 1) to ROOK,
        Point(8, 1) to ROOK,
        Point(2, 1) to KNIGHT,
        Point(7, 1) to KNIGHT,
        Point(3, 1) to BISHOP,
        Point(6, 1) to BISHOP,
        Point(4, 1) to QUEEN,
        Point(5, 1) to KING
    )
    val player = PLAYER_1
}

fun getDefaultConfigurationStream() : ConfigurationInputStream {
    val bytes = arrayListOf<Byte>()
    bytes.add(DefaultConfiguration.whites.size.toByte())
    bytes.add(DefaultConfiguration.blacks.size.toByte())
    bytes.add(DefaultConfiguration.player.toByte())

    for( (pos, piece) in DefaultConfiguration.whites) {
        bytes.add(pos.toIndex().toByte())
        bytes.add(piece.toByte())
    }

    for( (pos, piece) in DefaultConfiguration.blacks) {
        bytes.add(pos.toIndex().toByte())
        bytes.add(piece.toByte())
    }

    return ConfigurationInputStream(ByteArrayInputStream(bytes.toByteArray()))

}
//endregion
