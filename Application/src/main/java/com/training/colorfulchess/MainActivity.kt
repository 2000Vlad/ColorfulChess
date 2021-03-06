package com.training.colorfulchess

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.training.colorfulchess.game.GameActivity
import com.training.colorfulchess.game.GameActivity.Companion.GAME_MODE
import com.training.colorfulchess.game.GameActivity.Companion.GAME_SAVED
import com.training.colorfulchess.game.GameActivity.Companion.LOAD_GAME
import com.training.colorfulchess.game.GameActivity.Companion.NEW_GAME
import com.training.colorfulchess.game.GameOptionsActivity

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
       


    }

    fun launchNewGame(v: View) {
        val intent = Intent(this, GameOptionsActivity::class.java)
        startActivity(intent)

    }


    fun loadGame(v: View) {
        val hasSaved = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(GAME_SAVED, false)

        if (!hasSaved) {
            Toast.makeText(this, "There's no game to load", LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, GameActivity::class.java)
        val extras = Bundle()
        extras.putString(GAME_MODE, LOAD_GAME)
        intent.putExtras(extras)
        val options = ActivityOptions.makeSceneTransitionAnimation(this)
        startActivity(intent, options.toBundle())


    }
}
