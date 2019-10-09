package com.training.colorfulchess

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.lifecycle.ViewModelProviders
import com.training.colorfulchess.game.GameActivity
import com.training.colorfulchess.game.GameActivity.Companion.GAME_MODE
import com.training.colorfulchess.game.GameActivity.Companion.GAME_SAVED
import com.training.colorfulchess.game.GameActivity.Companion.LOAD_GAME
import com.training.colorfulchess.game.GameActivity.Companion.NEW_GAME
import com.training.colorfulchess.game.TableCell
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    var pawn = false
    lateinit var cell: TableCell
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("CELL_DIM",resources.getInteger(R.integer.cellDimensionDp).toString())
        Log.e("SCREEN_WIDTH", resources.displayMetrics.widthPixels.toString())
        Log.e("SCREEN_HEIGHT", resources.displayMetrics.heightPixels.toString())
        Log.e("SCREEN_DENSITY", resources.displayMetrics.density.toString())


    }

    fun launchNewGame(v: View) {
        val intent = Intent(this, GameActivity::class.java)
        val extras = Bundle()
        extras.putString(GAME_MODE,NEW_GAME)
        intent.putExtras(extras)
        startActivity(intent)
    }


    fun loadGame(v: View) {
       val hasSaved = PreferenceManager.getDefaultSharedPreferences(this)
           .getBoolean(GAME_SAVED, false)

        if(!hasSaved){
            Toast.makeText(this,"There's no game to load",LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, GameActivity::class.java)
        val extras = Bundle()
        extras.putString(GAME_MODE, LOAD_GAME)
        intent.putExtras(extras)
        startActivity(intent)


    }
}
