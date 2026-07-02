package com.example.ui.components.learningjourney

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flashcard
import com.example.data.VideoLesson
import com.example.ui.EduVideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsModalScreen(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    onClose: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    
    val flashcardList = remember(lesson.flashcards) {
        lesson.flashcards.ifEmpty {
            listOf(
                Flashcard(0, "What is the primary topic of this lesson?", "The primary study topic is: ${lesson.topic}"),
                Flashcard(1, "What difficulty is this lesson?", "This lesson difficulty level is: ${lesson.difficulty ?: "Intermediate"}")
            )
        }.toMutableStateList()
    }

    val currentCard = flashcardList.getOrNull(currentIndex) ?: flashcardList[0]

    fun toggleCardBookmark() {
        val updatedCard = currentCard.copy(isBookmarked = !currentCard.isBookmarked)
        flashcardList[currentIndex] = updatedCard
        viewModel.updateLessonProgress(lesson.id, flashcards = flashcardList.toList())
    }

    fun toggleCardLearned() {
        val updatedCard = currentCard.copy(isLearned = !currentCard.isLearned)
        flashcardList[currentIndex] = updatedCard
        
        val allLearned = flashcardList.all { it.isLearned }
        viewModel.updateLessonProgress(
            lesson.id, 
            flashcards = flashcardList.toList(),
            flashcardsStudied = if (allLearned) true else null
        )
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "FlashcardFlip"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spaced Repetition Flashcards", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1) / flashcardList.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Card ${currentIndex + 1} of ${flashcardList.size}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${flashcardList.count { it.isLearned }} Learned",
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { isFlipped = !isFlipped }
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .background(
                        brush = if (rotation <= 90f) {
                            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceVariant))
                        } else {
                            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.surface))
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (rotation <= 90f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "CONCEPT / QUESTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentCard.front,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Tap to Flip and see answer",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "EXPLANATION / ANSWER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentCard.back,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { toggleCardBookmark() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (currentCard.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark Card",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Button(
                    onClick = { toggleCardLearned() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentCard.isLearned) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (currentCard.isLearned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp).testTag("flashcard_learned_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentCard.isLearned) "Learned" else "Mark as Learned", fontSize = 12.sp)
                    }
                }

                IconButton(
                    onClick = { isFlipped = !isFlipped },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "Flip Card",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            isFlipped = false
                        }
                    },
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (currentIndex < flashcardList.size - 1) {
                            currentIndex++
                            isFlipped = false
                        } else {
                            viewModel.updateLessonProgress(lesson.id, flashcardsStudied = true)
                            onClose()
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("flashcard_next_button")
                ) {
                    Text(if (currentIndex < flashcardList.size - 1) "Next" else "Finish Study")
                    if (currentIndex < flashcardList.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}
