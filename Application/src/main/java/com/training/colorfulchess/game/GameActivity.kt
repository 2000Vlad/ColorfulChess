package com.training.colorfulchess.game

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.training.colorfulchess.R
import com.training.colorfulchess.game.adapters.SkinAdapter
import com.training.colorfulchess.game.timer.GameTimer
import com.training.colorfulchess.game.timer.doOnEnd
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import kotlin.collections.List

class GameActivity : AppCompatActivity(), GameStateListener, SkinAdapter.OnSkinSelectedListener {

    companion object {
        const val SAVED_GAME_FILE = "savedGame"
        const val GAME_MODE = "gameMode"
        const val NEW_GAME = "newGame"
        const val LOAD_GAME = "loadGame"
        const val GAME_SAVED = "gameSaved"
    }

    private lateinit var controller: TableController

    private val table: GridLayout by lazy { findViewById<GridLayout>(R.id.include2) }

    private val player1View: TextView by lazy { findViewById<TextView>(R.id.player1_textview) }

    private val player2View: TextView by lazy { findViewById<TextView>(R.id.player2_textview) }

    private val timer1: GameTimer by lazy { findViewById<GameTimer>(R.id.player1_timer) }

    private val timer2: GameTimer by lazy { findViewById<GameTimer>(R.id.player2_timer) }

    private val drawerLayout: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.game_drawer) }

    private val skinRecyclerView: RecyclerView by lazy { findViewById<RecyclerView>(R.id.skin_rv) }

    private val skinAdapter: SkinAdapter by lazy { SkinAdapter() }

    private var activeTimer: Int = NONE

    private var save = true

    private lateinit var serializer: GameSerializer

    private lateinit var viewModel: ChessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gameMode = intent.extras!!.getString(GAME_MODE)

        val gameController = GameController(GameConfiguration())

        var configuration = Configuration()


        if (savedInstanceState == null) {
            viewModel = ViewModelProviders.of(this).get(ChessViewModel::class.java)

            gameController.listener = this

            when (gameMode) {
                NEW_GAME -> {
                    gameController.configuration = defaultGameConfiguration
                    configuration = defaultConfiguration
                }

                LOAD_GAME -> {
                    gameController.deserialize(

                        openFileInput(
                            SAVED_GAME_FILE
                        )
                    )
                    configuration.fromFile(
                        openFileInput(SAVED_GAME_FILE)

                    )

                }
                else -> throw IllegalArgumentException("Game mode is incorrect")
            }
            viewModel.provider = gameController
            viewModel.selectedPathProvider = gameController


        }
        serializer = gameController
        controller = TableController(table, configuration, getSkinById(DEFAULT_SKIN_ID, this))
        controller.initialize()
        for (i in 0..63) {
            val cell = table[i] as TableCell
            val dragListener = CellDragListener(viewModel, controller)
            val touchListener = CellTouchListener(viewModel, controller)
            dragListener.position = i
            touchListener.position = i
            dragListener.stateProvider = viewModel
            cell.setOnDragListener(dragListener)
            cell.setOnTouchListener(touchListener)


        }

        if (gameController.player == PLAYER_1) {
            player1View.text = getString(R.string.your_turn)
            player2View.text = getString(R.string.enemy_turn)
        } else {
            player1View.text = getString(R.string.enemy_turn)
            player2View.text = getString(R.string.your_turn)
        }

        activeTimer = if (gameController.player == PLAYER_1) {
            timer1.start()
            PLAYER_1
        } else {
            timer2.start()
            PLAYER_2
        }

        timer1.doOnEnd {
            this@GameActivity.controller.processTransformations(*(viewModel.switchTurns().toTypedArray()))
        }
        timer2.doOnEnd {
            this@GameActivity.controller.processTransformations(*(viewModel.switchTurns().toTypedArray()))
        }

        skinRecyclerView.layoutManager = LinearLayoutManager(this)
        skinAdapter.listener = this
        skinAdapter.skins = listOf(
            getSkinById(DEFAULT_SKIN_ID,this),
            getSkinById(DARK_SKIN_ID, this)
        )
        skinRecyclerView.adapter = skinAdapter

    }

    override fun onSkinChanged(skin: Skin) {
        controller.skin = skin
        drawerLayout.closeDrawer(GravityCompat.END)

    }

    override fun onGameStateChanged(state: Int) {
        when (state) {
            PLAYER_1 -> {
                player1View.text = getString(R.string.you_win)
                player2View.text = getString(R.string.enemy_wins)
                save = false
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(GAME_SAVED, false)
                    .apply()
                application.deleteFile(SAVED_GAME_FILE)
                timer1.reset()
                timer2.reset()
            }
            PLAYER_2 -> {
                player1View.text = getString(R.string.enemy_wins)
                player2View.text = getString(R.string.you_win)
                save = false
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(GAME_SAVED, false)
                    .apply()
                application.deleteFile(SAVED_GAME_FILE)
                timer1.reset()
                timer2.reset()
            }

        }
    }

    override fun onTurnChanged(player: Int) {
        when (player) {
            PLAYER_1 -> {
                player1View.text = getString(R.string.your_turn)
                player2View.text = getString(R.string.enemy_turn)
                timer2.reset()
                timer1.start()
                activeTimer = PLAYER_1
            }
            PLAYER_2 -> {
                player1View.text = getString(R.string.enemy_turn)
                player2View.text = getString(R.string.your_turn)
                timer1.reset()
                timer2.start()
                activeTimer = PLAYER_2
            }

        }
    }

    fun switchTimers() {
        if (activeTimer == PLAYER_1) {
            timer1.reset()
            timer2.start()
            activeTimer = PLAYER_2
            return
        }
        if (activeTimer == PLAYER_2) {
            timer2.reset()
            timer1.start()
            activeTimer = PLAYER_1
            return
        }
    }

    override fun onPause() {
        super.onPause()
        if (!save) return
        serializer.serialize(
            openFileOutput(SAVED_GAME_FILE, Context.MODE_PRIVATE)
        )
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.edit()
            .putBoolean(GAME_SAVED, true)
            .apply()

    }

    interface ActionProvider {
        fun getTransformations(position: Int): List<Transformation>
        fun getActionData(): ActionData?
    }

    interface GameSerializer {
        fun serialize(stream: FileOutputStream)
    }
}

class TableController(gridLayout: GridLayout, config: Configuration, textures: Skin) :
    CellClickListener.TransformationProcessor, Skin.SkinProvider {
    private val table = gridLayout
    private var configuration = config
    override var skin: Skin = textures
        set(value) {
            field = value
            initialize()
        }

    fun initialize() {
        for (i in 0..63) {
            val cellView = table[i] as TableCell
            val properties = configuration.cellAt(i)
            cellView.backgroundTexture =
                when (properties!!.background) {
                    WHITE -> skin.whiteCell
                    BLACK -> skin.blackCell
                    SELECTED -> skin.selectedCell
                    ENEMY_SELECTED -> skin.enemyCell
                    DEFAULT ->
                        if ((i / 8) % 2 == 0)
                            if (i % 2 == 0) skin.whiteCell
                            else skin.blackCell
                        else
                            if (i % 2 == 0) skin.blackCell
                            else skin.whiteCell
                    else -> throw IllegalArgumentException("CellProperties.background (instance) incorrect")
                }
            val reverse = properties.reverse
            val color = properties.color
            var pieceDrawable = when (properties.piece) {
                PAWN ->
                    if (color == BLACK) skin.blackPawn
                    else skin.whitePawn
                BISHOP ->
                    if (color == BLACK) skin.blackBishop
                    else skin.whiteBishop
                KNIGHT ->
                    if (color == BLACK) skin.blackKnight
                    else skin.whiteKnight
                ROOK ->
                    if (color == BLACK) skin.blackRook
                    else skin.whiteRook
                QUEEN ->
                    if (color == BLACK) skin.blackQueen
                    else skin.whiteQueen
                KING ->
                    if (color == BLACK) skin.blackKing
                    else skin.whiteKing
                NONE -> null
                else -> throw IllegalArgumentException("Piece not correct")

            }
            if (reverse && pieceDrawable != null)
                pieceDrawable = reverseDrawable(table.context, pieceDrawable)

            cellView.pieceDrawable = pieceDrawable

        }
    }

    override fun processTransformations(vararg transformations: Transformation) {
        configuration.change(*transformations)
        for (transformation in transformations) {
            val position = transformation.position
            val piece = transformation.newProperties.piece
            val background = transformation.newProperties.background
            val color = transformation.newProperties.color
            val reverse = transformation.newProperties.reverse
            val cellView = table[position] as TableCell
            cellView.backgroundTexture =
                when (background) {
                    BLACK -> skin.blackCell
                    WHITE -> skin.whiteCell
                    SELECTED -> skin.selectedCell
                    ENEMY_SELECTED -> skin.enemyCell
                    DEFAULT ->
                        if ((position / 8) % 2 == 0)
                            if (position % 2 == 0) skin.whiteCell
                            else skin.blackCell
                        else
                            if (position % 2 == 0) skin.blackCell
                            else skin.whiteCell
                    else -> throw IllegalArgumentException("Cell background incorrect")
                }
            var pieceDrawable =
                when (piece) {
                    PAWN -> if (color == BLACK) skin.blackPawn
                    else skin.whitePawn
                    BISHOP -> if (color == BLACK) skin.blackBishop
                    else skin.whiteBishop
                    ROOK -> if (color == BLACK) skin.blackRook
                    else skin.whiteRook
                    KNIGHT -> if (color == BLACK) skin.blackKnight
                    else skin.whiteKnight
                    QUEEN -> if (color == BLACK) skin.blackQueen
                    else skin.whiteQueen
                    KING -> if (color == BLACK) skin.blackKing
                    else skin.whiteKing
                    NONE -> null
                    else -> throw IllegalArgumentException("piece is incorrect")
                }
            if (reverse && pieceDrawable != null) pieceDrawable =
                reverseDrawable(table.context, pieceDrawable)
            cellView.pieceDrawable = pieceDrawable


        }
    }

}

class Skin(
    var blackPawn: Drawable,
    var whitePawn: Drawable,

    var blackBishop: Drawable,
    var whiteBishop: Drawable,

    var blackRook: Drawable,
    var whiteRook: Drawable,

    var blackKnight: Drawable,
    var whiteKnight: Drawable,

    var blackQueen: Drawable,
    var whiteQueen: Drawable,

    var blackKing: Drawable,
    var whiteKing: Drawable,

    var blackCell: Drawable,
    var whiteCell: Drawable,
    var selectedCell: Drawable,
    var enemyCell: Drawable,

    val id: Int,
    val name: String
) {

    interface SkinProvider {
        var skin: Skin
    }
}

fun defaultSkin(context: Context) = Skin(
    context.getDrawable(R.drawable.pawn_black)!!,
    context.getDrawable(R.drawable.pawn_white)!!,
    context.getDrawable(R.drawable.bishop_black)!!,
    context.getDrawable(R.drawable.bishop_white)!!,
    context.getDrawable(R.drawable.rook_black)!!,
    context.getDrawable(R.drawable.rook_white)!!,
    context.getDrawable(R.drawable.knight_black)!!,
    context.getDrawable(R.drawable.knight_white)!!,
    context.getDrawable(R.drawable.queen_black)!!,
    context.getDrawable(R.drawable.queen_white)!!,
    context.getDrawable(R.drawable.king_black)!!,
    context.getDrawable(R.drawable.king_white)!!,
    context.getDrawable(R.drawable.black_cell)!!,
    context.getDrawable(R.drawable.white_cell)!!,
    context.getDrawable(R.drawable.selected_cell)!!,
    context.getDrawable(R.drawable.enemy_cell)!!,
    id = 0,
    name = "Default Skin"
)

fun darkSkin(context: Context) = Skin(
    context.getDrawable(R.drawable.pawn_black)!!,
    context.getDrawable(R.drawable.pawn_white)!!,
    context.getDrawable(R.drawable.bishop_black)!!,
    context.getDrawable(R.drawable.bishop_white)!!,
    context.getDrawable(R.drawable.rook_black)!!,
    context.getDrawable(R.drawable.rook_white)!!,
    context.getDrawable(R.drawable.knight_black)!!,
    context.getDrawable(R.drawable.knight_white)!!,
    context.getDrawable(R.drawable.queen_black)!!,
    context.getDrawable(R.drawable.queen_white)!!,
    context.getDrawable(R.drawable.king_black)!!,
    context.getDrawable(R.drawable.king_white)!!,
    context.getDrawable(R.drawable.dark_black_cell)!!,
    context.getDrawable(R.drawable.dark_white_cell)!!,
    context.getDrawable(R.drawable.selected_cell)!!,
    context.getDrawable(R.drawable.enemy_cell)!!,
    id = 1,
    name = "Dark Skin"
)

const val DEFAULT_SKIN_ID = 0
const val DARK_SKIN_ID = 1
fun getSkinById(id: Int, context: Context) =
    when (id) {
        DEFAULT_SKIN_ID -> defaultSkin(context)
        DARK_SKIN_ID -> darkSkin(context)
        else -> throw IllegalArgumentException("Skin id incorrect")
    }
