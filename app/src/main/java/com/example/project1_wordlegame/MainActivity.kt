package com.example.project1_wordlegame

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var guess1Text: TextView
    private lateinit var guess1Check: TextView // No longer used for the 'O+X' string if using colored text
    private lateinit var guess2Text: TextView
    private lateinit var guess2Check: TextView
    private lateinit var guess3Text: TextView
    private lateinit var guess3Check: TextView
    private lateinit var guessEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var restartButton: Button
    private lateinit var finalWordDisplay: TextView

    private var wordToGuess: String = ""
    private var guessCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components from XML
        guess1Text = findViewById(R.id.guess1_text)
        // guess1Check is now optional if we're directly coloring the guess1Text
        guess2Text = findViewById(R.id.guess2_text)
        // guess2Check is now optional
        guess3Text = findViewById(R.id.guess3_text)
        // guess3Check is now optional
        guessEditText = findViewById(R.id.guess_edit_text)
        submitButton = findViewById(R.id.submit_button)
        restartButton = findViewById(R.id.restart_button)
        finalWordDisplay = findViewById(R.id.final_word_display)

        // Start a new game when the app opens
        startNewGame()

        // Handle button clicks
        submitButton.setOnClickListener {
            checkUserGuess()
        }

        restartButton.setOnClickListener {
            startNewGame()
        }
    }

    private fun startNewGame() {
        wordToGuess = FourLetterWordList.getRandomFourLetterWord()
        guessCount = 0

        // Reset UI elements
        guess1Text.text = "Guess #1"
        guess1Text.setTextColor(Color.BLACK) // Reset to default color
        // guess1Check.text = "" // If still using the 'O+X' check
        guess2Text.text = "Guess #2"
        guess2Text.setTextColor(Color.BLACK)
        // guess2Check.text = ""
        guess3Text.text = "Guess #3"
        guess3Text.setTextColor(Color.BLACK)
        // guess3Check.text = ""
        guessEditText.setText("")
        finalWordDisplay.visibility = View.GONE
        submitButton.visibility = View.VISIBLE
        submitButton.isEnabled = true
        guessEditText.isEnabled = true
        restartButton.visibility = View.GONE

        // Hide keyboard if it was open
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(guessEditText.windowToken, 0)
    }

    private fun checkUserGuess() {
        val guess = guessEditText.text.toString().uppercase()

        // Input validation as per stretch goals
        if (guess.length != 4 || !guess.matches("[A-Z]+".toRegex())) {
            Toast.makeText(this, "Please enter a valid 4-letter alphabetical word.", Toast.LENGTH_SHORT).show()
            return
        }

        guessCount++
        val result = checkGuess(guess, wordToGuess) // Pass wordToGuess to checkGuess
        updateUI(guess, result)

        // Hide keyboard and clear EditText after guess
        guessEditText.setText("")
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(guessEditText.windowToken, 0)

        if (guess == wordToGuess) {
            endGame(true)
        } else if (guessCount >= 3) {
            endGame(false)
        }
    }

    private fun updateUI(guess: String, result: String) {
        val coloredGuess = getColoredSpannable(guess, result)

        when (guessCount) {
            1 -> {
                guess1Text.text = coloredGuess
            }
            2 -> {
                guess2Text.text = coloredGuess
            }
            3 -> {
                guess3Text.text = coloredGuess
            }
        }
    }

    private fun endGame(isWin: Boolean) {
        submitButton.isEnabled = false
        guessEditText.isEnabled = false
        submitButton.visibility = View.GONE // Hide submit button
        restartButton.visibility = View.VISIBLE // Show restart button
        finalWordDisplay.visibility = View.VISIBLE
        finalWordDisplay.text = "The word was: $wordToGuess"

        if (isWin) {
            Toast.makeText(this, "You Won! 🎉", Toast.LENGTH_LONG).show()
            // Optional: Add a star icon or confetti animation here
            //
        } else {
            Toast.makeText(this, "Game Over. 😔", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Parameters / Fields:
     * wordToGuess : String - the target word the user is trying to guess
     * guess : String - what the user entered as their guess
     *
     * Returns a String of 'O', '+', and 'X', where:
     * 'O' represents the right letter in the right place
     * '+' represents the right letter in the wrong place
     * 'X' represents a letter not in the target word
     */
    private fun checkGuess(guess: String, wordToGuess: String): String {
        val result = CharArray(4)
        val wordToGuessChars = wordToGuess.toMutableList()

        // First pass: Find exact matches (O) and mark them
        for (i in 0 until 4) {
            if (guess[i] == wordToGuess[i]) {
                result[i] = 'O'
                wordToGuessChars[i] = ' ' // Mark as used
            }
        }

        // Second pass: Find misplaced letters (+)
        for (i in 0 until 4) {
            if (result[i] != 'O') { // Only check if not already an exact match
                if (wordToGuessChars.contains(guess[i])) {
                    result[i] = '+'
                    val index = wordToGuessChars.indexOf(guess[i])
                    wordToGuessChars[index] = ' ' // Mark as used
                } else {
                    result[i] = 'X'
                }
            }
        }
        return String(result)
    }

    /**
     * Creates a SpannableStringBuilder to color individual characters of the guess.
     * 'O' -> Green
     * '+' -> Orange
     * 'X' -> Gray
     */
    private fun getColoredSpannable(guess: String, result: String): SpannableStringBuilder {
        val sb = SpannableStringBuilder(guess)
        for (i in guess.indices) {
            val color = when (result[i]) {
                'O' -> Color.GREEN
                '+' -> Color.parseColor("#FFA500") // Orange
                else -> Color.GRAY
            }
            val foregroundColorSpan = ForegroundColorSpan(color)
            sb.setSpan(foregroundColorSpan, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return sb
    }
}