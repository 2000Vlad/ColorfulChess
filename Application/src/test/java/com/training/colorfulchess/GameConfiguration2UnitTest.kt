package com.training.colorfulchess

import android.graphics.Point
import com.training.colorfulchess.game.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.google.common.truth.Truth.*;
import com.training.colorfulchess.game.modelvm2.*

@RunWith(RobolectricTestRunner::class)
class GameConfiguration2UnitTest {
    @Test
    fun gameState_WorksCorrect() {

    }

    @Test
    fun getKingPositions_WorksCorrect() {
        val configuration =
            GameConfiguration2()
        configuration.player = PLAYER_1
        configuration.whitePieces =
            mutableMapOf(
                Point(3, 5) to KING
            )
        configuration.blackPieces =
            mutableMapOf(
                Point(4, 3) to PAWN,
                Point(6, 4) to ROOK,
                Point(6, 6) to QUEEN
            )
        configuration.apply {
            setPiece(Point(3, 5), KING)
            setPiece(Point(4, 3), PAWN)
            setPiece(Point(6, 4), ROOK)
            setPiece(Point(6, 6), QUEEN)

            setColor(Point(3, 5), WHITE)
            setColor(Point(4, 3), BLACK)
            setColor(Point(6, 4), BLACK)
            setColor(Point(6, 6), BLACK)
        }
        val path = configuration.getKingPositions(Point(3, 5)).toList()
        assertThat(path).containsExactly(Point(2, 5), Point(4, 5))

    }

    @Test
    fun getState_WorksCorrect() { //GameConfiguration2.init() throws StalemateException so test is successful
        var configuration = fromTestFile("testcase2.txt")
        assert(configuration.gameState == STALEMATE)
        configuration = fromTestFile("testcase3.txt")
        assert(configuration.gameState == CHECKMATE)
        configuration = fromTestFile("testcase4.txt")
        assert(configuration.gameState == NONE)
        assertThat(configuration.getAvailablePositions(Point(8,3)).toList())
            .containsExactly(
                Point(6,3)
            )
    }

    @Test
    fun getAbsoluteRookPositions_WorksCorrect(){
        val configuration = fromTestFile("testcase4.txt")
        assertThat(configuration.getAbsolutePositions(Point(8,3)).toList())
            .containsExactly(
                Point(8,2),
                Point(8,1),
                Point(7,3),
                Point(6,3),
                Point(5,3),
                Point(4,3),
                Point(3,3),
                Point(8,4)
            )
    }
    fun getKingConstraints_WorksCorrect(){

    }
    @Test
    fun test_fromFile() {
        val configuration =
           fromTestFile("testcase2.txt")


    }
}