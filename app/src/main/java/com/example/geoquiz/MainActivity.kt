package com.example.geoquiz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.geoquiz.databinding.ActivityMainBinding

// Keys used to save/restore quiz state across configuration changes (e.g. rotation)
private const val KEY_CURRENT_INDEX = "currentIndex"
private const val KEY_CORRECT_COUNT = "correctCount"
private const val KEY_ANSWERED_COUNT = "answeredCount"
private const val KEY_ANSWERED_ARRAY = "answeredArray"
private const val KEY_CHEATED_ARRAY = "cheatedArray"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // The question bank. Add more Question(...) entries here to expand the quiz.
    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, true),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, false)
    )

    private var currentIndex = 0
    private var correctCount = 0
    private var answeredCount = 0

    // Tracks, per question index, whether the user peeked at the answer via CheatActivity.
    // Cheated questions still count as "answered" but are never counted as correct.
    private val cheatedOnQuestion = BooleanArray(questionBank.size)

    // Modern replacement for startActivityForResult; handles the CheatActivity result.
    private val cheatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val didCheat = CheatActivity.wasAnswerShown(result.data)
            if (didCheat) {
                cheatedOnQuestion[currentIndex] = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restore quiz state after rotation / process recreation.
        savedInstanceState?.let { state ->
            currentIndex = state.getInt(KEY_CURRENT_INDEX, 0)
            correctCount = state.getInt(KEY_CORRECT_COUNT, 0)
            answeredCount = state.getInt(KEY_ANSWERED_COUNT, 0)

            state.getBooleanArray(KEY_ANSWERED_ARRAY)?.forEachIndexed { i, answered ->
                if (i < questionBank.size) questionBank[i].answered = answered
            }
            state.getBooleanArray(KEY_CHEATED_ARRAY)?.forEachIndexed { i, cheated ->
                if (i < cheatedOnQuestion.size) cheatedOnQuestion[i] = cheated
            }
        }

        binding.trueButton.setOnClickListener { checkAnswer(true) }
        binding.falseButton.setOnClickListener { checkAnswer(false) }

        binding.nextButton.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        binding.prevButton.setOnClickListener {
            currentIndex = (currentIndex - 1 + questionBank.size) % questionBank.size
            updateQuestion()
        }

        binding.questionTextView.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        binding.cheatButton.setOnClickListener {
            val answerIsTrue = questionBank[currentIndex].answer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            cheatLauncher.launch(intent)
        }

        updateQuestion()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt(KEY_CURRENT_INDEX, currentIndex)
        savedInstanceState.putInt(KEY_CORRECT_COUNT, correctCount)
        savedInstanceState.putInt(KEY_ANSWERED_COUNT, answeredCount)
        savedInstanceState.putBooleanArray(
            KEY_ANSWERED_ARRAY,
            questionBank.map { it.answered }.toBooleanArray()
        )
        savedInstanceState.putBooleanArray(KEY_CHEATED_ARRAY, cheatedOnQuestion)
    }

    private fun updateQuestion() {
        val question = questionBank[currentIndex]
        binding.questionTextView.setText(question.textResId)

        // Disable the answer buttons once a question has already been answered,
        // so the user can't inflate their score by answering twice.
        binding.trueButton.isEnabled = !question.answered
        binding.falseButton.isEnabled = !question.answered

        updateScoreDisplay()
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val question = questionBank[currentIndex]
        if (question.answered) return

        question.answered = true
        answeredCount++
        binding.trueButton.isEnabled = false
        binding.falseButton.isEnabled = false

        val messageResId = when {
            cheatedOnQuestion[currentIndex] -> R.string.judgment_toast
            userAnswer == question.answer -> {
                correctCount++
                R.string.correct_toast
            }
            else -> R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
        updateScoreDisplay()

        if (answeredCount == questionBank.size) {
            showFinalScore()
        }
    }

    private fun updateScoreDisplay() {
        binding.scoreTextView.text =
            getString(R.string.score_format, correctCount, questionBank.size)
    }

    private fun showFinalScore() {
        val percent = (correctCount * 100) / questionBank.size
        Toast.makeText(
            this,
            getString(R.string.score_format, correctCount, questionBank.size) +
                " ($percent%)",
            Toast.LENGTH_LONG
        ).show()
    }
}
