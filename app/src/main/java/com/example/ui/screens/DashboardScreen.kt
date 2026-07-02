package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.EduVideoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: EduVideoViewModel,
    modifier: Modifier = Modifier
) {
    // User States
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userXP by viewModel.userXP.collectAsState()
    val userStreak by viewModel.userStreak.collectAsState()
    val completedLessons by viewModel.completedLessonsCount.collectAsState()
    val studyTimeMinutes by viewModel.totalStudyTimeMinutes.collectAsState()
    val avgQuizScore by viewModel.averageQuizScorePercent.collectAsState()
    val unlockedBadges by viewModel.unlockedBadges.collectAsState()

    // Derived level math: e.g. 500 XP per level
    val currentLevel = (userXP / 500) + 1
    val currentLevelMinXp = (currentLevel - 1) * 500
    val levelProgressXp = userXP - currentLevelMinXp
    val progressPercent = levelProgressXp.toFloat() / 500f

    // Intellectual Focus Index formula (just for fun and engagement)
    val focusIndex = remember(userXP, completedLessons, avgQuizScore) {
        val base = (completedLessons * 15) + (avgQuizScore * 0.4f) + (userXP * 0.05f)
        base.coerceIn(15f, 100f).toInt()
    }

    // Interactive Checklist Milestones
    val checklistItems = remember {
        listOf(
            ChecklistItem("first_lesson", "Compile your first video lesson", "Explore any topic and generate interactive lessons", 25, Icons.Default.AutoStories),
            ChecklistItem("perfect_mastery", "Score perfect mastery in a quiz", "Solve any lesson quiz with 100% correct answers", 50, Icons.Default.Star),
            ChecklistItem("ai_coach", "Consult the Socratic AI Coach", "Have an active dialogue on study materials", 20, Icons.Default.Psychology),
            ChecklistItem("spaced_rep", "Record Spaced Repetition cards", "Assess flashcards to optimize knowledge retention", 15, Icons.Default.ViewCarousel),
            ChecklistItem("jot_note", "Write custom learning notes", "Jot down a quick summary on the study scratchpad", 15, Icons.Default.EditNote)
        )
    }

    // Trending Course Spotlight
    val trendingTopics = remember {
        listOf(
            TrendingTopic("Quantum Computing", "Physics", "General", "A study of quantum qubits, entanglement, and superposition algorithms.", "Visual Storyteller", Icons.Default.Science, Color(0xFF6366F1)),
            TrendingTopic("CRISPR & Bio-Genetics", "Biology", "College", "Understanding gene-editing technology, CRISPR Cas-9, and biotechnology.", "Socratic Tutor", Icons.Default.Biotech, Color(0xFF10B981)),
            TrendingTopic("Medieval World Chronicles", "History", "General", "The rise and fall of medieval empires, castles, and historical trade routes.", "Visual Storyteller", Icons.Default.AutoStories, Color(0xFFF59E0B)),
            TrendingTopic("Introduction to Python & AI", "Computer Science", "Middle School", "Master basic coding concepts, variables, loops, and deep neural networks.", "Gamified", Icons.Default.Code, Color(0xFF06B6D4))
        )
    }

    // Rotating Motivational Quotes
    val quotes = remember {
        listOf(
            Quote("The important thing is not to stop questioning. Curiosity has its own reason for existing.", "Albert Einstein"),
            Quote("Nothing in life is to be feared, it is only to be understood. Now is the time to understand more.", "Marie Curie"),
            Quote("Education is not the filling of a vessel, but the kindling of a flame.", "Socrates"),
            Quote("I would rather have questions that can't be answered than answers that can't be questioned.", "Richard Feynman"),
            Quote("The beautiful thing about learning is that nobody can take it away from you.", "B.B. King")
        )
    }
    var currentQuoteIndex by remember { mutableStateOf(0) }

    val currentDateStr = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
    }

    // Premium dark gradient background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B0F19), // Deep rich space blue
            Color(0xFF111827), // Slate 900
            Color(0xFF090D16)  // Black-blue dark
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- SECTION 1: HEADER & GREETING ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentDateStr.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Welcome, $userName!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "🎓", fontSize = 24.sp)
                }
                Text(
                    text = "LoomiEdu Space Studio Dashboard",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Streak Flame Display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "$userStreak",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "DAYS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }

        // --- SECTION 2: THE METRIC RADAR GRID ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SCHOLAR PERFORMANCE INDEX",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )

                // Level & XP Progress Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Level $currentLevel Scholar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "$levelProgressXp / 500 XP to Level ${currentLevel + 1}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TOTAL: $userXP XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Smooth M3 Progress Bar
                LinearProgressIndicator(
                    progress = progressPercent,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color(0xFF334155),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                // Stat Summary Cards Grid (Adaptive Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mini Card 1: Completed Lectures
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Text("Lessons", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("$completedLessons", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    // Mini Card 2: Focus Score
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            Text("Focus Index", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("$focusIndex%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    // Mini Card 3: Badges
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFEA4335), modifier = Modifier.size(18.dp))
                            Text("Badges", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("${unlockedBadges.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }

        // --- SECTION 3: INTERACTIVE ROADMAP CHECKLIST ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ACTIVE SCHOLAR ROADMAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Achieve Milestones to gain level-up XP!",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Divider(color = Color(0xFF334155), thickness = 1.dp)

                checklistItems.forEach { item ->
                    val isCompleted = viewModel.isChecklistItemCompleted(item.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleChecklistItem(item.id, item.xpReward) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Checkbox custom container
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCompleted) Color(0xFF10B981) else Color(0xFF0F172A))
                                .border(
                                    2.dp,
                                    if (isCompleted) Color(0xFF10B981) else Color(0xFF475569),
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Icon of the activity
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isCompleted) Color(0xFF059669).copy(alpha = 0.15f) else Color(0xFF334155).copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (isCompleted) Color(0xFF10B981) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Content Texts
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) Color(0xFF94A3B8) else Color.White,
                                textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                            Text(
                                text = item.desc,
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Reward Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCompleted) Color(0xFFECFDF5) else Color(0xFFFFFBEB))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+${item.xpReward} XP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isCompleted) Color(0xFF047857) else Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 4: CURATED TRENDING SPOTLIGHTS ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "POPULAR CURRICULUM TOPICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Let's list the popular/trending curriculum topics
                // Display in 2x2 grid or horizontal scrolling. Let's do adaptive vertical column layout with cards, it's clean and easy to read.
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    trendingTopics.forEach { topic ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Inject and direct to Home Screen
                                    viewModel.topic.value = topic.topic
                                    viewModel.subject.value = topic.subject
                                    viewModel.studentClass.value = topic.classLevel
                                    viewModel.teachingStyle.value = topic.teachingStyle
                                    viewModel.setScreen(AppScreen.Home)
                                }
                                .testTag("trending_topic_${topic.topic.replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(topic.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = topic.icon,
                                        contentDescription = null,
                                        tint = topic.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(topic.color, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = topic.classLevel.uppercase(),
                                                fontSize = 7.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = topic.subject,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = topic.color
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = topic.topic,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = topic.desc,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Select Topic",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 5: SHUFFLEABLE SCHOLAR QUOTE ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACADEMIC MOTIVATION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = {
                            currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
                        },
                        modifier = Modifier.size(24.dp).testTag("shuffle_quote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Next Quote",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "\"${quotes[currentQuoteIndex].text}\"",
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Text(
                    text = "— ${quotes[currentQuoteIndex].author}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- SECTION 6: QUICK ACTION CENTER ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "QUICK STUDY ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action 1: Create Lesson (Goes to Home Screen)
                Button(
                    onClick = { viewModel.setScreen(AppScreen.Home) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_create_lesson")
                ) {
                    Icon(Icons.Default.MovieCreation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Build Lesson", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Action 2: Library Vault
                Button(
                    onClick = { viewModel.setScreen(AppScreen.Library) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_view_vault")
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lecture Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Action 3: Profile Info
                Button(
                    onClick = { viewModel.setScreen(AppScreen.Profile) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_view_profile")
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Profile Stats", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Data Classes for Onboarding and Curated Spotlights
data class ChecklistItem(
    val id: String,
    val title: String,
    val desc: String,
    val xpReward: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class TrendingTopic(
    val topic: String,
    val subject: String,
    val classLevel: String,
    val desc: String,
    val teachingStyle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

data class Quote(
    val text: String,
    val author: String
)
