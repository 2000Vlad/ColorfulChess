package com.training.colorfulchess.game.modelvm2

import android.graphics.Point
import androidx.lifecycle.LiveData
import com.training.colorfulchess.game.*
import com.training.colorfulchess.game.SELECTED
import com.training.colorfulchess.game.ENEMY_SELECTED

internal fun ChessViewModel2.select(pos: Point): Transformation {

    return Transformation(
        position = pos.toIndex(),
        newProperties = CellProperties(
            piece = controller.configuration.getPiece(pos),
            color = controller.configuration.getColor(pos),
            background = getSelectedBackground(pos),
            reverse = controller.configuration.getColor(pos) == BLACK
        )

    )
}

internal fun ChessViewModel2.select(positions: Collection<Point>): Collection<Transformation> {
    return positions.map { select(it) }
}

internal fun ChessViewModel2.deselect(pos: Point): Transformation {
    return Transformation(
        position = pos.toIndex(),
        newProperties = CellProperties(
            piece = controller.configuration.getPiece(pos),
            color = controller.configuration.getColor(pos),
            background = DEFAULT,
            reverse = controller.configuration.getColor(pos) == BLACK
        )
    )
}

internal fun ChessViewModel2.move(from: Point, to: Point): Collection<Transformation> {
    val fromTransformation =
        Transformation(
            position = from.toIndex(),
            newProperties = CellProperties(
                piece = NONE,
                color = NONE,
                background = DEFAULT,
                reverse = false
            )
        )
    val toTransformation =
        Transformation(
            position = to.toIndex(),
            newProperties = CellProperties(
                piece = controller.configuration.getPiece(to),
                color = controller.configuration.getColor(to),
                background = DEFAULT,
                reverse = controller.configuration.getColor(to) == BLACK
            )
        )
    return listOf(fromTransformation, toTransformation)
}

internal fun ChessViewModel2.deselect(positions: Collection<Point>?): Collection<Transformation> {
    if (positions.isNullOrEmpty()) return emptyList()
    return positions.map {
        deselect(it)
    }
}

internal fun ChessViewModel2.getSelectedBackground(pos: Point): Int {

    if(controller.isFriendPiece(pos)) {
        return SELECTED
    }

    if(controller.isEnemyPiece(pos)) {
        if(controller.path.value!!.contains(pos))
            return SELECTED
        else return ENEMY_SELECTED
    }

    if(controller.configuration.isEmpty(pos))
        return SELECTED

    throw PlayerException()

}

internal fun GameController2.isFriendPiece(pos: Point) =
    when (configuration.player) {
        PLAYER_1 -> configuration.getColor(pos) == WHITE
        PLAYER_2 -> configuration.getColor(pos) == BLACK
        else -> throw PlayerException()

    }

internal fun GameController2.isEnemyPiece(pos: Point) =
    when (configuration.player) {
        PLAYER_1 -> configuration.getColor(pos) == BLACK
        PLAYER_2 -> configuration.getColor(pos) == WHITE
        else -> throw PlayerException()

    }

internal fun ChessViewModel2.clean(
    pos: Point?,
    path: Collection<Point>?
): Collection<Transformation> {
    val toBeCleaned = mutableListOf<Transformation>()
    if (pos != null)
        toBeCleaned.add(deselect(pos))
    if (!path.isNullOrEmpty())
        toBeCleaned.addAll(deselect(path))

    return toBeCleaned
}

internal fun clean(pos: Point?, path: Collection<Point>?): Collection<Point> {
    val toBeCleaned = mutableListOf<Point>()
    if (pos != null)
        toBeCleaned.add(pos)
    if (!path.isNullOrEmpty())
        toBeCleaned.addAll(path)

    return toBeCleaned
}




