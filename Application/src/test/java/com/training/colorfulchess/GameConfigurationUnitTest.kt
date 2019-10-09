package com.training.colorfulchess

import android.graphics.Point
import com.training.colorfulchess.game.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class GameConfigurationUnitTest {
    @Test
    fun knight_Moves_Correct() {
        val configuration: GameConfiguration = GameConfiguration()
        val knight = Point(4, 4)
        configuration.setPiece(knight, KNIGHT)
        configuration.setColor(knight, WHITE)
        val path = configuration.getAbsoluteKnightMovingPositions(knight)
        val positions = path.toList()

        assertEquals(3, positions[0].x)
        assertEquals(2, positions[0].y)

        assertEquals(5, positions[1].x)
        assertEquals(2, positions[1].y)

        assertEquals(6, positions[2].x)
        assertEquals(3, positions[2].y)

        assertEquals(6, positions[3].x)
        assertEquals(5, positions[3].y)

        assertEquals(5, positions[4].x)
        assertEquals(6, positions[4].y)

        assertEquals(3, positions[5].x)
        assertEquals(6, positions[5].y)

        assertEquals(2, positions[6].x)
        assertEquals(5, positions[6].y)

        assertEquals(2, positions[7].x)
        assertEquals(3, positions[7].y)

    }

    @Test
    fun knight_Moves_Correct2() {
        val configuration: GameConfiguration = GameConfiguration()
        val knight = Point(7, 4)
        val queen = Point(5, 3)
        val rook = Point(5, 5)


        configuration.setPiece(knight, KNIGHT)
        configuration.setColor(knight, WHITE)

        configuration.setPiece(queen, QUEEN)
        configuration.setColor(queen, BLACK)

        configuration.setPiece(rook, ROOK)
        configuration.setColor(rook, WHITE)

        val positions = configuration.getAbsoluteKnightMovingPositions(knight)
        val path = positions.toList()

        assert(path[0].x == 6); assert(path[0].y == 2)
        assert(path[1].x == 8); assert(path[1].y == 2)
        assert(path[2].x == 8); assert(path[2].y == 6)
        assert(path[3].x == 6); assert(path[3].y == 6)
        assert(path[4].x == 5); assert(path[4].y == 3)
    }

    @Test
    fun rook_Moves_Correct() {

        val configuration = GameConfiguration()

        val rook = Point(5, 5)
        val knight = Point(7, 5)
        val pawn = Point(5, 6)
        val queen = Point(5, 3)

        configuration.setPiece(rook, ROOK)
        configuration.setColor(rook, WHITE)

        configuration.setPiece(knight, KNIGHT)
        configuration.setColor(knight, WHITE)

        configuration.setPiece(queen, QUEEN)
        configuration.setColor(queen, BLACK)

        configuration.setPiece(pawn, PAWN)
        configuration.setColor(pawn, BLACK)

        val positions = configuration.getAbsoluteRookMovingPositions(rook)
        val path = positions.toList()

        assert(path.size == 8)

        assertPoints(path[0], Point(5, 4))
        assertPoints(path[1], Point(5, 3))
        assertPoints(path[2], Point(6, 5))
        assertPoints(path[3], Point(5, 6))
        assertPoints(path[4], Point(4, 5))
        assertPoints(path[5], Point(3, 5))
        assertPoints(path[6], Point(2, 5))
        assertPoints(path[7], Point(1, 5))


    }

    @Test
    fun bishop_Moves_Correct() {
        val configuration = GameConfiguration()

        val bishop = Point(5, 5)
        val knight = Point(4, 6)
        val queen = Point(7, 3)

        configuration.setPiece(bishop, BISHOP)
        configuration.setColor(bishop, WHITE)

        configuration.setPiece(queen, QUEEN)
        configuration.setColor(queen, BLACK)

        configuration.setPiece(knight, KNIGHT)
        configuration.setColor(knight, WHITE)

        val positions = configuration.getAbsoluteBishopMovingPositions(bishop)
        val path = positions.toList()

        var currentPosition: Point

        assert(path.size == 9)

        currentPosition = bishop.forwardLeft()
        assertPoints(path[0], currentPosition); currentPosition = currentPosition.forwardLeft()
        assertPoints(path[1], currentPosition); currentPosition = currentPosition.forwardLeft()
        assertPoints(path[2], currentPosition); currentPosition = currentPosition.forwardLeft()
        assertPoints(path[3], currentPosition); currentPosition = currentPosition.forwardLeft()

        currentPosition = bishop.forwardRight()
        assertPoints(path[4], currentPosition); currentPosition = currentPosition.forwardRight()
        assertPoints(path[5], currentPosition); currentPosition = currentPosition.forwardRight()

        currentPosition = bishop.backwardRight()
        assertPoints(path[6], currentPosition); currentPosition = currentPosition.backwardRight()
        assertPoints(path[7], currentPosition); currentPosition = currentPosition.backwardRight()
        assertPoints(path[8], currentPosition); currentPosition = currentPosition.backwardRight()

    }

    @Test
    fun toIndex_Is_Correct() {
        assertEquals(20, Point(5, 3).toIndex())
        assertEquals(0, Point(1, 1).toIndex())
        assertEquals(7, Point(8,1).toIndex())
    }

    @Test
    fun other_tests() {
        assert(Point(2,2) == Point(2,2))
        assert(Point(2,2).backwardRight() == Point(3,3))
        val list = sequenceOf(1,2,3).take(4).toList()
        assert(list.size == 3)
    }

    @Test
    fun hasKnightThreat_Works_Correct() {
        val configuration = GameConfiguration()
        var knight = Point(4,4)
        var underThreat = Point(6,5)
        configuration.setPiece(knight, KNIGHT)
        configuration.setColor(knight, WHITE)

        var threat = configuration.hasKnightThreat(underThreat, BLACK)
        assert(threat)
        assert(configuration.getPiece(underThreat) == NONE)
        assert(configuration.getColor(underThreat) == NONE)

        threat = configuration.hasKnightThreat(underThreat, WHITE)
        assert(!threat)
        assert(configuration.getPiece(underThreat) == NONE)
        assert(configuration.getColor(underThreat) == NONE)

        underThreat = Point(5,5)
        threat = configuration.hasKnightThreat(underThreat, BLACK)
        assert(!threat)
        assert(configuration.getPiece(underThreat) == NONE)
        assert(configuration.getColor(underThreat) == NONE)

        underThreat = Point(5,6)
        threat = configuration.hasKnightThreat(underThreat, BLACK)
        assert(threat)
        assert(configuration.getPiece(underThreat) == NONE)
        assert(configuration.getColor(underThreat) == NONE)

        knight = Point(1,1)
        configuration.setPiece(knight, KNIGHT)
        configuration.setColor(knight, WHITE)
        underThreat = Point(3,2)
        threat = configuration.hasKnightThreat(underThreat, BLACK)
        assert(threat)
        assert(configuration.getPiece(underThreat) == NONE)
        assert(configuration.getColor(underThreat) == NONE)
    }

    @Test
    fun hasPawnThreat_Works_Correct() {
        val configuration = GameConfiguration()
        val rook = Point(4,5)
        var pawn = Point(3,4)
        configuration.setPiece(rook, ROOK)
        configuration.setColor(rook, WHITE)
        configuration.setPiece(pawn, PAWN)
        configuration.setColor(pawn, BLACK)

        var hasThreat = configuration.hasPawnThreat(rook, WHITE)
        assert(hasThreat)
        configuration.setPiece(pawn, NONE)
        configuration.setColor(pawn, NONE)
        pawn = rook.backwardLeft()
        configuration.setPiece(pawn, PAWN)
        configuration.setColor(pawn, WHITE)
        hasThreat = configuration.hasPawnThreat(rook, WHITE)
        assert(!hasThreat)


    }

    @Test
    fun hasLinearThreatOnDirection_Works_Correct(){
        val configuration = GameConfiguration()
        val point = Point(4,4)
        val rook = Point(4,8)
        val queen = Point(3,3)
        configuration.setPiece(rook, ROOK)
        configuration.setColor(rook, BLACK)
        configuration.setPiece(queen, QUEEN)
        configuration.setColor(queen, BLACK)

        val hasThreat = configuration.hasLinearThreatOnDirection(point, WHITE, Point::right)
        assert(!hasThreat)
    }

    @Test
    fun hasThreat_Works_Correct() {
        val configuration = GameConfiguration()
        var pos = hasThreat_Works_Correct_Case1(configuration)
        var hasThreat = configuration.hasThreat(pos, WHITE)
        assert(hasThreat.size != 0)
        pos = hasThreat_Works_Correct_Case2(configuration)
        hasThreat = configuration.hasThreat(pos, WHITE)
        assert(hasThreat.size == 0)
    }

    @Test
    fun king_Path_Works_Correct(){
        val configuration = GameConfiguration()
        var king : Point
        var path : Sequence<Point>
        king = king_Path_Works_Correct_Case1(configuration)
        path = configuration.getKingPositions(king)
        assert_King_Path_Works_Correct_Case1(path)
        king = king_Path_Works_Correct_Case2(configuration)
        path = configuration.getKingPositions(king)
        assert_King_Path_Works_Correct_Case2(path)
        king = king_Path_Works_Correct_Case3(configuration)
        path = configuration.getKingPositions(king)
        assert_King_Path_Works_Correct_Case3(path)






    }

    @Test
    fun get_Horizontal_Constraints_Works_Correct(){
        val configuration = GameConfiguration()
        val king = getHorizontalConstraints_Works_Correct_Case1(configuration)
        val constraints = configuration.getHorizontalConstraints(king)
        assert(constraints.size == 1)
        assert(constraints[0].friend == Point(6,5))
    }
    @Test
    fun getConstraints_Works_Correct() {
        val configuration = GameConfiguration()
        val king = getConstraints_Works_Correct_Case1(configuration)
        val constraints = configuration.getConstraints(king)
        assert_getConstraints_Case1(constraints)

    }

    @Test
    fun getChecks_Works_Correct() {
        val configuration = GameConfiguration()
        val king = getChecks_Works_Correct_Case1(configuration)
        val checks = configuration.getChecks(king)
        //assert(checks.size == 16)
    }

    @Test
    fun getAvailablePositions_Works_Correct_Pawn(){
        val configuration = GameConfiguration()

        var pawn = getAvailablePositions_Pawn_Case1_0(configuration)
        var positions = configuration.getAvailablePositions(pawn).toList()
        assert_getAvailablaPositions_Pawn_Case1_0(positions)

        pawn = getAvailablePositions_Pawn_Case1_1(configuration)
        positions = configuration.getAvailablePositions(pawn).toList()
        assert_getAvailablaPositions_Pawn_Case1_1(positions)

        pawn = getAvailablePositions_Pawn_Case2_0(configuration)
        positions = configuration.getAvailablePositions(pawn).toList()
        assert_getAvailablePositions_Pawn_Case2_0(positions)

        pawn = getAvailablePositions_Pawn_Case2_1(configuration)
        positions = configuration.getAvailablePositions(pawn).toList()
        assert_getAvailablePositions_Pawn_Case2_1(positions)

        pawn = getAvailablePositions_Pawn_Case3_0(configuration)
        positions = configuration.getAvailablePositions(pawn).toList()
        assert_getAvailablePositions_Pawn_Case3_0(positions)

        pawn = getAvailablePositions_Pawn_Case3_1(configuration)
        positions = configuration.getAvailablePositions(pawn).toList()
        assert_getAvailablePositions_Pawn_Case3_1(positions)


    }

    @Test
    fun getAvailablePositions_Works_Correct_Bishop(){
        val configuration = GameConfiguration()

        var bishop : Point// = getAvailablePositions_Bishop_Case1_0(configuration)
        var positions : List<Point> //= configuration.getAvailablePositions(bishop).toList()
       // assert_getAvailablePositions_Bishop_Case1_0(positions)

       // bishop = getAvailablePositions_Bishop_Case2_0(configuration)
       // positions = configuration.getAvailablePositions(bishop).toList()
       // assert_getAvailablePositions_Bishop_Case2_0(positions)

       // bishop = getAvailablePositions_Bishop_Case3_0(configuration)
       // positions = configuration.getAvailablePositions(bishop).toList()
       // assert_getAvailablePositions_Bishop_Case3_0(positions)

        bishop = getAvailablePositions_Bishop_Case4_0(configuration)
        positions = configuration.getAvailablePositions(bishop).toList()
        assert_getAvailablePositions_Bishop_Case4_0(positions)
    }








}
