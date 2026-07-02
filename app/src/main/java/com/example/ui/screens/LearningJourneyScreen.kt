package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoLesson
import com.example.ui.AppScreen
import com.example.ui.EduVideoViewModel
import com.example.ui.components.learningjourney.*

// Enum representing the 8 stages of the Learning Journey
enum class JourneyStage(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    VIDEO("Interactive Video", "Engaging visual slide voiceover lesson", Icons.Default.PlayArrow),
    SUMMARY("AI Study Summary", "Short & detailed conceptually-rich notes", Icons.Default.Description),
    KEY_CONCEPTS("Key Concepts Explorer", "Expandable definitions of core pillars", Icons.Default.Layers),
    MIND_MAP("AI Mind Map", "Visual structural hierarchy of sub-concepts", Icons.Default.AltRoute),
    FLASHCARDS("Spaced Flashcards", "Dynamic swipe & flip study companion", Icons.Default.Style),
    QUIZ("Assessment Quiz", "Mixed MCQ, T/F, and fill-in-the-blanks test", Icons.Default.Quiz),
    PROGRESS("Learning Analytics", "Live progress, milestones and study metrics", Icons.Default.Analytics),
    RECOMMENDATION("Recommendations", "Adaptive easier, similar or advanced pathways", Icons.Default.TrendingUp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningJourneyScreen(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    modifier: Modifier = Modifier
) {
    var activeModalStage by remember { mutableStateOf<JourneyStage?>(null) }

    // Status check helpers
    val isVideoCompleted = lesson.isVideoCompleted
    val isSummaryRead = lesson.isSummaryRead
    val isFlashcardsCompleted = lesson.isFlashcardsStudied
    val isQuizCompleted = lesson.quizScore != null

    // Determine lock/current status for each stage
    fun getStageStatus(stage: JourneyStage): Triple<Boolean, Boolean, Boolean> {
        // Returns Triple(isUnlocked, isCurrent, isCompleted)
        return when (stage) {
            JourneyStage.VIDEO -> Triple(true, !isVideoCompleted, isVideoCompleted)
            JourneyStage.SUMMARY -> Triple(isVideoCompleted, isVideoCompleted && !isSummaryRead, isSummaryRead)
            JourneyStage.KEY_CONCEPTS -> Triple(isSummaryRead, false, isSummaryRead) // Unlocks after summary
            JourneyStage.MIND_MAP -> Triple(isSummaryRead, false, isSummaryRead) // Unlocks after summary
            JourneyStage.FLASHCARDS -> Triple(isSummaryRead, isSummaryRead && !isFlashcardsCompleted, isFlashcardsCompleted)
            JourneyStage.QUIZ -> Triple(isFlashcardsCompleted || isSummaryRead, (isFlashcardsCompleted || isSummaryRead) && !isQuizCompleted, isQuizCompleted)
            JourneyStage.PROGRESS -> Triple(true, false, lesson.completionPercentage == 100)
            JourneyStage.RECOMMENDATION -> Triple(true, false, lesson.completionPercentage == 100)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = lesson.topic,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "AI Learning Journey • ${lesson.studentClass}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setScreen(AppScreen.Home) },
                        modifier = Modifier.testTag("journey_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // Quick progress ring on the top bar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { lesson.completionPercentage / 100f },
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${lesson.completionPercentage}%",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    // Header Card introducing the Journey Roadmap
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ready to master this topic?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Complete each milestone step-by-step. Watching the video unlocks study materials and flashcards, building up to the final mastery quiz!",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                // Render the 8 Stages Timeline
                itemsIndexed(
                    items = JourneyStage.entries.toTypedArray(),
                    key = { _, stage -> stage.name }
                ) { index, stage ->
                    val (unlocked, current, completed) = getStageStatus(stage)
                    TimelineItem(
                        stage = stage,
                        unlocked = unlocked,
                        current = current,
                        completed = completed,
                        isLast = index == JourneyStage.entries.size - 1,
                        onCardClicked = {
                            if (unlocked) {
                                activeModalStage = stage
                            }
                        }
                    )
                }
            }

            // Interactive Modal Overlay views
            AnimatedVisibility(
                visible = activeModalStage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                activeModalStage?.let { stage ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (stage) {
                            JourneyStage.VIDEO -> VideoModalScreen(
                                viewModel = viewModel,
                                lesson = lesson,
                                onClose = { activeModalStage = null }
                            )
                            JourneyStage.SUMMARY -> SummaryModalScreen(
                                viewModel = viewModel,
                                lesson = lesson,
                                onClose = { activeModalStage = null }
                            )
                            JourneyStage.KEY_CONCEPTS -> KeyConceptsModalScreen(
                                viewModel = viewModel,
                                lesson = lesson,
                                onClose = { activeModalStage = null }
                            )
                            JourneyStage.MIND_MAP -> MindMapModalScreen(
                                viewModel = viewModel,
                                lesson = lesson,
                                onClose = { activeModalStage = null }
                            )
                            JourneyStage.FLASHCARDS -> FlashcardsModalScreen(
                                viewModel = viewModel,
                                lesson = lesson,
                                onClose = { activeModalStage = null }
                            )
                            JourneyStage.QUIZ -> QuizModalScreen(
                                viewModel = viewModel,
                                lesson = lesson,
                                onClose = { activeModalStage = null }
                            )
                            JourneyStage.PROGRESS -> ProgressModalScreen(
                                viewModel = viewModel,
                                lesson = lesson,
                                onClose = { activeModalStage = null }
                            )
                            JourneyStage.RECOMMENDATION -> RecommendationModalScreen(
                                viewModel = viewModel,
                                lesson = lesson,
                                onClose = { activeModalStage = null }
                            )
                        }
                    }
                }
            }
        }
    }
}
