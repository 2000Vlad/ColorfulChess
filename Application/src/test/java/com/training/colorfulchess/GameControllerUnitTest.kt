package com.training.colorfulchess

import android.graphics.Point
import android.os.SystemClock
import android.util.Log
import android.util.TimeUtils
import com.training.colorfulchess.game.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import io.mockk.*
import org.junit.Before
import com.google.common.truth.Truth.*
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class GameControllerUnitTest {
    @Before
    fun initExtensions() {
        mockkStatic("com.training.colorfulchess.game.GameConfigurationKt")

    }

    @Test
    fun getActionType_1() {
        val configuration: GameConfiguration = mockk()
        val controller = GameController(configuration)

        configuration.stubColor(BLACK, Point(1, 2))
        configuration.stubColor(WHITE, Point(1, 1))
        configuration.stubColor(NONE, Point(1, 3))

        controller.currentAction = noAction
        controller.player = PLAYER_1

        assert(controller.isSelectedAction(Point(1, 1)))
        assert(!controller.isDeselectedAction(Point(1, 1)))
        assert(!controller.isEnemySelectedAction(Point(1, 1)))
        assert(!controller.isMovedAction(Point(1, 1)))
        assert(!controller.isEnemyDeselectedAction(Point(1, 1)))

        assert(!controller.isSelectedAction(Point(1, 2)))
        assert(!controller.isDeselectedAction(Point(1, 2)))
        assert(controller.isEnemySelectedAction(Point(1, 2)))
        assert(!controller.isMovedAction(Point(1, 2)))
        assert(!controller.isEnemyDeselectedAction(Point(1, 2)))


        controller.currentAction = Action(
            ACTION_SELECTED,
            ActionSelectedData().apply {
                this.selectedPosition = Point(1, 1)
            }
        )
        configuration.stubEmpty(Point(1, 3), true)

        controller.currentPiecePath = mutableListOf(Point(1, 3))
        assert(!controller.isSelectedAction(Point(1, 3)))
        assert(!controller.isDeselectedAction(Point(1, 3)))
        assert(!controller.isEnemySelectedAction(Point(1, 3)))
        assert(controller.isMovedAction(Point(1, 3)))
        assert(!controller.isEnemyDeselectedAction(Point(1, 3)))

        controller.currentPiecePath = mutableListOf()

        controller.currentAction = Action(
            ACTION_ENEMY_SELECTED,
            ActionEnemySelectedData().apply {
                this.selectedPosition = Point(1, 2)
            }
        )
        controller.currentPiecePath = mutableListOf(Point(1, 2))
        assert(!controller.isSelectedAction(Point(1, 2)))
        assert(!controller.isDeselectedAction(Point(1, 2)))
        assert(!controller.isEnemySelectedAction(Point(1, 2)))
        assert(!controller.isMovedAction(Point(1, 2)))
        assert(controller.isEnemyDeselectedAction(Point(1, 2)))


    }

    @Test
    fun performSelectAction_1() {
        val configuration = mockk<GameConfiguration>()
        val controller = GameController(configuration)

        configuration.stubPiece(KNIGHT, Point(4, 5))
        configuration.stubColor(BLACK, Point(4, 5))
        configuration.stubPiece(PAWN, Point(5, 3))
        configuration.stubColor(WHITE, Point(5, 3))
        configuration.stubPiece(NONE, Point(6, 4))
        configuration.stubColor(NONE, Point(6, 4))

        configuration.stubPath(
            Point(4, 5),
            Point(5, 3),
            Point(6, 4)
        )
        controller.currentAction = noAction
        controller.player = PLAYER_2
        val start = System.nanoTime()
        val transformations = controller.performSelectAction(Point(4, 5)).transformations
        val end = System.nanoTime()
        System.out.`println`(((end - start) / 1000000).toString() + "miliseconds")
        assertThat(transformations)
            .containsExactly(
                Transformation(
                    Point(5, 3).toIndex(),
                    CellProperties(
                        piece = PAWN,
                        background = SELECTED,
                        color = WHITE,
                        reverse = false
                    )
                ),
                Transformation(
                    Point(6, 4).toIndex(),
                    CellProperties(
                        piece = NONE,
                        background = SELECTED,
                        color = NONE,
                        reverse = false
                    )
                ),
                Transformation(
                    Point(4, 5).toIndex(),
                    CellProperties(
                        piece = KNIGHT,
                        background = SELECTED,
                        color = BLACK,
                        reverse = true
                    )
                )
            )


    }

    @Test
    fun performEnemySelect_1() {
        val configuration = GameConfiguration()
        val controller = GameController(configuration)
        val whitePawn = Point(4, 3)
        val blackPawn = Point(4, 6)
        configuration.apply {
            stubPiece(PAWN, whitePawn)
            stubPiece(PAWN, blackPawn)
            stubColor(BLACK, blackPawn)
            stubColor(WHITE, whitePawn)
            stubColor(NONE, whitePawn.backward())
            stubPiece(NONE, whitePawn.backward())
            stubColor(NONE, whitePawn.backward().backward())
            stubPiece(NONE, whitePawn.backward().backward())
        }
        controller.currentPiecePath = mutableListOf(Point(4, 4), Point(4, 5))
        controller.player = PLAYER_2
        controller.currentAction = Action(ACTION_SELECTED, ActionSelectedData().apply {
            selectedPosition = Point(4, 6)
        })
        val transformations = controller.performEnemySelectAction(whitePawn).transformations
        assertThat(transformations).containsExactly(
            Transformation(
                whitePawn.backward().toIndex(),
                CellProperties(
                    piece = NONE,
                    color = NONE,
                    background = DEFAULT,
                    reverse = false
                )
            ),
            Transformation(
                whitePawn.backward().backward().toIndex(),
                CellProperties(
                    piece = NONE,
                    color = NONE,
                    background = DEFAULT,
                    reverse = false
                )
            ),
            Transformation(
                blackPawn.toIndex(),
                CellProperties(
                    piece = PAWN,
                    color = BLACK,
                    background = DEFAULT,
                    reverse = true
                )
            ),
            Transformation(
                whitePawn.toIndex(),
                CellProperties(
                    piece = PAWN,
                    color = WHITE,
                    background = ENEMY_SELECTED,
                    reverse = false
                )
            )

        )
    }

    @Test
    fun performSelectAction_performance() {
        val configuration = GameConfiguration()
        val controller = GameController(configuration)
        configuration.apply {
            setPiece(Point(4, 5), KNIGHT)
            setColor(Point(4, 5), WHITE)
        }
        controller.player = PLAYER_1
        var transformations = emptyList<Transformation>()
        val start = SystemClock.elapsedRealtime()
        transformations = controller.performSelectAction(Point(4, 5)).transformations
        val end = SystemClock.elapsedRealtime()
        System.out.println((end - start).toString() + " miliseconds")


    }

    @Test
    fun performMoveAction_1() {
        val configuration = mockk<GameConfiguration>()
        val controller = GameController(configuration)

        configuration.stubPiece(KNIGHT, Point(4, 5))
        configuration.stubColor(WHITE, Point(4, 5))
        configuration.stubEmpty(Point(4, 5), false)

        configuration.stubPiece(BISHOP, Point(5, 3))
        configuration.stubColor(BLACK, Point(5, 3))
        configuration.stubEmpty(Point(5, 3), false)

        configuration.stubPiece(NONE, Point(6, 4))
        configuration.stubColor(NONE, Point(6, 4))
        configuration.stubEmpty(Point(6, 4), true)
        controller.player = PLAYER_1
        controller.currentPiecePath = mutableListOf(Point(5, 3), Point(6, 4))
        val transformations = controller.performMoveAction(Point(4, 5), Point(5, 3)).transformations
        assertThat(transformations)
            .containsExactly(
                Transformation(
                    Point(5, 3).toIndex(),
                    CellProperties(
                        piece = BISHOP,
                        color = BLACK,
                        background = DEFAULT,
                        reverse = true
                    )
                ),
                Transformation(//
                    Point(6, 4).toIndex(),
                    CellProperties(
                        piece = NONE,
                        color = NONE,
                        background = DEFAULT,
                        reverse = false
                    )
                ),
                Transformation(
                    Point(4, 5).toIndex(),
                    CellProperties(
                        piece = KNIGHT,
                        color = WHITE,
                        background = DEFAULT,
                        reverse = false
                    )
                ),
                Transformation(
                    Point(4, 5).toIndex(),
                    CellProperties(
                        piece = NONE,
                        color = NONE,
                        background = DEFAULT,
                        reverse = false
                    )
                ),
                Transformation(
                    Point(5, 3).toIndex(),
                    CellProperties(
                        piece = KNIGHT,
                        color = WHITE,
                        background = DEFAULT,
                        reverse = false
                    )
                )
            )


    }

    @Test
    fun performDeselectAction_1() {
        val configuration = mockk<GameConfiguration>()
        val controller = GameController(configuration)
        controller.player = PLAYER_2
        val pawn = Point(4, 4)
        configuration.apply {
            stubPiece(PAWN, pawn)
            stubColor(BLACK, pawn)
            stubEmpty(pawn, false)

            stubPiece(NONE, pawn.forward())
            stubColor(NONE, pawn.forward())
            stubEmpty(pawn.forward(), true)

            stubPiece(NONE, pawn.forward().forward())
            stubColor(NONE, pawn.forward().forward())
            stubEmpty(pawn.forward().forward(), true)

        }
        val action = Action(
            ACTION_SELECTED,
            ActionSelectedData().apply {
                selectedPosition = pawn
                piece = PieceData().apply {
                    pieceType = PAWN
                    pieceColor = BLACK
                }
            }
        )
        controller.currentPiecePath = mutableListOf(pawn.forward(), pawn.forward().forward())
        val transformations = controller.performDeselectAction(pawn).transformations
        assertThat(transformations)
            .containsExactly(
                Transformation(
                    pawn.toIndex(),
                    CellProperties(
                        piece = PAWN,
                        color = BLACK,
                        background = DEFAULT,
                        reverse = true
                    )
                ),
                Transformation(
                    pawn.forward().toIndex(),
                    CellProperties(
                        piece = NONE,
                        color = NONE,
                        background = DEFAULT,
                        reverse = false
                    )
                ),
                Transformation(
                    pawn.forward().forward().toIndex(),
                    CellProperties(
                        piece = NONE,
                        color = NONE,
                        background = DEFAULT,
                        reverse = false
                    )
                )

            )
    }

    @Test
    fun performEnemyDeselectAction_1() {
        val configuration = mockk<GameConfiguration>()
        val controller = GameController(configuration)
        controller.player = PLAYER_1
        val enemyPawn = Point(4, 4)
        configuration.apply {

            stubPiece(PAWN, enemyPawn)
            stubColor(BLACK, enemyPawn)
            stubEmpty(enemyPawn, false)

            stubPiece(NONE, enemyPawn.forward())
            stubColor(NONE, enemyPawn.forward())
            stubEmpty(enemyPawn.forward(), true)

        }
        controller.currentAction = Action(ACTION_ENEMY_SELECTED, null).apply {

            actionData = ActionEnemySelectedData().apply {
                enemyPiece = PieceData().apply {
                    this.pieceType = PAWN
                    this.pieceColor = BLACK
                }
                selectedPosition = enemyPawn
            }
        }
        val transformations = controller.performEnemyDeselectAction(enemyPawn).transformations
        assertThat(transformations).containsExactly(
            Transformation(
                enemyPawn.toIndex(),
                CellProperties(
                    piece = PAWN,
                    color = BLACK,
                    background = DEFAULT,
                    reverse = true
                )
            )
        )

    }

    @Test
    fun test() {
        //val uselessObject = mockk<UselessClass>()
        //every { uselessObject.numbers } returns sequenceOf(3, 2, 1)
        //assert(uselessObject.numbers.elementAt(0) == 3)
        val t1 =
            Transformation(
                position = 20,
                newProperties = CellProperties(
                    piece = 2,
                    color = 8,
                    background = 10,
                    reverse = true
                )
            )
        val t2 =
            Transformation(
                Point(5, 3).toIndex(),
                CellProperties(
                    piece = BISHOP,
                    color = BLACK,
                    background = DEFAULT,
                    reverse = true
                )
            )
        assert(t1 == t2)

    }


}


fun GameConfiguration.stubColor(color: Int, position: Point) {
    every { getColor(position) } returns color
}

fun GameConfiguration.stubPiece(piece: Int, position: Point) {
    every { getPiece(position) } returns piece
}

fun GameConfiguration.stubPath(piece: Point, vararg path: Point) {
    every { getAvailablePositions(piece) } returns path.asSequence()
}

fun GameConfiguration.stubEmpty(position: Point, empty: Boolean) {
    every { isEmpty(position) } returns empty
}

class UselessClass {
    var numbers = sequenceOf(1, 2, 3)
    fun getNumber(index: Int): Int {
        Log.e("UselessClass", "getNumberCalled")
        return numbers.elementAt(index)
    }
}