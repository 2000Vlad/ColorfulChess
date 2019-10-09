package com.training.colorfulchess

import android.graphics.Point
import com.training.colorfulchess.game.*
import com.training.colorfulchess.game.check.HorizontalCheck
import com.training.colorfulchess.game.check.MainDiagonalCheck
import com.training.colorfulchess.game.check.VerticalCheck
import com.training.colorfulchess.game.constraint.HorizontalConstraint
import com.training.colorfulchess.game.constraint.MainDiagonalConstraint
import com.training.colorfulchess.game.constraint.SecondDiagonalConstraint

fun hasThreat_Works_Correct_Case1(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val knight = Point(4, 5)
    val pawn = Point(6, 6)
    configuration.setPiece(knight, KNIGHT); configuration.setColor(knight, BLACK)
    configuration.setPiece(pawn, PAWN); configuration.setColor(pawn, WHITE)
    return pawn
}

fun hasThreat_Works_Correct_Case2(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val knight = Point(4, 5)
    val pawn = Point(6, 6)
    configuration.setPiece(knight, KNIGHT); configuration.setColor(knight, WHITE)
    configuration.setPiece(pawn, PAWN); configuration.setColor(pawn, WHITE)
    return pawn
}

fun hasThreat_Works_Correct_Case3(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    return Point(0, 0)
}

fun king_Path_Works_Correct_Case1(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val rook1 = Point(1, 4)
    val rook2 = Point(7, 8)
    val queen = Point(4, 5)
    val pawn = Point(7, 2)
    val king = Point(1, 8)

    configuration.apply {
        setPiece(rook1, ROOK); setColor(rook1, BLACK)
        setPiece(rook2, ROOK); setColor(rook2, BLACK)
        setPiece(pawn, PAWN); setColor(pawn, BLACK)
        setPiece(queen, QUEEN); setColor(queen, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
    }

    return king
}

fun king_Path_Works_Correct_Case2(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val queen = Point(3, 2)
    val pawn = Point(6, 2)
    val knight = Point(7, 2)
    val king = Point(5, 4)
    configuration.run {
        setPiece(queen, QUEEN); setColor(queen, BLACK)
        setPiece(pawn, PAWN); setColor(pawn, BLACK)
        setPiece(knight, KNIGHT); setColor(knight, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
        return king
    }
}

fun king_Path_Works_Correct_Case3(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val king = Point(4, 5)
    val pawn1 = Point(2, 5)
    val pawn2 = Point(6, 5)
    val knight = Point(2, 2)
    val queen = Point(7, 1)
    val rook = Point(7, 4)
    return configuration.run {
        setPiece(king, KING); setColor(king, WHITE)
        setPiece(pawn1, PAWN); setColor(pawn1, BLACK)
        setPiece(pawn2, PAWN); setColor(pawn2, BLACK)
        setPiece(knight, KNIGHT); setColor(knight, BLACK)
        setPiece(queen, QUEEN); setColor(queen, BLACK)
        setPiece(rook, ROOK); setColor(queen, BLACK)
        king
    }
}

fun getHorizontalConstraints_Works_Correct_Case1(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)

    val king = Point(5, 5)
    val pawn = Point(6, 5)
    val rook = Point(8, 5)

    configuration.run {
        setPiece(king, KING); setColor(king, WHITE)
        setPiece(pawn, PAWN); setColor(pawn, WHITE)
        setPiece(rook, ROOK); setColor(rook, BLACK)
        return king
    }

}

fun getHorizontalConstraints_Works_Correct_Case2(configuration: GameConfiguration): Point {
    return Point()
}

fun getHorizontalConstraints_Works_Correct_Case3(configuration: GameConfiguration): Point {
    return Point()
}

fun getVerticalConstraints_Works_Correct_Case1(configuration: GameConfiguration): Point {
    return Point()
}

fun getVerticalConstraints_Works_Correct_Case2(configuration: GameConfiguration): Point {
    return Point()
}

fun getVerticalConstraints_Works_Correct_Case3(configuration: GameConfiguration): Point {
    return Point()
}

fun getConstraints_Works_Correct_Case1(configuration: GameConfiguration): Point {
    val knight = Point(5, 2)
    val queen1 = Point(7, 3)
    val pawn1 = Point(5, 4)
    val pawn2 = Point(4, 5)
    val pawn3 = Point(6, 5)
    val pawn4 = Point(6, 6)
    val rook = Point(7, 5)
    val queen2 = Point(2, 5)
    val queen3 = Point(7, 7)
    val king = Point(5, 5)

    return configuration.run {
        setPiece(knight, KNIGHT); setColor(knight, BLACK)
        setPiece(queen1, QUEEN); setColor(queen1, BLACK)
        setPiece(pawn1, PAWN); setColor(pawn1, WHITE)
        setPiece(pawn2, PAWN); setColor(pawn2, WHITE)
        setPiece(pawn3, PAWN); setColor(pawn3, WHITE)
        setPiece(pawn4, PAWN); setColor(pawn4, WHITE)
        setPiece(rook, ROOK); setColor(rook, BLACK)
        setPiece(queen2, QUEEN); setColor(queen2, BLACK)
        setPiece(queen3, QUEEN); setColor(queen3, BLACK)
        setPiece(king, KING); setColor(king, WHITE)

        king
    }
}

fun getConstraints_Works_Correct_Case2(configuration: GameConfiguration): Point {
    val knight = Point(5, 2)
    val queen1 = Point(7, 3)
    val pawn1 = Point(5, 4)
    val pawn2 = Point(4, 5)
    val pawn3 = Point(6, 5)
    val pawn4 = Point(6, 6)
    val rook = Point(7, 5)
    val queen2 = Point(2, 5)
    val queen3 = Point(7, 7)
    val king = Point(5, 5)
    val bishop = Point(2, 2)
    val rook2 = Point(4, 4)

    return configuration.run {
        setPiece(knight, KNIGHT); setColor(knight, BLACK)
        setPiece(queen1, QUEEN); setColor(queen1, BLACK)
        setPiece(pawn1, PAWN); setColor(pawn1, WHITE)
        setPiece(pawn2, PAWN); setColor(pawn2, WHITE)
        setPiece(pawn3, PAWN); setColor(pawn3, WHITE)
        setPiece(pawn4, PAWN); setColor(pawn4, WHITE)
        setPiece(rook, ROOK); setColor(rook, BLACK)
        setPiece(queen2, QUEEN); setColor(queen2, BLACK)
        setPiece(queen3, QUEEN); setColor(queen3, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
        setPiece(bishop, BISHOP); setColor(bishop, BLACK)
        setPiece(rook2, ROOK); setColor(rook2, WHITE)

        king
    }
}

fun getConstraints_Works_Correct_Case3(configuration: GameConfiguration): Point {
    val knight = Point(5, 2)
    val queen1 = Point(7, 3)
    val pawn1 = Point(5, 4)
    val pawn2 = Point(4, 5)
    val pawn3 = Point(6, 5)
    val pawn4 = Point(6, 6)
    val pawn5 = Point(3, 7)
    val rook = Point(7, 5)
    val queen2 = Point(2, 5)
    val queen3 = Point(7, 7)
    val queen4 = Point(2, 8)
    val king = Point(5, 5)
    val bishop = Point(2, 2)
    val rook2 = Point(4, 4)

    return configuration.run {
        setPiece(knight, KNIGHT); setColor(knight, BLACK)
        setPiece(queen1, QUEEN); setColor(queen1, BLACK)
        setPiece(pawn1, PAWN); setColor(pawn1, WHITE)
        setPiece(pawn2, PAWN); setColor(pawn2, WHITE)
        setPiece(pawn3, PAWN); setColor(pawn3, WHITE)
        setPiece(pawn4, PAWN); setColor(pawn4, WHITE)
        setPiece(rook, ROOK); setColor(rook, BLACK)
        setPiece(queen2, QUEEN); setColor(queen2, BLACK)
        setPiece(queen3, QUEEN); setColor(queen3, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
        setPiece(bishop, BISHOP); setColor(bishop, BLACK)
        setPiece(rook2, ROOK); setColor(rook2, WHITE)
        setPiece(pawn5, PAWN); setColor(pawn5, WHITE)
        setPiece(queen4, QUEEN); setColor(queen4, BLACK)
        king
    }
}

fun getChecks_Works_Correct_Case0(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val king = Point(4, 4)
    configuration.run {
        setPiece(king, KING); setColor(king, WHITE)
        return king
    }
}

fun getChecks_Works_Correct_Case1(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val king = Point(4, 4)
    val queen1 = Point(2, 2)
    val queen2 = Point(2, 4)
    val queen3 = Point(6, 2)
    val queen4 = Point(4, 6)
    val rook1 = Point(6, 4)
    val rook2 = Point(4, 2)
    val bishop1 = Point(6, 6)
    val bishop2 = Point(2, 6)
    configuration.run {
        setPiece(king, KING); setColor(king, BLACK)

        setPiece(queen1, QUEEN); setColor(queen1, WHITE)

        setPiece(queen2, QUEEN); setColor(queen2, WHITE)

        setPiece(queen3, QUEEN); setColor(queen3, WHITE)

        setPiece(queen4, QUEEN); setColor(queen4, WHITE)

        setPiece(bishop1, BISHOP); setColor(bishop1, WHITE)

        setPiece(bishop2, BISHOP); setColor(bishop2, WHITE)

        setPiece(rook1, ROOK); setColor(rook1, WHITE)

        setPiece(rook2, ROOK); setColor(rook2, WHITE)


        return king
    }
}

fun getChecks_Works_Correct_Case2(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val king = Point(4, 4)
    val queen1 = Point(2, 2)
    val queen2 = Point(2, 4)
    val queen3 = Point(6, 2)
    val queen4 = Point(4, 6)
    val rook1 = Point(6, 4)
    val rook2 = Point(4, 2)
    val bishop1 = Point(6, 6)
    val bishop2 = Point(2, 6)
    val knight1 = Point(3, 2)
    val knight2 = Point(5, 2)
    val knight3 = Point(6, 3)
    val knight4 = Point(6, 5)
    val knight5 = Point(5, 6)
    val knight6 = Point(3, 6)
    val knight7 = Point(2, 5)
    val knight8 = Point(2, 3)
    configuration.run {

        setPiece(king, KING); setColor(king, WHITE)

        setPiece(queen1, QUEEN); setColor(queen1, BLACK)

        setPiece(queen2, QUEEN); setColor(queen2, BLACK)

        setPiece(queen3, QUEEN); setColor(queen3, BLACK)

        setPiece(queen4, QUEEN); setColor(queen4, BLACK)

        setPiece(bishop1, BISHOP); setColor(bishop1, BLACK)

        setPiece(bishop2, BISHOP); setColor(bishop2, BLACK)

        setPiece(rook1, ROOK); setColor(rook1, BLACK)

        setPiece(rook2, ROOK); setColor(rook2, BLACK)

        setPiece(knight1, KNIGHT); setColor(knight1, BLACK)

        setPiece(knight2, KNIGHT); setColor(knight2, BLACK)

        setPiece(knight3, KNIGHT); setColor(knight3, BLACK)

        setPiece(knight4, KNIGHT); setColor(knight4, BLACK)

        setPiece(knight5, KNIGHT); setColor(knight5, BLACK)

        setPiece(knight6, KNIGHT); setColor(knight6, BLACK)

        setPiece(knight7, KNIGHT); setColor(knight7, BLACK)

        setPiece(knight8, KNIGHT); setColor(knight8, BLACK)


        return king
    }
}

fun getAvailablePositions_Pawn_Case1_0(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val pawn = Point(4, 7)
    val queen = Point(4, 5)
    val bishop = Point(5, 6)
    configuration.apply {
        setPiece(pawn, PAWN); setColor(pawn, WHITE)
        setPiece(bishop, BISHOP); setColor(bishop, BLACK)
        setPiece(queen, QUEEN); setColor(queen, BLACK)
        whiteKingConstraints = arrayListOf()
        blackKingConstraints = arrayListOf()
        whiteKingChecks = arrayListOf()
        blackKingChecks = arrayListOf()
    }
    return pawn

}

fun getAvailablePositions_Pawn_Case1_1(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val pawn = Point(4, 7)
    val queen = Point(4, 5)
    val bishop = Point(5, 6)
    configuration.apply {
        setPiece(pawn, PAWN); setColor(pawn, BLACK)
        setPiece(bishop, BISHOP); setColor(bishop, WHITE)
        setPiece(queen, QUEEN); setColor(queen, WHITE)
        whiteKingConstraints = arrayListOf()
        blackKingConstraints = arrayListOf()
        whiteKingChecks = arrayListOf()
        blackKingChecks = arrayListOf()
    }
    return pawn

}

fun getAvailablePositions_Pawn_Case2_0(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val pawn = Point(4, 7)
    val rook = Point(2, 6)
    val king = Point(6, 6)
    configuration.apply {
        setPiece(pawn, PAWN); setColor(pawn, WHITE)
        setPiece(rook, ROOK); setColor(rook, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
        whiteKingChecks = arrayListOf(HorizontalCheck(king, rook))
    }
    return pawn
}

fun getAvailablePositions_Pawn_Case2_1(configuration: GameConfiguration): Point {

    resetConfiguration(configuration)
    val pawn = Point(4, 7)
    val rook = Point(2, 6)
    val king = Point(6, 6)
    configuration.apply {
        setPiece(pawn, PAWN); setColor(pawn, BLACK)
        setPiece(rook, ROOK); setColor(rook, WHITE)
        setPiece(king, KING); setColor(king, BLACK)
        blackKingChecks = arrayListOf(HorizontalCheck(king, rook))
    }
    return pawn

}

fun getAvailablePositions_Pawn_Case3_0(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val pawn = Point(4, 6)
    val rook = Point(2, 6)
    val king = Point(6, 6)
    configuration.apply {
        setPiece(pawn, PAWN); setColor(pawn, WHITE)
        setPiece(rook, ROOK); setColor(rook, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
        whiteKingConstraints = arrayListOf(HorizontalConstraint(king, pawn))
    }
    return pawn
}

fun getAvailablePositions_Pawn_Case3_1(configuration: GameConfiguration): Point {

    resetConfiguration(configuration)
    val pawn = Point(4, 6)
    val rook = Point(2, 6)
    val king = Point(6, 6)
    configuration.apply {
        setPiece(pawn, PAWN); setColor(pawn, BLACK)
        setPiece(rook, ROOK); setColor(rook, WHITE)
        setPiece(king, KING); setColor(king, BLACK)
        whiteKingConstraints = arrayListOf(HorizontalConstraint(king, pawn))
    }
    return pawn

}

fun getAvailablePositions_Bishop_Case1_0(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val bishop = Point(4, 4)
    val king = Point(5, 5)
    val queen1 = Point(6, 2)
    val queen2 = Point(2, 2)
    configuration.apply {
        setPiece(bishop, BISHOP); setColor(bishop, WHITE)
        setPiece(queen1, QUEEN); setColor(queen1, WHITE)
        setPiece(queen2, QUEEN); setColor(queen2, BLACK)
        setPiece(king, KING); setColor(king, BLACK)
    }

    return bishop
}

fun getAvailablePositions_Bishop_Case1_1(configuration: GameConfiguration): Point {

    resetConfiguration(configuration)
    val bishop = Point(4, 4)
    val king = Point(5, 5)
    val queen1 = Point(6, 2)
    val queen2 = Point(2, 2)
    configuration.apply {
        setPiece(bishop, BISHOP); setColor(bishop, BLACK)
        setPiece(queen1, QUEEN); setColor(queen1, BLACK)
        setPiece(queen2, QUEEN); setColor(queen2, WHITE)
        setPiece(king, KING); setColor(king, WHITE)
    }

    return bishop
}

fun getAvailablePositions_Bishop_Case2_0(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val bishop = Point(4, 4)
    val queen = Point(2, 2)
    val king = Point(5, 5)
    configuration.apply {
        setPiece(bishop, BISHOP); setColor(bishop, WHITE)
        setPiece(queen, QUEEN); setColor(queen, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
        whiteKingConstraints = arrayListOf(MainDiagonalConstraint(king, bishop))
    }
    return bishop
}

fun getAvailablePositions_Bishop_Case3_0(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val bishop = Point(4, 4)
    val queen = Point(6, 2)
    val king = Point(3, 5)
    configuration.apply {
        setPiece(bishop, BISHOP); setColor(bishop, WHITE)
        setPiece(queen, QUEEN); setColor(queen, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
        whiteKingConstraints = arrayListOf(SecondDiagonalConstraint(king, bishop))
    }
    return bishop
}

fun getAvailablePositions_Bishop_Case4_0(configuration: GameConfiguration): Point {
    resetConfiguration(configuration)
    val bishop = Point(4, 4)
    val rook = Point(2, 4)
    val king = Point(2, 7)
    configuration.apply {
        setPiece(bishop, BISHOP); setColor(bishop, WHITE)
        setPiece(rook, ROOK); setColor(rook, BLACK)
        setPiece(king, KING); setColor(king, WHITE)
        whiteKingChecks = arrayListOf(VerticalCheck(king, rook))
        return bishop
    }

}

fun resetConfiguration(configuration: GameConfiguration) {
    for (i in 1..8)
        for (j in 1..8) {
            configuration.setPiece(Point(i, j), NONE)
            configuration.setColor(Point(i, j), NONE)
        }
}