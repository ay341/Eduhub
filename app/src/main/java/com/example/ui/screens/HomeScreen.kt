package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VideoLesson
import com.example.ui.AppScreen
import com.example.ui.EduVideoViewModel
import com.example.ui.components.homescreen.*
import com.example.ui.theme.DesignSystem

private val ClassLevels = listOf("Elementary", "Middle School", "High School", "College", "General")
private val Durations = listOf(5, 10, 20, 30, 45, 60)
private val Subjects = listOf("Science", "Physics", "Chemistry", "Biology", "History", "Maths", "Computer Science", "Geography")
private val Boards = listOf("CBSE", "ICSE", "State Board", "International", "University")
private val Languages = listOf("English", "Hindi", "Spanish", "French", "German", "Japanese")
private val Difficulties = listOf("Beginner", "Intermediate", "Advanced")
private val TeachingStyles = listOf("Visual Storyteller", "Socratic Tutor", "Sarcastic/Funny", "Gamified")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: EduVideoViewModel,
    recentLessons: List<VideoLesson>,
    modifier: Modifier = Modifier
) {
    val topicState by viewModel.topic.collectAsState()
    val studentClassState by viewModel.studentClass.collectAsState()
    val durationState by viewModel.durationMinutes.collectAsState()
    val subjectState by viewModel.subject.collectAsState()
    val boardState by viewModel.board.collectAsState()
    val languageState by viewModel.language.collectAsState()
    val difficultyState by viewModel.difficulty.collectAsState()
    val teachingStyleState by viewModel.teachingStyle.collectAsState()

    // Smart Import States
    val importUrlState by viewModel.importUrl.collectAsState()
    val isUploadingState by viewModel.isUploading.collectAsState()
    val uploadedFileNameState by viewModel.uploadedFileName.collectAsState()

    // User States
    val userName by viewModel.userName.collectAsState()
    val userXP by viewModel.userXP.collectAsState()
    val userStreak by viewModel.userStreak.collectAsState()
    val completedLessons by viewModel.completedLessonsCount.collectAsState()
    val currentLevel = (userXP / 500) + 1

    // Creation Modes Tab State
    var selectedGeneratorTab by remember { mutableStateOf(0) } // 0: AI Generator, 1: Smart Content Import

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(DesignSystem.Spacing.Default),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.SectionSpacing)
    ) {
        // --- 1. USER PROFILE HEADER & CHIP ---
        GreetingSection(
            userName = userName,
            currentLevel = currentLevel,
            userXP = userXP,
            userStreak = userStreak,
            onProfileClick = { viewModel.setScreen(AppScreen.Profile) },
            onDashboardClick = { viewModel.setScreen(AppScreen.Dashboard) },
            onLibraryClick = { viewModel.setScreen(AppScreen.Library) }
        )

        // --- 2. HERO CONTINUE LEARNING / BANNER ---
        if (recentLessons.isNotEmpty()) {
            val lastLesson = recentLessons.first()
            ContinueLearningSection(
                lastLesson = lastLesson,
                onResumeClick = { viewModel.selectLesson(lastLesson) }
            )
        } else {
            HeroGenerateCard()
        }

        // --- 3. DAILY GOAL PROGRESS RING & STATS ---
        DailyChallengeCard(completedLessons = completedLessons)

        LearningStatisticsSection(
            userXP = userXP,
            completedLessons = completedLessons,
            currentLevel = currentLevel
        )

        // --- 4. ADVANCED GENERATION & IMPORT SUITE (TABBED PANEL) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = DesignSystem.Shapes.CardHuge,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignSystem.Spacing.Default),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Default)
            ) {
                
                // Segmented Tabs
                TabRow(
                    selectedTabIndex = selectedGeneratorTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedGeneratorTab])
                        )
                    }
                ) {
                    Tab(
                        selected = selectedGeneratorTab == 0,
                        onClick = { selectedGeneratorTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Generator", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedGeneratorTab == 1,
                        onClick = { selectedGeneratorTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Smart Import", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // --- TAB CONTENT 0: AI GENERATOR ---
                if (selectedGeneratorTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        
                        // Class
                        Text(
                            text = "STUDENT CLASS LEVEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ClassLevels.forEach { level ->
                                val isSel = studentClassState == level
                                FilterChip(
                                    selected = isSel,
                                    onClick = { viewModel.studentClass.value = level },
                                    label = { Text(level, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("class_chip_$level")
                                )
                            }
                        }

                        // Subject & Board
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SUBJECT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = subjectState,
                                    onValueChange = { viewModel.subject.value = it },
                                    placeholder = { Text("e.g. Physics") },
                                    singleLine = true,
                                    shape = DesignSystem.Shapes.CardMedium,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.background,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SCHOOL BOARD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = boardState,
                                    onValueChange = { viewModel.board.value = it },
                                    placeholder = { Text("e.g. CBSE") },
                                    singleLine = true,
                                    shape = DesignSystem.Shapes.CardMedium,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.background,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Language & Difficulty
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LANGUAGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = languageState,
                                    onValueChange = { viewModel.language.value = it },
                                    placeholder = { Text("e.g. English") },
                                    singleLine = true,
                                    shape = DesignSystem.Shapes.CardMedium,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.background,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DIFFICULTY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                FlowRow {
                                    Difficulties.forEach { diff ->
                                        val isSel = difficultyState == diff
                                        SuggestionChip(
                                            onClick = { viewModel.difficulty.value = diff },
                                            label = { Text(diff, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                            ),
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Teaching Style Choice
                        Text(
                            text = "TEACHING STYLE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TeachingStyles.forEach { style ->
                                val isSel = teachingStyleState == style
                                FilterChip(
                                    selected = isSel,
                                    onClick = { viewModel.teachingStyle.value = style },
                                    label = { Text(style, fontSize = 10.sp) }
                                )
                            }
                        }

                        // Duration slider or chip selection
                        Text(
                            text = "TIMELINE VIDEO DURATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Durations.forEach { minutes ->
                                val isSel = durationState == minutes
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background
                                    ),
                                    shape = DesignSystem.Shapes.CardSmall,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.durationMinutes.value = minutes }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "$minutes", fontSize = 14.sp, fontWeight = FontWeight.Black)
                                            Text(text = "MIN", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Learning Topic Input
                        Text(
                            text = "LESSON TOPIC",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        OutlinedTextField(
                            value = topicState,
                            onValueChange = { viewModel.topic.value = it },
                            placeholder = { Text("e.g. Photosynthesis, Newton's Laws, French Revolution...", color = Color(0xFF64748B)) },
                            leadingIcon = { Icon(Icons.Default.SmartButton, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            shape = DesignSystem.Shapes.CardMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("topic_input")
                        )
                    }
                }

                // --- TAB CONTENT 1: SMART CONTENT IMPORT ---
                if (selectedGeneratorTab == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "IMPORT ACADEMIC LINKS (YOUTUBE / SOCIAL)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        OutlinedTextField(
                            value = importUrlState,
                            onValueChange = { viewModel.importUrl.value = it },
                            placeholder = { Text("Paste YouTube, Instagram, or Facebook URL here...") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                if (importUrlState.isNotBlank()) {
                                    IconButton(onClick = { viewModel.importUrl.value = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = DesignSystem.Shapes.CardMedium,
                            modifier = Modifier.fillMaxWidth().testTag("import_url_input")
                        )

                        Text(
                            text = "OR UPLOAD STUDY DOCUMENTS / RECORDINGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        // File upload grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UploadCard(
                                title = "Upload PDF",
                                icon = Icons.Default.PictureAsPdf,
                                color = Color(0xFFEF4444),
                                onClick = { viewModel.triggerSimulatedUpload("Photosynthesis_Class_Syllabus.pdf") },
                                modifier = Modifier.weight(1f)
                            )
                            UploadCard(
                                title = "Upload Doc",
                                icon = Icons.Default.Description,
                                color = Color(0xFF3B82F6),
                                onClick = { viewModel.triggerSimulatedUpload("World_History_Notes.docx") },
                                modifier = Modifier.weight(1f)
                            )
                            UploadCard(
                                title = "Voice Audio",
                                icon = Icons.Default.AudioFile,
                                color = Color(0xFF10B981),
                                onClick = { viewModel.triggerSimulatedUpload("Biology_Lecture_Recording.mp3") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Animated uploading states
                        UploadingIndicator(isVisible = isUploadingState)

                        AnimatedVisibility(visible = uploadedFileNameState.isNotEmpty() && !isUploadingState) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(DesignSystem.Shapes.CardMedium)
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFF10B981), DesignSystem.Shapes.CardMedium)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Import Successful!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = "Source: $uploadedFileNameState",
                                        fontSize = 11.sp,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 5. ACTION INITIATE BUILDER BUTTON ---
                Button(
                    onClick = { viewModel.generateVideoLesson() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = DesignSystem.Shapes.CardLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MovieCreation,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedGeneratorTab == 1) "Compile Import Source" else "Generate Video Lesson",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // --- 5. QUICK SHORTCUTS & CHANNELS ---
        AiToolsGrid(
            onTutorClick = {
                if (recentLessons.isNotEmpty()) {
                    viewModel.selectLesson(recentLessons.first())
                } else {
                    viewModel.topic.value = "Quantum Physics"
                    viewModel.generateVideoLesson()
                }
            },
            onVaultClick = { viewModel.setScreen(AppScreen.Library) }
        )

        // --- 6. HISTORIC GENERATED LECTURES Vault ---
        RecentActivitySection(
            recentLessons = recentLessons,
            onLessonSelect = { viewModel.selectLesson(it) },
            onSeeAllClick = { viewModel.setScreen(AppScreen.Library) }
        )
    }
}
