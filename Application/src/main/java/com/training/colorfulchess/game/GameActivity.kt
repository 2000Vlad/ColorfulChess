package com.training.colorfulchess.game

import android.content.ClipData
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.TimingLogger
import android.view.DragEvent.ACTION_DRAG_STARTED
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.get
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.training.colorfulchess.R
import com.training.colorfulchess.game.adapters.SkinPagerAdapter
import kotlinx.android.synthetic.main.activity_game.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException
import java.util.ArrayList
import kotlin.collections.List

class GameActivity : AppCompatActivity(), GameStateListener {

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

    private val skinPager : ViewPager by lazy { findViewById<ViewPager>(R.id.skin_vp) }

    private lateinit var skinAdapter : SkinPagerAdapter

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

        }
        serializer = gameController
        controller = TableController(table, configuration, getSkinById(DEFAULT_SKIN_ID,this))
        controller.initialize()
        for (i in 0..63) {
            val cell = table[i] as TableCell
            val listener = CellClickListener(viewModel, controller)
            listener.position = i
            cell.setOnClickListener(listener)
        }
        if(gameController.player == PLAYER_1) {
            player1View.text = "Your turn"
            player2View.text = "Enemy turn"
        }
        else {
            player1View.text = "Enemy turn"
            player2View.text = "Your turn"
        }
        skinAdapter = SkinPagerAdapter(supportFragmentManager, defaultSkin(this), darkSkin(this))
        skinPager.adapter = skinAdapter
        skinPager.addOnPageChangeListener(SkinPageChangedListener(controller, this))


    }

    override fun onGameStateChanged(state: Int) {
        when (state) {
            PLAYER_1 -> {
                player1View.text = "You win"
                player2View.text = "Enemy wins"
                save = false
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(GAME_SAVED, false)
                    .apply()
                application.deleteFile(SAVED_GAME_FILE)
            }
            PLAYER_2 -> {
                player1View.text = "Enemy wins"
                player2View.text = "Your win"
                save = false
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(GAME_SAVED, false)
                    .apply()
                application.deleteFile(SAVED_GAME_FILE)
            }

        }
    }

    override fun onTurnChanged(player: Int) {
        when (player) {
            PLAYER_1 -> {
                player1View.text = "Your turn"
                player2View.text = "Enemy turn"
            }
            PLAYER_2 -> {
                player1View.text = "Enemy turn"
                player2View.text = "Your turn"
            }

        }
    }

    fun clicked(cellView: TableCell) {

    }

    fun setClickListeners() {
        for (position in 0..63) {
            val cellView = table[position] as TableCell
            cellView.setOnClickListener {
                clicked(it as TableCell)
            }
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
        set(value : Skin) {
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
    val name : String
) {

    interface SkinProvider {
        var skin : Skin
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
