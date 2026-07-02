package com.example.ui.components.learningjourney

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuizQuestion
import com.example.data.VideoLesson
import com.example.ui.EduVideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizModalScreen(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    onClose: () -> Unit
) {
    val questions = remember(lesson.quiz) {
        lesson.quiz.ifEmpty {
            listOf(
                QuizQuestion(
                    question = "Under which board is this lesson generated?",
                    options = listOf("CBSE", "ICSE", "State Board", "IB"),
                    correctAnswerIndex = 0,
                    explanation = "This lesson was customized based on the CBSE parameters.",
                    type = "MULTIPLE_CHOICE"
                ),
                QuizQuestion(
                    question = "The virtual duration of this video lesson is ${lesson.durationMinutes} minutes.",
                    options = listOf("True", "False"),
                    correctAnswerIndex = 0,
                    explanation = "The virtual video duration matches exactly the user input.",
                    type = "TRUE_FALSE"
                )
            )
        }
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var fillInAnswerText by remember { mutableStateOf("") }
    var answerSubmitted by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentQuestionIndex]

    fun submitAnswer() {
        if (answerSubmitted) return
        
        val isCorrect = when (currentQuestion.type) {
            "FILL_IN_BLANKS" -> {
                val correctText = currentQuestion.options.getOrNull(currentQuestion.correctAnswerIndex)?.lowercase() ?: ""
                fillInAnswerText.trim().lowercase() == correctText
            }
            else -> {
                selectedAnswerIndex == currentQuestion.correctAnswerIndex
            }
        }

        if (isCorrect) {
            correctAnswersCount++
        }
        answerSubmitted = true
    }

    fun nextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            selectedAnswerIndex = null
            fillInAnswerText = ""
            answerSubmitted = false
        } else {
            val finalPercentage = (correctAnswersCount * 100) / questions.size
            viewModel.updateLessonProgress(lesson.id, newQuizScore = finalPercentage)
            quizFinished = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Conceptual Mastery Quiz", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!quizFinished) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Score: $correctAnswersCount/${questions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1) / questions.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = when (currentQuestion.type) {
                                "TRUE_FALSE" -> "True or False"
                                "FILL_IN_BLANKS" -> "Fill in the Blanks"
                                else -> "Multiple Choice"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQuestion.question,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp
                        )
                    }
                }

                when (currentQuestion.type) {
                    "FILL_IN_BLANKS" -> {
                        OutlinedTextField(
                            value = fillInAnswerText,
                            onValueChange = { if (!answerSubmitted) fillInAnswerText = it },
                            label = { Text("Type correct missing word") },
                            modifier = Modifier.fillMaxWidth().testTag("quiz_fill_blank_input"),
                            enabled = !answerSubmitted
                        )
                    }
                    else -> {
                        currentQuestion.options.forEachIndexed { index, option ->
                            val isSelected = selectedAnswerIndex == index
                            val isCorrectAnswer = index == currentQuestion.correctAnswerIndex
                            
                            Card(
                                onClick = { if (!answerSubmitted) selectedAnswerIndex = index },
                                modifier = Modifier.fillMaxWidth().testTag("quiz_option_$index"),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        answerSubmitted && isCorrectAnswer -> Color(0xFFC8E6C9)
                                        answerSubmitted && isSelected && !isCorrectAnswer -> Color(0xFFFFCDD2)
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { if (!answerSubmitted) selectedAnswerIndex = index },
                                        enabled = !answerSubmitted
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = option, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                if (answerSubmitted) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Explanation Breakdown",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentQuestion.explanation,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (!answerSubmitted) {
                            submitAnswer()
                        } else {
                            nextQuestion()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("quiz_submit_button"),
                    enabled = when (currentQuestion.type) {
                        "FILL_IN_BLANKS" -> fillInAnswerText.isNotBlank()
                        else -> selectedAnswerIndex != null
                    }
                ) {
                    Text(if (!answerSubmitted) "Submit Answer" else "Next Question")
                }

            } else {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(96.dp)
                )

                Text(
                    text = "Quiz Mastered!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                val percentage = (correctAnswersCount * 100) / questions.size
                Text(
                    text = "You scored $percentage% ($correctAnswersCount of ${questions.size} correct answers).",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.updateLessonProgress(lesson.id, newQuizScore = percentage)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("quiz_exit_button")
                ) {
                    Text("Complete Quiz")
                }
            }
        }
    }
}
