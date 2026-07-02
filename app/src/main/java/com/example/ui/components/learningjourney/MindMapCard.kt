package com.example.ui.components.learningjourney

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MindMapNode
import com.example.data.VideoLesson
import com.example.ui.EduVideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapModalScreen(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    onClose: () -> Unit
) {
    var selectedNode by remember { mutableStateOf<MindMapNode?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Conceptual Mind Map", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
                .background(Color(0xFF12131A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E202B))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Tap on any structural node in the bubble graph below to inspect the AI concept definitions.",
                    fontSize = 11.sp,
                    color = Color(0xFFAEB2C6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.mindMapNodes.isEmpty()) {
                    Text("No Mind Map generated.", color = Color.White)
                } else {
                    val rootNode = lesson.mindMapNodes.firstOrNull { it.parentId == null } ?: lesson.mindMapNodes.first()
                    val childNodes = lesson.mindMapNodes.filter { it.parentId == rootNode.id }
                    
                    Box(
                        modifier = Modifier
                            .size(700.dp, 500.dp)
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            
                            childNodes.forEachIndexed { index, _ ->
                                val angle = (360f / childNodes.size) * index
                                val rad = Math.toRadians(angle.toDouble())
                                val length = 180f
                                
                                val childX = centerX + (length * Math.cos(rad)).toFloat()
                                val childY = centerY + (length * Math.sin(rad)).toFloat()
                                
                                drawLine(
                                    color = Color(0xFF3F51B5),
                                    start = Offset(centerX, centerY),
                                    end = Offset(childX, childY),
                                    strokeWidth = 3f
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(110.dp)
                                .shadow(8.dp, CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFE91E63), Color(0xFFC2185B))
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { selectedNode = rootNode },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rootNode.label,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(6.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        childNodes.forEachIndexed { index, node ->
                            val angle = (360f / childNodes.size) * index
                            val rad = Math.toRadians(angle.toDouble())
                            val length = 180f
                            
                            val offsetX = (length * Math.cos(rad)).toInt()
                            val offsetY = (length * Math.sin(rad)).toInt()

                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset { IntOffset(offsetX, offsetY) }
                                    .size(90.dp)
                                    .shadow(6.dp, CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                                        ),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedNode = node },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = node.label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(6.dp),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedNode != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                selectedNode?.let { node ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E202B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF00B0FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = node.label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { selectedNode = null }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = node.description.ifEmpty { "Main educational core concept definition." },
                                color = Color(0xFFAEB2C6),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("mindmap_complete_button")
            ) {
                Text("Complete Exploration")
            }
        }
    }
}
