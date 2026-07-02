package com.example.ui.components.learningjourney

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.JourneyStage
import com.example.ui.theme.DesignSystem

@Composable
fun TimelineItem(
    stage: JourneyStage,
    unlocked: Boolean,
    current: Boolean,
    completed: Boolean,
    isLast: Boolean,
    onCardClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left Column: Timeline Indicator Line and Dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
        ) {
            // Icon/Dot Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = when {
                            completed -> MaterialTheme.colorScheme.primary
                            current -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    )
                    .border(
                        width = if (current) 2.dp else 0.dp,
                        color = if (current) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable(enabled = unlocked, onClick = onCardClicked)
            ) {
                Icon(
                    imageVector = when {
                        completed -> Icons.Default.Check
                        !unlocked -> Icons.Default.Lock
                        else -> stage.icon
                    },
                    contentDescription = null,
                    tint = when {
                        completed -> MaterialTheme.colorScheme.onPrimary
                        current -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            // Connecting vertical line to next item
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Column: Premium Interactive Card
        Card(
            onClick = onCardClicked,
            enabled = unlocked,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
                .testTag("timeline_stage_${stage.name.lowercase()}"),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    completed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    current -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    unlocked -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                }
            ),
            border = BorderStroke(
                width = if (current) 1.5.dp else 1.dp,
                color = when {
                    current -> MaterialTheme.colorScheme.primary
                    completed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (current) 4.dp else 1.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stage.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        if (completed) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COMPLETED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        } else if (current) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CURRENT STEP",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stage.subtitle,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = if (unlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Icon(
                    imageVector = if (unlocked) Icons.Default.ChevronRight else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
