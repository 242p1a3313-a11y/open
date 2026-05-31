package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatEntry
import com.example.ui.components.BackgroundParticles
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodel.EcoViewModel
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(viewModel: EcoViewModel) {
    val session by viewModel.sessionState.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val preferredLanguage by viewModel.selectedLanguage.collectAsState()

    // Top scaffold containing top details bar, main tabs, and a sliding navigation bar
    BackgroundParticles {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                DashboardHeader(
                    studentName = session?.fullName ?: "Rahul",
                    streakCount = session?.streak ?: 15,
                    coins = session?.ecoPoints ?: 1200,
                    activeLang = preferredLanguage,
                    onSignOut = { viewModel.signOut() }
                )
            },
            bottomBar = {
                DashboardBottomBar(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.setDashboardTab(it) }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = activeTab, label = "TabCrossfade") { tab ->
                    when (tab) {
                        "Mitra Chat" -> MitraChatTab(viewModel)
                        "Scan Diagnostic" -> ScanTab(viewModel)
                        "My Garden" -> GardenTab(viewModel)
                        "Achievements" -> AchievementsTab(viewModel, session?.badges ?: "Green Starter")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(
    studentName: String,
    streakCount: Int,
    coins: Int,
    activeLang: String,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Color(0xD0081C0F)) // Dark deep canopy frame background
            .border(width = 1.dp, color = Color(0x1AFFFFFF), shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌱 EcoFriend",
                        color = Color(0xFF6CF097),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x336CF097))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when(activeLang) {
                                "Hindi" -> "HI-IN 🇮🇳"
                                "Telugu" -> "TE-IN 🇮🇳"
                                else -> "EN-IN 🇬🇧"
                            },
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text(
                    text = "Hello, $studentName!",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Streak metric pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FF6D00))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streakCount Day",
                            color = Color(0xFFFF9100),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Points metric pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x336CF097))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌳", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coins PTS",
                            color = Color(0xFF6CF097),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Sign Out Option
                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardBottomBar(activeTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Mitra Chat", "Scan Diagnostic", "My Garden", "Achievements")
    
    NavigationBar(
        containerColor = Color(0xD0081C0F), // Semi translucent bottom plate
        tonalElevation = 8.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(width = 0.8.dp, color = Color(0x19FFFFFF), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        tabs.forEach { tab ->
            val isSelected = activeTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = { 
                    Text(
                        text = when(tab) {
                            "Mitra Chat" -> "Mitra Chat"
                            "Scan Diagnostic" -> "Leaf Scan"
                            "My Garden" -> "Garden"
                            else -> "Badges"
                        }, 
                        fontSize = 10.sp, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            "Mitra Chat" -> Icons.Default.Face
                            "Scan Diagnostic" -> Icons.Default.Search
                            "My Garden" -> Icons.Default.Favorite
                            else -> Icons.Default.Star
                        },
                        contentDescription = tab
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color.LightGray,
                    selectedTextColor = Color(0xFF6CF097),
                    unselectedTextColor = Color.LightGray,
                    indicatorColor = Color(0xFF6CF097)
                )
            )
        }
    }
}

@Composable
fun MitraChatTab(viewModel: EcoViewModel) {
    val chats by viewModel.chatEntries.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()
    val isVoiceRecording by viewModel.isVoiceRecording.collectAsState()
    val activeLang by viewModel.selectedLanguage.collectAsState()

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Auto scroll chat to bottom when entry count changes
    LaunchedEffect(chats.size) {
        if (chats.isNotEmpty()) {
            lazyListState.animateScrollToItem(chats.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // AI Avatar Card explaining status
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x330C2414)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .border(width = 0.5.dp, color = Color(0x15FFFFFF), shape = RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PrakritiMitra Active",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = when(activeLang) {
                            "Hindi" -> "उपयुक्त पौधे, पानी की सलाह और पौधों की बीमारी के इलाज के लिए मुझसे पूछें।"
                            "Telugu" -> "మొక్కల పెంపకం, ఆకు తెగుళ్ళు మరియు పర్యావరణ పరిరక్షణ గురించి నన్ను అడగండి."
                            else -> "Ask me for plant recommendation, water intervals or disease treatment."
                        },
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Chats History Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x11FFFFFF))
                .border(0.8.dp, Color(0x19FFFFFF), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            if (chats.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌿", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your conversation with PrakritiMitra is empty.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Try speech typing or write 'Recommend a tree' below!",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp, vertical = 4.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chats) { item ->
                        ChatBubbleField(item)
                    }
                    if (isAILoading) {
                        item {
                            PrakritiMitraThinkingBubble()
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simulated speech indicator when recording is active. Generates waving visual blocks
        if (isVoiceRecording) {
            SimulatedMicPulseWaves()
        }

        // Input Send Box Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated Voice Command Button with browser-like audio feedback
            IconButton(
                onClick = { viewModel.toggleVoiceRecording() },
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isVoiceRecording) Color(0xFFFF5252) else Color(0x33FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.Default.Share, // Simulated speech icon shape representation
                    contentDescription = "Voice Input",
                    tint = Color.White
                )
            }

            OutlinedTextField(
                value = chatInput,
                onValueChange = { viewModel.chatInput.value = it },
                placeholder = {
                    Text(
                        text = when(activeLang) {
                            "Hindi" -> "संदेश भेजें..."
                            "Telugu" -> "సందేశాన్ని టైప్ చేయండి..."
                            else -> "Message PrakritiMitra..."
                        },
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF6CF097),
                    unfocusedBorderColor = Color(0x40FFFFFF)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Message Send triggers database injection
            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submitChatMessage()
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32))
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatBubbleField(chat: ChatEntry) {
    val isUser = chat.sender == "user"
    val bubbleColor = if (isUser) Color(0xFF1B5E20) else Color(0x2BFFFFFF)
    val align = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Column {
                // Language label indicating exactly what trilingual parsing occurred
                if (chat.detectedLanguage.isNotBlank()) {
                    Text(
                        text = "Detected Lang: ${chat.detectedLanguage}",
                        color = Color(0xFF81C784),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = chat.text,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        
        Text(
            text = if (isUser) "Student" else "PrakritiMitra",
            color = Color.Gray,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun PrakritiMitraThinkingBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Text("🤖", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "PrakritiMitra is thinking...",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SimulatedMicPulseWaves() {
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "wave1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(animation = tween(650, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "wave2"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22FF5252))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Listening speech command...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(12.dp))
        
        // Simulating 4 energetic visual frequency waves
        Box(modifier = Modifier.size(6.dp, 16.dp * scale1).clip(CircleShape).background(Color.White))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(6.dp, 16.dp * scale2).clip(CircleShape).background(Color.White))
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(6.dp, 16.dp * scale1).clip(CircleShape).background(Color.White))
    }
}

@Composable
fun ScanTab(viewModel: EcoViewModel) {
    val progress by viewModel.scanProgress.collectAsState()
    val scannedName by viewModel.scannedPlantName.collectAsState()
    val diagnostics by viewModel.scanDiagnostics.collectAsState()
    val careTreatment by viewModel.scanTreatment.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌿 AI Camera Leaf Scan",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Text(
            text = "Point camera at any damaged or healthy leaves",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Start
        )

        // Leaf scanning camera viewport box representation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x26000000))
                .border(2.dp, Color(0xFF6CF097), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Lens Gridlines drawing representation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val horizontalThird = size.width / 3
                val verticalThird = size.height / 3

                // Drawing subtle viewfinder gridlines
                drawLine(Color(0x33ffffff), Offset(horizontalThird, 0f), Offset(horizontalThird, size.height), 1f)
                drawLine(Color(0x33ffffff), Offset(horizontalThird * 2, 0f), Offset(horizontalThird * 2, size.height), 1f)
                drawLine(Color(0x33ffffff), Offset(0f, verticalThird), Offset(size.width, verticalThird), 1f)
                drawLine(Color(0x33ffffff), Offset(0f, verticalThird * 2), Offset(size.width, verticalThird * 2), 1f)
            }

            if (progress == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍃 [ Leaf Shape Target ]", color = Color(0xFFAEEA00), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Fits leaf silhouette within bounds to diagnose accurately", color = Color.LightGray, fontSize = 11.sp)
                }
            } else {
                Surface(
                    color = Color(0xAA000000),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6CF097))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (progress) {
                                "INIT" -> "Initializing AI sensor camera lenses..."
                                "SCANNING" -> "Scanning leafy surface geometry structures..."
                                "ANALYZING" -> "Interpreting cells with PrakritiMitra AI..."
                                else -> "Displaying Results..."
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (progress == null) {
            Button(
                onClick = { viewModel.triggerLeafScan() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Start Real-time Scanning (+100 Eco Point)", fontWeight = FontWeight.Bold, color = Color.White)
            }
        } else if (progress == "RESULT_HEALTHY" || progress == "RESULT_DISEASE") {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (progress == "RESULT_HEALTHY") Icons.Default.CheckCircle else Icons.Default.Warning,
                            tint = if (progress == "RESULT_HEALTHY") Color(0xFF69F0AE) else Color(0xFFFF5252),
                            contentDescription = "Diagnostic status"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = scannedName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = diagnostics, color = Color.LightGray, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = careTreatment, color = Color(0xFFB2FF59), fontSize = 13.sp, lineHeight = 18.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.clearScan() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Scan & Try Again", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun GardenTab(viewModel: EcoViewModel) {
    var waterCount1 by remember { mutableStateOf(12) }
    var waterCount2 by remember { mutableStateOf(8) }

    val session by viewModel.sessionState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("🌳 Student Green Garden", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Collect Eco points daily by keeping your plants watered!", color = Color.LightGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar overview of goals
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Your Plantation Target Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                
                val currentGoal = session?.plantationGoal ?: 5
                val currentTrees = session?.treesToGrow ?: 2
                
                Text("Goal: Plant $currentGoal plants & grow $currentTrees trees this year", color = Color.LightGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                
                LinearProgressIndicator(
                    progress = 0.6f,
                    trackColor = Color(0x33FFFFFF),
                    color = Color(0xFF6CF097),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("3 planted (60%)", color = Color.LightGray, fontSize = 11.sp)
                    Text("Goal: $currentGoal", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Growing plants lists inside garden
        Text("Your Growing Plants (2)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Card item 1
        PlantCardItem(
            name = "Organic Tulsi (Holy Basil)",
            location = "Student Room Window Sill",
            lastWateredHours = waterCount1,
            onWaterPressed = {
                waterCount1 = 0
                // Add points
                viewModel.waterPlant(15)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Card item 2
        PlantCardItem(
            name = "Curry Leaf Sapling",
            location = "College Hostel Balcony",
            lastWateredHours = waterCount2,
            onWaterPressed = {
                waterCount2 = 0
                viewModel.waterPlant(15)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Add Plant Dialog Trigger
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x19FFFFFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Simulate Adding a dynamic plant */ }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Sapling", tint = Color(0xFF6CF097))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register a New Sapling (+50 XP)", color = Color(0xFF6CF097), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun PlantCardItem(
    name: String,
    location: String,
    lastWateredHours: Int,
    onWaterPressed: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x23FFFFFF)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("📍 $location", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (lastWateredHours == 0) "Status: Just Watered! 💧" else "Last Watered: $lastWateredHours hours ago",
                    color = if (lastWateredHours > 10) Color(0xFFFF8B8B) else Color(0xFF99FF99),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onWaterPressed,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Water 💧", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AchievementsTab(viewModel: EcoViewModel, userBadges: String) {
    val badgesList = userBadges.split(",").map { it.trim() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("🥇 Student Achievements & Badges", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Unlock medals by participating in eco-planting audits with PrakritiMitra!", color = Color.LightGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Badge Grid
        BadgeHighlightCard(
            title = "🥉 Green Starter",
            desc = "Awarded automatically upon registration and onboarding your first sapling.",
            unlocked = badgesList.contains("Green Starter")
        )

        Spacer(modifier = Modifier.height(10.dp))

        BadgeHighlightCard(
            title = "🥈 Tree Guardian",
            desc = "Awarded once your garden plants are watered consistently and reach 1300 Eco Points.",
            unlocked = badgesList.contains("Tree Guardian")
        )

        Spacer(modifier = Modifier.height(10.dp))

        BadgeHighlightCard(
            title = "🥇 Eco Champion",
            desc = "The highest student achievement. Unlocked when we hit 1500 Eco Points and analyze leaf health effectively.",
            unlocked = badgesList.contains("Eco Champion")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Local Campus Leaderboard to raise competitive vibes
        Text("🏆 Campus Green Standings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LeaderboardItem(rank = 1, name = "Anjali Rao (Grade 10)", points = 1650, isSelf = false)
        LeaderboardItem(rank = 2, name = "Rahul Kumar (You)", points = 1200, isSelf = true)
        LeaderboardItem(rank = 3, name = "Vikram Singh (Grade 9)", points = 950, isSelf = false)
    }
}

@Composable
fun BadgeHighlightCard(title: String, desc: String, unlocked: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) Color(0x382E7D32) else Color(0x19FFFFFF)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (unlocked) Color(0xFF6CF097) else Color(0x11FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) Color(0x556CF097) else Color(0x11FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unlocked) "🏅" else "🔒",
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = if (unlocked) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = desc,
                    color = if (unlocked) Color.LightGray else Color.DarkGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, name: String, points: Int, isSelf: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelf) Color(0x3D6CF097) else Color(0x0AFFFFFF)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#$rank",
                    color = if (rank == 1) Color(0xFFFFD54F) else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.width(28.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = if (isSelf) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Text("$points XP", color = Color(0xFF6CF097), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
