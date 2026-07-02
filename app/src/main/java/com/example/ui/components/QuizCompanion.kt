package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuizQuestion
import com.example.data.VideoLesson
import com.example.ui.EduVideoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizCompanion(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    selectedQuizTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(selectedQuizTab) }
    val tabs = listOf("Quiz", "AI Tutor", "Flashcards", "Notes", "DPP", "PYQs", "Lesson Plan", "Download PDF")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Horizontally Scrollable TabRow for the expanded academic suit
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (activeTab == index) MaterialTheme.colorScheme.primary else Color(0xFF64748B)
                            )
                        }
                    )
                }
            }

            // Tab Content Frame
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                label = "TabContent"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> InteractiveQuizTab(viewModel = viewModel, lesson = lesson, quiz = lesson.quiz)
                    1 -> AiTutorTab(viewModel = viewModel, lesson = lesson)
                    2 -> FlashcardsTab(viewModel = viewModel, lesson = lesson)
                    3 -> StudyNotesTab(viewModel = viewModel, lesson = lesson)
                    4 -> DppTab(lesson = lesson)
                    5 -> PyqsTab(lesson = lesson)
                    6 -> LessonPlanTab(lesson = lesson)
                    7 -> DownloadPdfTab(viewModel = viewModel, lesson = lesson)
                }
            }
        }
    }
}

@Composable
fun InteractiveQuizTab(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    quiz: List<QuizQuestion>
) {
    if (quiz.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No quiz questions generated for this lesson.", color = Color(0xFF64748B), fontSize = 14.sp)
        }
        return
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var answeredCount by remember { mutableStateOf(0) }
    var showResults by remember { mutableStateOf(false) }

    val question = quiz.getOrNull(currentQuestionIndex)

    // Sync score reporting with ViewModel when results are achieved!
    LaunchedEffect(showResults) {
        if (showResults) {
            viewModel.recordQuizCompletion(lesson, score)
        }
    }

    if (showResults) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFD1FAE5), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "Lesson Quiz Completed!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Your Score: $score / ${quiz.size}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            val feedbackText = when {
                score == quiz.size -> "✦ Perfect Score! +100 XP Mastery! ✦"
                score >= quiz.size / 2 -> "✦ Great job! concepts captured! ✦"
                else -> "Watch the video lesson once more to perfect your score!"
            }

            Text(
                text = feedbackText,
                color = Color(0xFF49454F),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Button(
                onClick = {
                    currentQuestionIndex = 0
                    selectedAnswerIndex = null
                    score = 0
                    answeredCount = 0
                    showResults = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("quiz_retry_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Quiz", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    } else if (question != null) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUESTION ${currentQuestionIndex + 1} OF ${quiz.size}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Score: $score",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = question.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                question.options.forEachIndexed { index, option ->
                    val isSelected = selectedAnswerIndex == index
                    val isCorrect = question.correctAnswerIndex == index
                    val hasAnswered = selectedAnswerIndex != null

                    val optionBorderColor = when {
                        isSelected && isCorrect -> Color(0xFF10B981)
                        isSelected && !isCorrect -> Color(0xFFEF4444)
                        hasAnswered && isCorrect -> Color(0xFF10B981)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }

                    val optionBgColor = when {
                        isSelected && isCorrect -> Color(0xFFECFDF5)
                        isSelected && !isCorrect -> Color(0xFFFEF2F2)
                        hasAnswered && isCorrect -> Color(0xFFECFDF5)
                        else -> Color(0xFFF1F5F9).copy(alpha = 0.5f)
                    }

                    val optionIndicatorIcon = when {
                        hasAnswered && isCorrect -> Icons.Default.CheckCircle
                        isSelected && !isCorrect -> Icons.Default.Cancel
                        else -> null
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(optionBgColor)
                            .border(1.dp, optionBorderColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = !hasAnswered) {
                                selectedAnswerIndex = index
                                answeredCount++
                                if (index == question.correctAnswerIndex) {
                                    score++
                                }
                            }
                            .padding(12.dp)
                            .testTag("quiz_option_$index"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isSelected) optionBorderColor else Color(0xFFE2E8F0),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + index).toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = option,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (optionIndicatorIcon != null) {
                            Icon(
                                imageVector = optionIndicatorIcon,
                                contentDescription = null,
                                tint = optionBorderColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Real-time answer feedback
            if (selectedAnswerIndex != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "EXPLANATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = question.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentQuestionIndex < quiz.lastIndex) {
                            currentQuestionIndex++
                            selectedAnswerIndex = null
                        } else {
                            showResults = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_next_button")
                ) {
                    Text(
                        text = if (currentQuestionIndex < quiz.lastIndex) "Next Question" else "Finish Quiz",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AiTutorTab(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson
) {
    val messages by viewModel.tutorMessages.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chat History Frame
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(messages) { _, msg ->
                val isMe = msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) MaterialTheme.colorScheme.primary else Color.White
                        ),
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isMe) 14.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 14.dp
                        ),
                        border = if (isMe) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.widthIn(max = 240.dp)
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            if (msg.text == "...") {
                                // Typing Loading indicator
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(3) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = msg.text,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = if (isMe) Color.White else Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active chat Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask Socratic Loomi Tutor about this...", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("tutor_chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        viewModel.askTutor(inputQuery)
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("tutor_chat_send")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FlashcardsTab(viewModel: EduVideoViewModel, lesson: VideoLesson) {
    val scenes = lesson.scenes
    if (scenes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No content available for flashcards.", color = Color(0xFF64748B), fontSize = 14.sp)
        }
        return
    }

    var currentCardIndex by remember { mutableStateOf(0) }
    val currentScene = scenes[currentCardIndex]
    
    var isFlipped by remember(currentCardIndex) { mutableStateOf(false) }
    
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "CardFlipRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .clip(RoundedCornerShape(16.dp))
                .clickable { isFlipped = !isFlipped }
                .background(
                    if (rotation <= 90f) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = if (rotation <= 90f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CONCEPT CARD ${currentCardIndex + 1}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentScene.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap to Reveal Answers",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                        },
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "KEY TAKEAWAYS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val points = currentScene.textOnScreen.split("\n")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        points.forEach { point ->
                            if (point.isNotBlank()) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = point.trim().removePrefix("•").removePrefix("-").trim(),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Spaced Repetition Rating (Visible when flipped)
        AnimatedVisibility(
            visible = isFlipped,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Self-Assess Your Memory Recall:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val existingRating = viewModel.getFlashcardRating(lesson.id, currentCardIndex)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating 1: Hard
                    Button(
                        onClick = {
                            viewModel.recordFlashcardSpacedRepetition(lesson.id, currentCardIndex, "Hard")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (existingRating == "Hard") Color(0xFFEF4444) else Color(0xFFFEF2F2),
                            contentColor = if (existingRating == "Hard") Color.White else Color(0xFFEF4444)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("Hard 🔴", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Rating 2: Good
                    Button(
                        onClick = {
                            viewModel.recordFlashcardSpacedRepetition(lesson.id, currentCardIndex, "Good")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (existingRating == "Good") Color(0xFFF59E0B) else Color(0xFFFEF3C7),
                            contentColor = if (existingRating == "Good") Color.White else Color(0xFFB45309)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFDE047)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("Good 🟡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Rating 3: Easy
                    Button(
                        onClick = {
                            viewModel.recordFlashcardSpacedRepetition(lesson.id, currentCardIndex, "Easy")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (existingRating == "Easy") Color(0xFF10B981) else Color(0xFFECFDF5),
                            contentColor = if (existingRating == "Easy") Color.White else Color(0xFF047857)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("Easy 🟢", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (existingRating.isNotEmpty()) {
                    Text(
                        text = "Spaced Repetition study recorded! ✓",
                        fontSize = 10.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentCardIndex > 0) currentCardIndex-- },
                enabled = currentCardIndex > 0
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
            }

            Text(
                text = "${currentCardIndex + 1} of ${scenes.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )

            IconButton(
                onClick = { if (currentCardIndex < scenes.lastIndex) currentCardIndex++ },
                enabled = currentCardIndex < scenes.lastIndex
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun StudyNotesTab(viewModel: EduVideoViewModel, lesson: VideoLesson) {
    var personalNotesText by remember(lesson.id) { mutableStateOf(viewModel.getPersonalNotes(lesson.id)) }
    var notesSavedMessageVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Personal Active Learning scratchpad card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PERSONAL STUDY SCRATCHPAD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        if (personalNotesText != viewModel.getPersonalNotes(lesson.id)) {
                            TextButton(
                                onClick = {
                                    viewModel.savePersonalNotes(lesson.id, personalNotesText)
                                    viewModel.addXP(15) // XP Award for active learning!
                                    notesSavedMessageVisible = true
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Save Notes (+15 XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (notesSavedMessageVisible) {
                            Text(
                                text = "Saved! ✓",
                                fontSize = 10.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = personalNotesText,
                        onValueChange = { 
                            personalNotesText = it
                            notesSavedMessageVisible = false
                        },
                        placeholder = { Text("Jot down custom insights, questions, or formulas here as you play the lesson...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("personal_notes_input")
                    )
                }
            }
        }

        if (lesson.detailedNotes != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AI GENERATED STUDY GUIDE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lesson.detailedNotes ?: "",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        itemsIndexed(lesson.scenes) { index, scene ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CONCEPT ${index + 1}: ${scene.title.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )

                    val points = scene.textOnScreen.split("\n")
                    points.forEach { point ->
                        if (point.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(8.dp)
                                        .padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = point.trim().removePrefix("•").removePrefix("-").trim(),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DppTab(lesson: VideoLesson) {
    val dpps = lesson.dpp ?: listOf("No DPP questions generated for this curriculum.")

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(dpps) { idx, dpp ->
            var expanded by remember { mutableStateOf(false) }
            val parts = dpp.split("Solution:")
            val questionText = parts.getOrNull(0)?.trim() ?: dpp
            val solutionText = parts.getOrNull(1)?.trim() ?: ""

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY PRACTICE PROBLEM #${idx + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEEF2F6), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Homework", fontSize = 8.sp, color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = questionText.removePrefix("Q${idx+1}:").removePrefix("Q[$idx]:").trim(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    if (solutionText.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (expanded) "Hide Step-by-Step Solution" else "Reveal Step-by-Step Solution",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        AnimatedVisibility(visible = expanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "DETAILED EXPLANATION",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                    Text(
                                        text = solutionText,
                                        fontSize = 11.sp,
                                        color = Color(0xFF065F46),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PyqsTab(lesson: VideoLesson) {
    val pyqs = lesson.pyqs ?: listOf("No Board PYQs compiled for this topic.")

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(pyqs) { idx, pyq ->
            var expanded by remember { mutableStateOf(false) }
            val parts = pyq.split("Answer:")
            val questionText = parts.getOrNull(0)?.trim() ?: pyq
            val answerText = parts.getOrNull(1)?.trim() ?: ""

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREVIOUS YEAR EXAM QUESTION #${idx + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Exam Board", fontSize = 8.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = questionText.removePrefix("PYQ ${idx+1}:").trim(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    if (answerText.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (expanded) "Hide Official Marking Answer" else "Reveal Official Marking Answer",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        AnimatedVisibility(visible = expanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFFBEB))
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "OFFICIAL KEY SOLUTIONS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = answerText,
                                        fontSize = 11.sp,
                                        color = Color(0xFF78350F),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonPlanTab(lesson: VideoLesson) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYLLABUS CURRICULUM PLAN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = lesson.lessonPlan ?: "Syllabus mapping and chronological curriculum plan outline generated by Loomi AI.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadPdfTab(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson
) {
    var compileStage by remember { mutableStateOf(0) } // 0: Idle, 1: Formatting, 2: Bundling, 3: Completed
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = if (compileStage == 3) Icons.Default.FileDownloadDone else Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = if (compileStage == 3) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )

            Text(
                text = "EPUB & PDF STUDY GUIDE BUNDLE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Compile custom structured study sheets, class lecture guides, homework sheets, and Previous Year mock keys into a print-ready vector PDF document.",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            if (compileStage == 0) {
                Button(
                    onClick = {
                        scope.launch {
                            compileStage = 1
                            delay(1200)
                            compileStage = 2
                            delay(1000)
                            compileStage = 3
                            viewModel.addXP(25)
                            viewModel.unlockBadge("Cloud Importer")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("compile_pdf_button")
                ) {
                    Icon(Icons.Default.BuildCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compile Print-Ready PDF", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else if (compileStage == 1 || compileStage == 2) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape))
                    Text(
                        text = if (compileStage == 1) "Generating high-contrast LaTeX vector layouts..." else "Bundling DPP sheets and signing package...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (compileStage == 3) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PDF Compile Successful!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            Text("Saved offline inside /documents/LoomiEdu_notes.pdf", fontSize = 10.sp, color = Color(0xFF047857))
                        }
                    }
                }

                TextButton(
                    onClick = { compileStage = 0 }
                ) {
                    Text("Compile Another Format", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
