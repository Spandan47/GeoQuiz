package com.example.geoquiz

import androidx.annotation.StringRes

/**
 * Represents a single true/false geography question.
 *
 * @param textResId string resource id for the question text
 * @param answer the correct answer (true or false)
 * @param answered whether the user has already answered this question
 */
data class Question(
    @StringRes val textResId: Int,
    val answer: Boolean,
    var answered: Boolean = false
)
