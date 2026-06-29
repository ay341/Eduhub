package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.GenerateState
import com.example.ui.EduVideoViewModel

enum class StepStatus {
    Completed,
    Active,
    Pending
}

@Composable
fun GenerationStepRow(
    title: String,
    description: String,
    status: StepStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulsingTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Status Icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            when (status) {
                StepStatus.Completed -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                StepStatus.Active -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer { alpha = pulseAlpha }
                    )
                }
                StepStatus.Pending -> {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Pending",
                        tint = Color(0xFFCAC4D0),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Text labels
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (status == StepStatus.Active) FontWeight.Bold else FontWeight.Medium,
                color = if (status == StepStatus.Active) {
                    MaterialTheme.colorScheme.primary
                } else if (status == StepStatus.Completed) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    Color(0xFF79747E)
                }
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = if (status == StepStatus.Pending) Color(0xFFCAC4D0) else Color(0xFF49454F),
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun GeneratingScreen(
    viewModel: EduVideoViewModel,
    generateState: GenerateState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (generateState) {
            is GenerateState.Progress -> {
                // Determine target progress and statuses based on current stage text
                val targetProgress = when (generateState.stage) {
                    "Structuring lesson curriculum..." -> 0.30f
                    "Designing visual animated slides..." -> 0.65f
                    "Generating voiceover narrative and quiz..." -> 0.90f
                    else -> 0.45f
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 800),
                    label = "LinearProgressAnimation"
                )

                val stage1Status = when (generateState.stage) {
                    "Structuring lesson curriculum..." -> StepStatus.Active
                    "Designing visual animated slides...", "Generating voiceover narrative and quiz..." -> StepStatus.Completed
                    else -> StepStatus.Active
                }

                val stage2Status = when (generateState.stage) {
                    "Structuring lesson curriculum..." -> StepStatus.Pending
                    "Designing visual animated slides..." -> StepStatus.Active
                    "Generating voiceover narrative and quiz..." -> StepStatus.Completed
                    else -> StepStatus.Pending
                }

                val stage3Status = when (generateState.stage) {
                    "Structuring lesson curriculum...", "Designing visual animated slides..." -> StepStatus.Pending
                    "Generating voiceover narrative and quiz..." -> StepStatus.Active
                    else -> StepStatus.Pending
                }

                // Pulsing/Progress Indicator Orb with AutoAwesome sparkle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(76.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Creating",
                        tint = Color(0xFFFBBF24), // Gold sparkle
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Generating stage text
                Text(
                    text = generateState.stage,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("generation_stage_text")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Immersive Linear Progress Indicator Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LoomiEdu Lesson Engine",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF79747E)
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Detailed Stepper/Checklist Box
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GenerationStepRow(
                            title = "1. Curriculum Structure",
                            description = "Structuring bite-sized academic curriculum syllabus for your topic.",
                            status = stage1Status
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 36.dp)
                        )
                        GenerationStepRow(
                            title = "2. Visual Layout Design",
                            description = "Crafting high-impact educational presentation slides and layout prompts.",
                            status = stage2Status
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 36.dp)
                        )
                        GenerationStepRow(
                            title = "3. Voiceover & interactive Quiz",
                            description = "Formulating teacher narrations and generating adaptive test questions.",
                            status = stage3Status
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Cancel Button
                OutlinedButton(
                    onClick = { viewModel.setScreen(AppScreen.Home) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.testTag("cancel_generation_button")
                ) {
                    Text("Cancel Generation", fontSize = 13.sp)
                }
            }

            is GenerateState.Error -> {
                // Error card presentation
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF2F2), // Soft Light Red
                        contentColor = Color(0xFF991B1B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generation_error_card")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFFEE2E2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error Icon",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Generation Failed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )

                        Text(
                            text = generateState.message,
                            fontSize = 13.sp,
                            color = Color(0xFF7F1D1D),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setScreen(AppScreen.Home) },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.weight(1f).testTag("error_back_button")
                            ) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Go Back", fontSize = 13.sp)
                            }

                            Button(
                                onClick = { viewModel.generateVideoLesson() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f).testTag("error_retry_button")
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry", fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            else -> {
                // Fallback / Idle (or Success)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
