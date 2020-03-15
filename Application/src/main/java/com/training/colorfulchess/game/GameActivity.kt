package com.training.colorfulchess.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.gridlayout.widget.GridLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.transition.Fade
import android.view.View.INVISIBLE
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.children
import com.training.colorfulchess.ConfigurationInputStream
import com.training.colorfulchess.ConfigurationOutputStream
import com.training.colorfulchess.R
import com.training.colorfulchess.game.adapters.SkinAdapter
import com.training.colorfulchess.game.di.dagger.ChessModule
import com.training.colorfulchess.game.di.dagger.DaggerChessComponent
import com.training.colorfulchess.game.listeners.CellClickListener
import com.training.colorfulchess.game.modelvm2.ChessViewModel2
import com.training.colorfulchess.game.modelvm2.GameEndObserver
import com.training.colorfulchess.game.modelvm2.TableTransformationObserver
import com.training.colorfulchess.game.modelvm2.TurnChangedObserver
import com.training.colorfulchess.game.timer.RadioTimer
import com.training.colorfulchess.game.timer.doOnEnd
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import kotlin.collections.List

class GameActivity : AppCompatActivity(), SkinAdapter.OnSkinSelectedListener {

    companion object {
        const val SAVED_GAME_FILE = "savedGame"
        const val GAME_MODE = "gameMode"
        const val NEW_GAME = "newGame"
        const val LOAD_GAME = "loadGame"
        const val GAME_SAVED = "gameSaved"
    }

    //private lateinit var controller: TableController

    val cellSize get() = resources.getDimensionPixelSize(R.dimen.cell_size)

    private val table: GridLayout by lazy { findViewById<GridLayout>(R.id.include2) }

    private val player1View: TextView by lazy { findViewById<TextView>(R.id.player1_textview) }

    private val player2View: TextView by lazy { findViewById<TextView>(R.id.player2_textview) }

    private val timer1: RadioTimer by lazy { findViewById<RadioTimer>(R.id.player1_timer) }

    private val timer2: RadioTimer by lazy { findViewById<RadioTimer>(R.id.player2_timer) }

    private val drawerLayout: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.game_drawer) }

    private val skinRecyclerView: RecyclerView by lazy { findViewById<RecyclerView>(R.id.skin_rv) }

    private val skinAdapter: SkinAdapter by lazy { SkinAdapter() }

    private var activeTimer: Int = NONE

    private lateinit var tableObserver: TableTransformationObserver

    private lateinit var turnObserver: TurnChangedObserver

    private lateinit var gameEndObserver: GameEndObserver

    private var save = true

    private lateinit var viewModel: ChessViewModel2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val component = DaggerChessComponent.builder()
            .chessModule(ChessModule(application, this))
            .build()

        viewModel = component.viewModel
        setupObservers()
        loadGame(intent!!.getStringExtra(GAME_MODE))

        setupSkinRecyclerView()
        setupTableListeners()
        setupTransitions()
        setupTimers(intent.getIntExtra(SECONDS_PER_TURN, 0).toFloat())



    }

    private fun loadGame(gameMode: String) {
        when (gameMode) {
            NEW_GAME -> loadNewGame()
            LOAD_GAME -> loadSavedGame()
            else -> throw IllegalArgumentException("loadGame() crashed") //replace with custom exception
        }

    }

    private fun loadNewGame() {
        viewModel.deserialize(getDefaultConfigurationStream(), true)
    }

    private fun loadSavedGame() {
        val fileInput = openFileInput(SAVED_GAME_FILE)
        val configStream = ConfigurationInputStream(fileInput)
        viewModel.deserialize(configStream, false)
    }

    private fun setupSkinRecyclerView() {
        skinRecyclerView.layoutManager = LinearLayoutManager(this)
        skinAdapter.listener = this
        skinAdapter.skins = listOf(
            getSkinById(DEFAULT_SKIN_ID, this, cellSize),
            getSkinById(DARK_SKIN_ID, this ,cellSize)
        )
        skinRecyclerView.adapter = skinAdapter
    }

    private fun setupTransitions() {
        window.enterTransition = Fade().apply { duration = 250 }
        window.exitTransition = Fade().apply { duration = 100 }
    }

    private fun setupTableListeners() {
        for ((i, v) in table.children.withIndex()) {
            val cell = v as TableCell
            cell.setOnClickListener(CellClickListener(viewModel, i))
            /*cell.setOnTouchListener(CellTouchListener2(viewModel, i))
            cell.setOnDragListener(CellDragListener2(viewModel, i))*/
        }

    }

    private fun setupTimers(time: Float) {
        if(time == 0f) {
            hideTimers()
            return
        }
        timer1.timeSpan = time
        timer2.timeSpan = time

        timer1.textColor = Color.BLACK
        timer2.textColor = Color.BLACK

        timer1.warnTime = time / 4
        timer2.warnTime = time / 4

        timer1.font = ResourcesCompat.getFont(this, R.font.radio)!!
        timer2.font = ResourcesCompat.getFont(this, R.font.radio)!!

        timer1.doOnEnd {
            viewModel.switchTurn()
        }

        timer2.doOnEnd {
            viewModel.switchTurn()
        }

    }

    private fun hideTimers() {
        timer1.visibility = INVISIBLE
        timer2.visibility = INVISIBLE

        timer1.isEnabled = false
        timer2.isEnabled = false
    }

    private fun setupObservers() {
        val cells = mutableListOf<TableCell>()
        for (v in table.children)
            cells.add(v as TableCell)
        val cellSize = resources.getDimensionPixelSize(R.dimen.cell_size)
        tableObserver = TableTransformationObserver(cells, getSkinById(DEFAULT_SKIN_ID, this, cellSize))
        turnObserver = TurnChangedObserver(timer1, timer2, player1View, player2View)
        gameEndObserver = GameEndObserver(
            player1View,
            player2View,
            PreferenceManager.getDefaultSharedPreferences(this),
            timer1,
            timer2,
            table.children.map { it as TableCell }.toList()
        )

        viewModel.tableChanges.observe(this, tableObserver)
        viewModel.gameEnd.observe(this, gameEndObserver)
        viewModel.turnChange.observe(this, turnObserver)
    }

    private fun saveGame() {
        val fileOutput = openFileOutput(SAVED_GAME_FILE, Context.MODE_PRIVATE)
        val configStream = ConfigurationOutputStream(fileOutput)
        viewModel.serialize(configStream)
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(GAME_SAVED, true)
            .apply()
    }

    override fun onSkinChanged(skin: Skin) {
        tableObserver.texture = skin
        drawerLayout.closeDrawer(GravityCompat.END)

    }






    override fun onPause() {
        super.onPause()
        saveGame()

    }

}

interface TransformationProcessor {
    fun processTransformations(vararg transformations: Transformation)
}

class Skin(
    var blackPawn: Bitmap,
    var whitePawn: Bitmap,

    var blackBishop: Bitmap,
    var whiteBishop: Bitmap,

    var blackRook: Bitmap,
    var whiteRook: Bitmap,

    var blackKnight: Bitmap,
    var whiteKnight: Bitmap,

    var blackQueen: Bitmap,
    var whiteQueen: Bitmap,

    var blackKing: Bitmap,
    var whiteKing: Bitmap,

    var blackCell: Bitmap,
    var whiteCell: Bitmap,
    var selectedCell: Bitmap,
    var enemyCell: Bitmap,

    val id: Int,
    val name: String,
    val context: Context
) {

    interface SkinProvider {
        var skin: Skin
    }
}

fun defaultSkin(context: Context, cellSize: Int) = Skin(
    context.getDrawable(R.drawable.pawn_black)!!.toBitmap(),
    context.getDrawable(R.drawable.pawn_white)!!.toBitmap(),
    context.getDrawable(R.drawable.bishop_black)!!.toBitmap(),
    context.getDrawable(R.drawable.bishop_white)!!.toBitmap(),
    context.getDrawable(R.drawable.rook_black)!!.toBitmap(),
    context.getDrawable(R.drawable.rook_white)!!.toBitmap(),
    context.getDrawable(R.drawable.knight_black)!!.toBitmap(),
    context.getDrawable(R.drawable.knight_white)!!.toBitmap(),
    context.getDrawable(R.drawable.queen_black)!!.toBitmap(),
    context.getDrawable(R.drawable.queen_white)!!.toBitmap(),
    context.getDrawable(R.drawable.king_black)!!.toBitmap(),
    context.getDrawable(R.drawable.king_white)!!.toBitmap(),
    context.getDrawable(R.drawable.black_cell)!!.toBitmap(cellSize, cellSize),
    context.getDrawable(R.drawable.white_cell)!!.toBitmap(cellSize, cellSize),
    context.getDrawable(R.drawable.selected_cell)!!.toBitmap(cellSize, cellSize),
    context.getDrawable(R.drawable.enemy_cell)!!.toBitmap(cellSize, cellSize),
    id = 0,
    name = "Default Skin",
    context = context
)

fun darkSkin(context: Context, cellSize: Int) = Skin(
    context.getDrawable(R.drawable.pawn_black)!!.toBitmap(),
    context.getDrawable(R.drawable.pawn_white)!!.toBitmap(),
    context.getDrawable(R.drawable.bishop_black)!!.toBitmap(),
    context.getDrawable(R.drawable.bishop_white)!!.toBitmap(),
    context.getDrawable(R.drawable.rook_black)!!.toBitmap(),
    context.getDrawable(R.drawable.rook_white)!!.toBitmap(),
    context.getDrawable(R.drawable.knight_black)!!.toBitmap(),
    context.getDrawable(R.drawable.knight_white)!!.toBitmap(),
    context.getDrawable(R.drawable.queen_black)!!.toBitmap(),
    context.getDrawable(R.drawable.queen_white)!!.toBitmap(),
    context.getDrawable(R.drawable.king_black)!!.toBitmap(),
    context.getDrawable(R.drawable.king_white)!!.toBitmap(),
    context.getDrawable(R.drawable.dark_black_cell)!!.toBitmap(cellSize, cellSize),
    context.getDrawable(R.drawable.dark_white_cell)!!.toBitmap(cellSize, cellSize),
    context.getDrawable(R.drawable.selected_cell)!!.toBitmap(cellSize, cellSize),
    context.getDrawable(R.drawable.enemy_cell)!!.toBitmap(cellSize, cellSize),
    id = 1,
    name = "Dark Skin",
    context = context
)

const val DEFAULT_SKIN_ID = 0
const val DARK_SKIN_ID = 1
fun getSkinById(id: Int, context: Context, intrinsicCellSize: Int) =
    when (id) {
        DEFAULT_SKIN_ID -> defaultSkin(context, intrinsicCellSize)
        DARK_SKIN_ID -> darkSkin(context, intrinsicCellSize)
        else -> throw IllegalArgumentException("Skin id incorrect")
    }
