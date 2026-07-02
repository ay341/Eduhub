package com.example.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoLesson
import com.example.ui.EduVideoViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun EduVideoPlayer(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    onVideoFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Playback States
    var isPlaying by remember { mutableStateOf(true) }
    var currentSceneIndex by remember { mutableStateOf(0) }
    var currentSceneElapsedTime by remember { mutableStateOf(0f) }
    var isMuted by remember { mutableStateOf(false) }
    var showSubtitles by remember { mutableStateOf(true) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // TTS Setup & Speed Preferences
    val speechRate by viewModel.ttsSpeechRate.collectAsState()
    val pitch by viewModel.ttsPitch.collectAsState()

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
        tts = instance
        onDispose {
            instance.stop()
            instance.shutdown()
        }
    }

    val currentScene = lesson.scenes.getOrNull(currentSceneIndex)

    // Speech trigger - Reacts to speed and pitch adjustments live
    LaunchedEffect(currentSceneIndex, isPlaying, isMuted, ttsReady, speechRate, pitch) {
        if (isPlaying && !isMuted && ttsReady && currentScene != null) {
            tts?.stop()
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)
            tts?.speak(currentScene.voiceoverText, TextToSpeech.QUEUE_FLUSH, null, "edu_scene_${currentSceneIndex}")
        } else {
            tts?.stop()
        }
    }

    // Playback Timer Loop - Synchronized with speech rate
    LaunchedEffect(isPlaying, currentSceneIndex, speechRate) {
        if (isPlaying && currentScene != null) {
            val totalSceneDurationMs = currentScene.durationSeconds * 1000
            var elapsedMs = (currentSceneElapsedTime * 1000).toInt()

            while (elapsedMs < totalSceneDurationMs) {
                delay(50)
                if (!isPlaying) break
                elapsedMs += (50 * speechRate).toInt()
                currentSceneElapsedTime = elapsedMs / 1000f
            }

            if (elapsedMs >= totalSceneDurationMs && isPlaying) {
                // Move to next scene
                if (currentSceneIndex < lesson.scenes.lastIndex) {
                    currentSceneElapsedTime = 0f
                    currentSceneIndex++
                } else {
                    // Video fully finished
                    isPlaying = false
                    currentSceneElapsedTime = currentScene.durationSeconds.toFloat()
                    onVideoFinished()
                }
            }
        }
    }

    // Space/Cosmic Palette
    val spaceBgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Deep Slate Navy
            Color(0xFF1E1B4B)  // Indigo Navy
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface) // Light surface frame
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        // --- 16:9 Interactive Screen ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(spaceBgGradient)
                .border(1.dp, Color(0xFF475569), RoundedCornerShape(16.dp))
                .testTag("video_player_screen")
        ) {
            // Slide Content
            if (currentScene != null) {
                // Animated slide layout
                AnimatedContent(
                    targetState = currentSceneIndex,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    },
                    label = "SlideTransition"
                ) { targetIndex ->
                    val sceneData = lesson.scenes[targetIndex]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Ambient particle canvas or icon overlay
                        Icon(
                            imageVector = when {
                                lesson.topic.contains("star", true) || lesson.topic.contains("space", true) || lesson.topic.contains("universe", true) -> Icons.Default.Language
                                lesson.topic.contains("math", true) || lesson.topic.contains("calculate", true) || lesson.topic.contains("geometry", true) -> Icons.Default.Calculate
                                lesson.topic.contains("history", true) || lesson.topic.contains("world war", true) || lesson.topic.contains("revolution", true) -> Icons.Default.History
                                lesson.topic.contains("code", true) || lesson.topic.contains("programming", true) || lesson.topic.contains("computer", true) -> Icons.Default.Code
                                else -> Icons.Default.School
                            },
                            contentDescription = "Topic Illustration",
                            tint = Color(0x1138BDF8), // Translucent cyan
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 10.dp, y = 10.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 60.dp), // keep spacing for background icon
                            verticalArrangement = Arrangement.Top
                        ) {
                            // Scene Tag & Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SCENE ${targetIndex + 1}/${lesson.scenes.size}",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sceneData.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            // Text on Screen
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sceneData.textOnScreen.split("\n").forEach { bullet ->
                                    if (bullet.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "✦",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(top = 2.dp, end = 6.dp)
                                            )
                                            Text(
                                                text = bullet.trim().removePrefix("•").removePrefix("-").trim(),
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 12.sp,
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

            // --- Realtime Subtitle Overlay (Closed Captioning) ---
            if (showSubtitles && currentScene != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .padding(horizontal = 16.dp)
                        .background(Color(0xCC020617), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x33F8FAFC), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentScene.voiceoverText,
                        color = Color(0xFFFCD34D), // Light yellow subtitles
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Custom Progress and Seek Slider ---
        // We compute elapsed virtual time based on selected duration (minutes)
        val totalDurationSeconds = lesson.scenes.sumOf { it.durationSeconds }
        val scenesAccumulatedSeconds = lesson.scenes.take(currentSceneIndex).sumOf { it.durationSeconds }
        val totalElapsedSeconds = scenesAccumulatedSeconds + currentSceneElapsedTime
        val progressPercent = if (totalDurationSeconds > 0) totalElapsedSeconds / totalDurationSeconds else 0f

        // Let's compute the display timeline clock
        // Scale the progress ratio into the virtual duration minutes chosen by the student
        val totalVirtualSeconds = lesson.durationMinutes * 60
        val virtualElapsedSeconds = (progressPercent * totalVirtualSeconds).toInt()
        
        val elapsedFormatted = String.format(Locale.getDefault(), "%02d:%02d", virtualElapsedSeconds / 60, virtualElapsedSeconds % 60)
        val totalFormatted = String.format(Locale.getDefault(), "%02d:00", lesson.durationMinutes)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = elapsedFormatted,
                color = Color(0xFF49454F),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(42.dp)
            )

            // Custom slider track
            Slider(
                value = progressPercent,
                onValueChange = { targetPercent ->
                    // Set playhead based on progress
                    val targetSeconds = targetPercent * totalDurationSeconds
                    var accumulated = 0
                    var foundSceneIndex = 0
                    for (i in lesson.scenes.indices) {
                        val dur = lesson.scenes[i].durationSeconds
                        if (accumulated + dur >= targetSeconds) {
                            foundSceneIndex = i
                            break
                        }
                        accumulated += dur
                        foundSceneIndex = i
                    }
                    currentSceneIndex = foundSceneIndex
                    currentSceneElapsedTime = targetSeconds - accumulated
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                    thumbColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .testTag("video_progress_slider")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = totalFormatted,
                color = Color(0xFF49454F),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Video Player Controls ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: Subtitles / Mute
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { showSubtitles = !showSubtitles },
                    modifier = Modifier.testTag("cc_button")
                ) {
                    Icon(
                        imageVector = if (showSubtitles) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                        contentDescription = "Toggle Subtitles",
                        tint = if (showSubtitles) MaterialTheme.colorScheme.primary else Color(0xFF79747E)
                    )
                }

                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier.testTag("mute_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute/Unmute Voice",
                        tint = if (!isMuted) MaterialTheme.colorScheme.primary else Color(0xFFEF4444)
                    )
                }
            }

            // Center Group: Navigation Play / Pause
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentSceneIndex > 0) {
                            currentSceneElapsedTime = 0f
                            currentSceneIndex--
                        } else {
                            currentSceneElapsedTime = 0f
                        }
                    },
                    enabled = currentSceneIndex > 0,
                    modifier = Modifier.testTag("prev_scene_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Scene",
                        tint = if (currentSceneIndex > 0) MaterialTheme.colorScheme.onBackground else Color(0xFFCAC4D0)
                    )
                }

                FilledIconButton(
                    onClick = { isPlaying = !isPlaying },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (currentSceneIndex < lesson.scenes.lastIndex) {
                            currentSceneElapsedTime = 0f
                            currentSceneIndex++
                        } else {
                            // Already on last scene, end the video
                            isPlaying = false
                            currentSceneElapsedTime = currentScene?.durationSeconds?.toFloat() ?: 0f
                            onVideoFinished()
                        }
                    },
                    modifier = Modifier.testTag("next_scene_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Scene",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Right Group: Voice Settings and Restart Scene
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { showSettingsMenu = !showSettingsMenu },
                    modifier = Modifier.testTag("playback_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Voice Settings",
                        tint = if (showSettingsMenu) MaterialTheme.colorScheme.primary else Color(0xFF79747E)
                    )
                }

                IconButton(
                    onClick = {
                        currentSceneElapsedTime = 0f
                        // Re-speak
                        if (isPlaying && !isMuted && ttsReady && currentScene != null) {
                            tts?.stop()
                            tts?.setSpeechRate(speechRate)
                            tts?.setPitch(pitch)
                            tts?.speak(currentScene.voiceoverText, TextToSpeech.QUEUE_FLUSH, null, "edu_scene_${currentSceneIndex}")
                        }
                    },
                    modifier = Modifier.testTag("restart_scene_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Restart Scene",
                        tint = Color(0xFF79747E)
                    )
                }
            }
        }

        // --- Voice Preferences Panel ---
        AnimatedVisibility(
            visible = showSettingsMenu,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "VOICEPLAY PREFERENCES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )

                    // Speed slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Delivery Rate: ${String.format(Locale.getDefault(), "%.1fx", speechRate)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Speed of speech voiceover",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        
                        Slider(
                            value = speechRate,
                            onValueChange = { viewModel.updateTtsSpeechRate(it) },
                            valueRange = 0.5f..2.0f,
                            steps = 5,
                            modifier = Modifier.width(140.dp)
                        )
                    }

                    // Pitch slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Pitch: ${String.format(Locale.getDefault(), "%.1fx", pitch)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tone pitch of virtual tutor",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        
                        Slider(
                            value = pitch,
                            onValueChange = { viewModel.updateTtsPitch(it) },
                            valueRange = 0.5f..2.0f,
                            steps = 5,
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }
        }
    }
}
