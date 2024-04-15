package com.example.assignment3_kotlin

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import java.text.DecimalFormat
import java.util.Random

/**
 * Game fragment
 */
class Game : Fragment() {
    private var tilesCount = 4
    private var clickCount = 0
    private var roundCount = 0
    private var points = 10
    private var pointsMultiple = 1
    private lateinit var gameName: TextView
    private lateinit var round: TextView
    private lateinit var tiles: TextView
    private lateinit var time: TextView
    private lateinit var score: TextView
    private var scoreValue = 0
    private var enabled = true
    private var cells = ArrayList<TextView>()
    private var countDownTimer: CountDownTimer? = null
    private var homeTimer: CountDownTimer? = null
    private val seconds3 = 3000L
    private val seconds5 = 5000L
    private val shared = "passHighscore"

    /**
     * Create the game fragment view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        initializeViews(view)
        setupGame()
        return view
    }

    /**
     * Initialize game view variables
     */
    private fun initializeViews(view: View) {
        gameName = view.findViewById(R.id.gameName)
        time = view.findViewById(R.id.time)
        score = view.findViewById(R.id.score)
        round = view.findViewById(R.id.round)
        tiles = view.findViewById(R.id.tiles)
        for (i in 0..5) {
            for (j in 0..5) {
                val resId = resources.getIdentifier("r$i"+"c$j", "id", requireActivity().packageName)
                val cell = view.findViewById<TextView>(resId)
                cells.add(cell)
            }
        }
    }

    /**
     * Initial round start
     */
    private fun setupGame() {
        setPlayerName()
        startRound()
    }

    /**
     * Get player name from navigationView and set to TextView
     */
    private fun setPlayerName() {
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.nav_view)
        val navName = navigationView.getHeaderView(0).findViewById<TextView>(R.id.name)
        gameName.text = navName.text.toString()
    }

    /**
     * Start round (reset tiles, timers & clicks; tile & point increase logic, generate random tiles, disable clicking for 3 seconds)
     */
    private fun startRound() {
        cancelHomeTimer()
        updateScoreInNavigationView(scoreValue)
        clickCount = 0
        round.text = roundCount.toString()
        if (roundCount % 3 == 0 && roundCount != 0) {
            pointsMultiple++
            tilesCount++
        }
        tiles.text = tilesCount.toString()
        enabled = false
        // Clear previous randomIndices
        for (cell in cells) {
            cell?.let {
                it.text = ""
            }
        }
        // Set new randomIndices
        val randomIndices = generateRandomIndices(tilesCount)
        highlightCells(randomIndices)
        startCountDownTimer(seconds3)
    }

    /**
     * Generate random pattern of tiles to be memorized
     */
    private fun generateRandomIndices(count: Int): ArrayList<Int> {
        val indices = ArrayList<Int>()
        val random = Random()
        while (indices.size < count) {
            val randomIndex = random.nextInt(36)
            if (!indices.contains(randomIndex)) {
                indices.add(randomIndex)
            }
        }
        return indices
    }

    /**
     * For all tiles, if tile clicked, change background to orange
     */
    private fun highlightCells(indices: ArrayList<Int>) {
        // Highlight all cells
        for (index in indices) {
            val cell = cells.getOrNull(index)
            cell?.setBackgroundResource(R.drawable.cell_clicked)
            cell?.text = "0" 
        }
    }

    /**
     * Start the 3 second memorization timer
     */
    private fun startCountDownTimer(duration: Long) {
        countDownTimer = object : CountDownTimer(duration, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsUntilFinished = millisUntilFinished / 1000.0
                val df = DecimalFormat("#.#")
                time.text = df.format(secondsUntilFinished)
            }

            override fun onFinish() {
                time.text = "0"
                if (duration == seconds3) {
                    revertCellsToDefaultBackground()
                    enableCellClickListeners()
                    enabled = true
                    startHomeTimer()
                }
            }
        }.start()
    }

    /**
     * Start the 5 second clickable timer, if time runs out: update highscore
     */
    private fun startHomeTimer() {
        homeTimer = object : CountDownTimer(seconds5, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsUntilFinished = millisUntilFinished / 1000.0
                val df = DecimalFormat("#.#")
                time.text = df.format(secondsUntilFinished)
            }

            override fun onFinish() {
                if (!TextUtils.isEmpty(gameName.text.toString())) {
                    updateHighScores(gameName.text.toString(), scoreValue)
                }
                homeLink()
            }
        }.start()
    }

    /**
     * Cancel 5 second timer (new round start & incorrect click)
     */
    private fun cancelHomeTimer() {
        homeTimer?.cancel()
    }

    /**
     * Change all tiles to original blue colour (new round start & start of copy phase of game)
     */
    private fun revertCellsToDefaultBackground() {
        for (cell in cells) {
            cell?.setBackgroundResource(R.drawable.cell_background) // Safely set background resource
        }
    }

    /**
     * Only allow tiles to be clicked if clicking is enabled
     */
    private fun enableCellClickListeners() {
        for (cell in cells) {
            cell?.setOnClickListener {
                if (enabled) {
                    handleCellClick(cell)
                }
            }
        }
    }

    /**
     * Click logic during copy phase: If invisible "0" marker and blue tile --> change to orange
     * If number of clicks = amount of tiles --> score point & start new round
     * If incorrect click --> game ends: highscore updated & go to home fragment
     */
    private fun handleCellClick(cell: TextView) {
        if (cell.text == "0" && cell.background.constantState == resources.getDrawable(R.drawable.cell_background).constantState) {
            cell.setBackgroundResource(R.drawable.cell_clicked)
            clickCount++
            if (clickCount == tilesCount) {
                scoreValue += points * pointsMultiple
                score.text = scoreValue.toString()
                revertCellsToDefaultBackground()
                roundCount++
                cancelHomeTimer()
                startRound()
            }
        } else {
            cancelHomeTimer()
            if (!TextUtils.isEmpty(gameName.text.toString())) {
                updateHighScores(gameName.text.toString(), scoreValue)
            }
            homeLink()
        }
    }

    /**
     * Updates player score in navigationView
     */
    private fun updateScoreInNavigationView(scoreValue: Int) {
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.nav_view)
        val textViewScore = navigationView.getHeaderView(0).findViewById<TextView>(R.id.highScore)
        textViewScore.text = scoreValue.toString()
    }

    /**
     * Updates player name & score in highscore fragment if greater than current top 3 scores
     */
    private fun updateHighScores(playerName: String, newScore: Int) {
        // Prevents crash when changing fragments via menu
        if (activity == null) {
            Log.e("updateHighScores", "Fragment is not attached to activity")
            return
        }

        // Necessary to pass & "save" player name & score
        val editor = requireActivity().getSharedPreferences(shared, Context.MODE_PRIVATE).edit()

        val score1 = requireActivity().getSharedPreferences(shared, Context.MODE_PRIVATE).getInt("score1", 0)
        val score2 = requireActivity().getSharedPreferences(shared, Context.MODE_PRIVATE).getInt("score2", 0)
        val score3 = requireActivity().getSharedPreferences(shared, Context.MODE_PRIVATE).getInt("score3", 0)
        val player1 = requireActivity().getSharedPreferences(shared, Context.MODE_PRIVATE).getString("player1", "")
        val player2 = requireActivity().getSharedPreferences(shared, Context.MODE_PRIVATE).getString("player2", "")

        // Switch name and score editor logic
        when {
            newScore > score1 -> {
                editor.putInt("score3", score2)
                editor.putString("player3", player2)
                editor.putInt("score2", score1)
                editor.putString("player2", player1)
                editor.putInt("score1", newScore)
                editor.putString("player1", playerName)
            }
            newScore > score2 -> {
                editor.putInt("score3", score2)
                editor.putString("player3", player2)

                editor.putInt("score2", newScore)
                editor.putString("player2", playerName)
            }
            newScore > score3 -> {
                editor.putInt("score3", newScore)
                editor.putString("player3", playerName)
            }
        }
        editor.apply()
    }

    /**
     * Game over sequence: reset timer & go to home fragment
     */
    private fun homeLink() {
        // if(isAdded()) --> Prevents crash; check if fragment is added to activity
        if (isAdded) {
            countDownTimer?.cancel()
            time.text = "0"
            val homeFragment = Home()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}