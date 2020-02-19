package com.training.colorfulchess.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
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
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.training.colorfulchess.ConfigurationInputStream
import com.training.colorfulchess.ConfigurationOutputStream
import com.training.colorfulchess.R
import com.training.colorfulchess.game.adapters.SkinAdapter
import com.training.colorfulchess.game.di.dagger.ChessModule
import com.training.colorfulchess.game.di.dagger.DaggerChessComponent
import com.training.colorfulchess.game.listeners.CellClickListener
import com.training.colorfulchess.game.listeners.CellDragListener2
import com.training.colorfulchess.game.listeners.CellTouchListener2
import com.training.colorfulchess.game.modelvm2.ChessViewModel2
import com.training.colorfulchess.game.modelvm2.GameEndObserver
import com.training.colorfulchess.game.modelvm2.TableTransformationObserver
import com.training.colorfulchess.game.modelvm2.TurnChangedObserver
import com.training.colorfulchess.game.timer.GameTimer
import com.training.colorfulchess.game.timer.RadioTimer
import com.training.colorfulchess.game.timer.doOnEnd
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import kotlin.collections.List
import kotlin.jvm.internal.Ref

class GameActivity : AppCompatActivity(), SkinAdapter.OnSkinSelectedListener {

    companion object {
        const val SAVED_GAME_FILE = "savedGame"
        const val GAME_MODE = "gameMode"
        const val NEW_GAME = "newGame"
        const val LOAD_GAME = "loadGame"
        const val GAME_SAVED = "gameSaved"
    }

    //private lateinit var controller: TableController

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
        setupTimers(20f)



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
            getSkinById(DEFAULT_SKIN_ID, this),
            getSkinById(DARK_SKIN_ID, this)
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

    }

    private fun setupObservers() {
        val cells = mutableListOf<TableCell>()
        for (v in table.children)
            cells.add(v as TableCell)
        tableObserver = TableTransformationObserver(cells, getSkinById(DEFAULT_SKIN_ID, this))
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

    interface ActionProvider {
        fun getTransformations(position: Int): List<Transformation>
        fun getActionData(): ActionData?
    }

    interface GameSerializer {
        fun serialize(stream: FileOutputStream)
    }
}

interface TransformationProcessor {
    fun processTransformations(vararg transformations: Transformation)
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
    val name: String,
    val context: Context
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
    name = "Default Skin",
    context = context
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
    name = "Dark Skin",
    context = context
)

const val DEFAULT_SKIN_ID = 0
const val DARK_SKIN_ID = 1
fun getSkinById(id: Int, context: Context) =
    when (id) {
        DEFAULT_SKIN_ID -> defaultSkin(context)
        DARK_SKIN_ID -> darkSkin(context)
        else -> throw IllegalArgumentException("Skin id incorrect")
    }
