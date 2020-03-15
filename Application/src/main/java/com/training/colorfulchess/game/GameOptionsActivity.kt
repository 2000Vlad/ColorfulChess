package com.training.colorfulchess.game

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.training.colorfulchess.R
import com.training.colorfulchess.game.timer.RadioTimer
import com.training.colorfulchess.game.timer.SECONDS_PER_MINUTE

class GameOptionsActivity : AppCompatActivity() {

    private val timerIntervals = arrayOf(1, 5, 10, 20)

    private val timerCheckBox : CheckBox by lazy { findViewById<CheckBox>(R.id.timer_cb) }
    private val timerBar : SeekBar by lazy { findViewById<SeekBar>(R.id.timer_sb) }
    private val timerText : TextView by lazy { findViewById<TextView>(R.id.timer_tv)}
    private val confirmButton : Button by lazy { findViewById<Button>(R.id.confirm_button) }
    private var timers = false
    private var time = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_options)



        timerBar.visibility = INVISIBLE
        timerCheckBox.isChecked = false
        timerCheckBox.setOnCheckedChangeListener {_, isChecked ->
            if(isChecked) {
                timerText.visibility = VISIBLE
                timerBar.visibility = VISIBLE
                timerBar.progress = 0
                timerText.text = "1 min"
                time = SECONDS_PER_MINUTE
                val a = Canvas()
                val p = Paint()

            }
            else {
                timerText.visibility = INVISIBLE
                timerBar.visibility = INVISIBLE
                time = 0L;
            }
        }
        timerBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                time = timerIntervals[progress] * SECONDS_PER_MINUTE
                val unit = if(progress == 0)" min" else " mins"
                timerText.text = timerIntervals[progress].toString().plus(unit)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        confirmButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            val extras = Bundle()
            extras.putString(GameActivity.GAME_MODE, GameActivity.NEW_GAME)
            if(timerCheckBox.isChecked)
            extras.putInt(SECONDS_PER_TURN, timerIntervals[timerBar.progress])
            else extras.putInt(SECONDS_PER_TURN, 0)
            intent.putExtras(extras)
            val options = ActivityOptions.makeSceneTransitionAnimation(this)
            startActivity(intent, options.toBundle())
        }

    }
}
//These are the flags that GameActivity will use to determine it's options not actual values
const val SECONDS_PER_TURN = "secsPerTurn"


