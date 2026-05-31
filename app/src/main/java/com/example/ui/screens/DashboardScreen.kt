package com.example.ui.screens

import android.speech.tts.TextToSpeech
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
import androidx.compose.ui.platform.LocalContext
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
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: EcoViewModel) {
    val session by viewModel.sessionState.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val preferredLanguage by viewModel.selectedLanguage.collectAsState()

    // Initialize Android TextToSpeech lazily & safely to prevent background thread binding crashes on unsupported systems.
    val context = LocalContext.current
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitFailed by remember { mutableStateOf(false) }

    val speakOut: (String, String) -> Unit = { text, lang ->
        if (ttsInstance == null && !ttsInitFailed) {
            try {
                var tts: TextToSpeech? = null
                tts = TextToSpeech(context.applicationContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        try {
                            val locale = when (lang) {
                                "Hindi" -> Locale("hi", "IN")
                                "Telugu" -> Locale("te", "IN")
                                else -> Locale("en", "IN")
                            }
                            tts?.language = locale
                            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PrakritiVoiceRef")
                        } catch (ex: Exception) {
                            android.util.Log.e("EcoTTS", "TTS Settings error", ex)
                        }
                    } else {
                        ttsInitFailed = true
                    }
                }
                ttsInstance = tts
            } catch (e: Exception) {
                android.util.Log.e("EcoTTS", "TTS Initialization failed", e)
                ttsInitFailed = true
            }
        } else {
            try {
                val locale = when (lang) {
                    "Hindi" -> Locale("hi", "IN")
                    "Telugu" -> Locale("te", "IN")
                    else -> Locale("en", "IN")
                }
                ttsInstance?.language = locale
                ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PrakritiVoiceRef")
            } catch (ex: Exception) {
                android.util.Log.e("EcoTTS", "TTS Speak failed", ex)
            }
        }
    }

    DisposableEffect(ttsInstance) {
        onDispose {
            try {
                ttsInstance?.shutdown()
            } catch (e: java.lang.Exception) {
                android.util.Log.e("EcoTTS", "TTS Shutdown failed", e)
            }
        }
    }

    BackgroundParticles {
        Scaffold(
            containerColor = BgFreshMint, // fresh pale mint background #F0FDF4
            topBar = {
                DashboardHeader(
                    studentName = session?.fullName ?: "Rahul",
                    streakCount = session?.streak ?: 15,
                    coins = session?.ecoPoints ?: 1200,
                    activeLang = preferredLanguage,
                    onLangChange = { viewModel.selectLanguage(it) },
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
                        "Home" -> HomeTab(viewModel)
                        "Plants" -> PlantsTab(viewModel)
                        "Scan" -> DiagnosticScanTab(viewModel)
                        "Chat" -> WebChatTab(viewModel, speakOut)
                        "Profile" -> StudentProfileTab(viewModel)
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
    onLangChange: (String) -> Unit,
    onSignOut: () -> Unit
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Color(0xF2FFFFFF))
            .border(width = 1.dp, color = BorderSage, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Logo and Dynamic Language switcher in header navbar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌱 EcoFriend",
                        color = BrandGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Box {
                        TextButton(
                            onClick = { showLangMenu = true },
                            colors = ButtonDefaults.textButtonColors(containerColor = TintCap),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(32.dp).testTag("header_lang_switcher")
                        ) {
                            Text(
                                text = when (activeLang) {
                                    "Hindi" -> "HI 🇮🇳 ▾"
                                    "Telugu" -> "TE 🇮🇳 ▾"
                                    else -> "EN 🇬🇧 ▾"
                                },
                                color = BrandGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("English 🇬🇧") },
                                onClick = {
                                    onLangChange("English")
                                    showLangMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("हिन्दी 🇮🇳 (Hindi)") },
                                onClick = {
                                    onLangChange("Hindi")
                                    showLangMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("తెలుగు 🇮🇳 (Telugu)") },
                                onClick = {
                                    onLangChange("Telugu")
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Streak Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF1EB))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$streakCount Days",
                                color = Color(0xFFE65100),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Coins Pill with custom #FACC15 AccentYellow container boundary
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TintCap)
                            .border(0.5.dp, AccentYellow, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌟", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$coins PTS",
                                color = BrandGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.size(36.dp).testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFBA1A1A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Welcome back, $studentName! 👋",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Composable
fun DashboardBottomBar(activeTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Home", "Plants", "Scan", "Chat", "Profile")

    NavigationBar(
        containerColor = Color(0xF2FFFFFF),
        tonalElevation = 8.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(width = 0.8.dp, color = BorderSage, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        tabs.forEach { tab ->
            val isSelected = activeTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = when(tab) {
                            "Home" -> "Home"
                            "Plants" -> "Plants"
                            "Scan" -> "Scan"
                            "Chat" -> "Chat"
                            else -> "Profile"
                        },
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.testTag("nav_label_$tab")
                    )
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            "Home" -> Icons.Default.Home
                            "Plants" -> Icons.Default.Star
                            "Scan" -> Icons.Default.Search
                            "Chat" -> Icons.Default.Face
                            else -> Icons.Default.Person
                        },
                        contentDescription = tab,
                        modifier = Modifier.testTag("nav_icon_$tab")
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = BrandGreen,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = BrandGreen
                )
            )
        }
    }
}

// ==========================================
// 🏠 HOME TAB - Incorporates Water Management and Climate Analysis
// ==========================================
@Composable
fun HomeTab(viewModel: EcoViewModel) {
    val activeLang by viewModel.selectedLanguage.collectAsState()

    // Water States
    val waterPlant by viewModel.waterPlantType.collectAsState()
    val waterWeatherCond by viewModel.waterWeather.collectAsState()
    val waterCalcResult by viewModel.waterResult.collectAsState()

    // Climate States
    val climTemp by viewModel.climateTemp.collectAsState()
    val climRain by viewModel.climateRain.collectAsState()
    val climHumidity by viewModel.climateHumidity.collectAsState()
    val climResult by viewModel.climateResult.collectAsState()

    var showTipsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Poster Card with Animated gradient and Glassmorphism feel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandGreen, PrimaryGreen)
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = when(activeLang) {
                        "Hindi" -> "बदलाव का हिस्सा बनें! 🌍"
                        "Telugu" -> "మార్పునకు ఊపిరి పోయండి! 🌍"
                        else -> "Be the Agent of Green Change! 🌍"
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when(activeLang) {
                        "Hindi" -> "पेड़ लगाएं, पानी का सही प्रबंधन करें और अपने पर्यावरण को समृद्ध बनाएं।"
                        "Telugu" -> "మొక్కలను నాటండి, నీటిని సంరక్షించండి మరియు ప్రకృతిని రక్షించండి."
                        else -> "Plant saplings daily, analyze soil, check climate suitability and water efficiently with PrakritiMitra."
                    },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Feature 3: WATER MANAGEMENT DEVICE
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💧 Smart Water Management",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Calculate optimal daily water quantity & view reminders",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Input row 1
                Text("Select Target Plant Species:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                var expandedPlant by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedButton(
                        onClick = { expandedPlant = true },
                        modifier = Modifier.fillMaxWidth().testTag("water_plant_dropdown")
                    ) {
                        Text(text = waterPlant, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    DropdownMenu(expanded = expandedPlant, onDismissRequest = { expandedPlant = false }) {
                        listOf("Organic Tulsi", "Curry Leaf Sapling", "Neem Tree Sapling", "Rose Bush", "Aloe Vera").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    viewModel.waterPlantType.value = item
                                    expandedPlant = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input row 2
                Text("Select Weather Atmosphere:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                var expandedWeather by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedButton(
                        onClick = { expandedWeather = true },
                        modifier = Modifier.fillMaxWidth().testTag("water_weather_dropdown")
                    ) {
                        Text(text = waterWeatherCond, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    DropdownMenu(expanded = expandedWeather, onDismissRequest = { expandedWeather = false }) {
                        listOf("Sunny", "Cloudy", "Rainy").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    viewModel.waterWeather.value = item
                                    expandedWeather = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action button
                Button(
                    onClick = { viewModel.triggerWaterCalculation() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.fillMaxWidth().testTag("calculate_water_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Calculate Water Quantity 💧", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (waterCalcResult.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TintCap)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = waterCalcResult,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Conservation Tips Indicator button
                TextButton(
                    onClick = { showTipsDialog = true },
                    colors = ButtonDefaults.textButtonColors(containerColor = TintCap),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💡 Read 5 Crucial Water Conservation Tips", color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Feature 4: CLIMATE SUITABILITY & KALENDAR ANALYSIS
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌦️ Climate Suitability Analyzer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Enter temperature, rainfall levels and relative humidity",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Numeric input Temp
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = climTemp,
                        onValueChange = { viewModel.climateTemp.value = it },
                        label = { Text("Temp (°C)") },
                        modifier = Modifier.weight(1f).testTag("climate_temp_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = customTextFieldColors()
                    )

                    OutlinedTextField(
                        value = climRain,
                        onValueChange = { viewModel.climateRain.value = it },
                        label = { Text("Rainfall (mm)") },
                        modifier = Modifier.weight(1f).testTag("climate_rain_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = customTextFieldColors()
                    )

                    OutlinedTextField(
                        value = climHumidity,
                        onValueChange = { viewModel.climateHumidity.value = it },
                        label = { Text("Humidity (%)") },
                        modifier = Modifier.weight(1f).testTag("climate_humidity_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = customTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.triggerClimateAnalysis() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.fillMaxWidth().testTag("test_climate_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Evaluate Compatibility ✅", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (climResult.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFFDEB))
                            .border(0.5.dp, Color(0xFFDCC135), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = climResult,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seasonal Planting Calendar
                Text(
                    text = "🗓️ Seasonal Sowing Calendar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalendarRow(season = "☀️ Summer (March - June)", plants = "Organic Tulsi, Aloe Vera, Chilli")
                    CalendarRow(season = "🌧️ Monsoon (July - October)", plants = "Neem Tree, Mint Herbs, Curry Leaf")
                    CalendarRow(season = "❄️ Winter (November - February)", plants = "Marigold Flower, Rose Shrub, Coriander")
                }
            }
        }
    }

    // Water Conservation Tips Dialog
    if (showTipsDialog) {
        AlertDialog(
            onDismissRequest = { showTipsDialog = false },
            confirmButton = {
                TextButton(onClick = { showTipsDialog = false }) {
                    Text("OK", color = BrandGreen)
                }
            },
            title = { Text("Water Conservation Tips 💚", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. **Mulching**: Place coconut coir or dry leaves on topsoil to lock moisture.", fontSize = 12.sp)
                    Text("2. **Vege-Wash Recycling**: Irrigate with kitchen water used for cleaning lentils.", fontSize = 12.sp)
                    Text("3. **Drip Bottles**: Hang inverted bottles with slow pin punctures above pots.", fontSize = 12.sp)
                    Text("4. **Zero Noon Watering**: Water before sunrise to minimize prompt evaporation.", fontSize = 12.sp)
                    Text("5. **Rainwater Collection**: Harvest clean patio rains specifically for sensitive sprouts.", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
fun CalendarRow(season: String, plants: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF7FDF9))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(BrandGreen)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(season, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
            Text(plants, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// ==========================================
// 🌿 PLANTS TAB - Smart Recommendations, Growth AI & Garden
// ==========================================
@Composable
fun PlantsTab(viewModel: EcoViewModel) {
    val session by viewModel.sessionState.collectAsState()
    
    // Rec states
    val recL by viewModel.recLocation.collectAsState()
    val recS by viewModel.recSeason.collectAsState()
    val recSo by viewModel.recSoil.collectAsState()
    val recSp by viewModel.recSpace.collectAsState()
    val recRes by viewModel.recResult.collectAsState()
    val isRecL by viewModel.isRecLoading.collectAsState()

    // Growth states
    val groP by viewModel.growthPlantName.collectAsState()
    val groD by viewModel.growthDays.collectAsState()
    val groRes by viewModel.growthResult.collectAsState()
    val isGroL by viewModel.isGrowthLoading.collectAsState()

    var customWater1 by remember { mutableStateOf(10) }
    var customWater2 by remember { mutableStateOf(4) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🌱 Smart Plantation Studio",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = TextPrimary
        )

        // Feature 1: SMART PLANTATION RECOMMENDATIONS (FORM)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 AI Plantation Recommender",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Input location/soil attributes below to get optimized recommendations:",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Location Input text
                OutlinedTextField(
                    value = recL,
                    onValueChange = { viewModel.recLocation.value = it },
                    label = { Text("What is your Location? (e.g. Hyderabad)") },
                    shape = RoundedCornerShape(8.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("rec_location_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Season selection dropdown
                    var expandSeason by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Season:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandSeason = true },
                                modifier = Modifier.fillMaxWidth().testTag("rec_season_button")
                            ) {
                                Text(recS, color = TextPrimary, fontSize = 11.sp)
                            }
                            DropdownMenu(expanded = expandSeason, onDismissRequest = { expandSeason = false }) {
                                listOf("Summer", "Winter", "Monsoon").forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            viewModel.recSeason.value = s
                                            expandSeason = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Soil Selection
                    var expandSoil by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Soil Details:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandSoil = true },
                                modifier = Modifier.fillMaxWidth().testTag("rec_soil_button")
                            ) {
                                Text(recSo, color = TextPrimary, fontSize = 11.sp)
                            }
                            DropdownMenu(expanded = expandSoil, onDismissRequest = { expandSoil = false }) {
                                listOf("Loamy", "Sandy", "Clayey", "Black Cotton").forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            viewModel.recSoil.value = s
                                            expandSoil = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Available Space selection
                var expandSpace by remember { mutableStateOf(false) }
                Column {
                    Text("Available Plantation Space Size:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                        OutlinedButton(
                            onClick = { expandSpace = true },
                            modifier = Modifier.fillMaxWidth().testTag("rec_space_button")
                        ) {
                            Text(recSp, color = TextPrimary)
                        }
                        DropdownMenu(expanded = expandSpace, onDismissRequest = { expandSpace = false }) {
                            listOf("Small Pots", "Medium Bed", "Balcony Hanging", "Large Open Backyard").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        viewModel.recSpace.value = s
                                        expandSpace = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.triggerSmartRecommendation() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.fillMaxWidth().testTag("trigger_recommend_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Trigger AI Plantation Guide 🌱", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (isRecL) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = BrandGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Reading soil grids, calculating air quality index...", fontSize = 12.sp, color = BrandGreen)
                    }
                }

                recRes?.let { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TintCap)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🔬 **Mitra AI Suggested Sapling:**", fontSize = 13.sp, color = BrandGreen, fontWeight = FontWeight.Bold)
                            Text("• **Best Plant**: ${item["plant"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **Expected Growth Cycle**: ${item["growth"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **Maintenance Effort**: ${item["maintenance"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **Environmental Benefits**: ${item["benefits"]}", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Feature 6: GROWTH PREDICTION AI
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 AI Growth Predictor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Project estimated growth milestones mathematically",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = groP,
                    onValueChange = { viewModel.growthPlantName.value = it },
                    label = { Text("Enter Plant / Sapling Name") },
                    modifier = Modifier.fillMaxWidth().testTag("growth_plant_field"),
                    shape = RoundedCornerShape(8.dp),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = groD,
                    onValueChange = { viewModel.growthDays.value = it },
                    label = { Text("Days from Planting (e.g. 45)") },
                    modifier = Modifier.fillMaxWidth().testTag("growth_days_field"),
                    shape = RoundedCornerShape(8.dp),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.triggerGrowthPrediction() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.fillMaxWidth().testTag("trigger_predict_growth_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Predict Growth Heights 📊", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (isGroL) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = BrandGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Graphing photosynthesis projections...", fontSize = 12.sp, color = BrandGreen)
                    }
                }

                groRes?.let { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFFDEB))
                            .border(0.5.dp, Color(0xFFDCC135), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📈 Projections for $groP:", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("• **Expected Height**: ${item["height"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **Plant growth stage**: ${item["stage"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **Harvest Milestones**: ${item["harvest"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **Estimated Yield Weight**: ${item["yield"]}", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Growing plants list Inside garden
        Text("Your Personal Plantings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        PlantCardItem(
            name = "Organic Tulsi (Holy Basil)",
            location = "Hostel Window Sill Patio",
            lastWateredHours = customWater1,
            onWaterPressed = {
                customWater1 = 0
                viewModel.waterPlant(15)
            }
        )

        PlantCardItem(
            name = "Curry Leaf Sapling",
            location = "Campus Garden Plot 4B",
            lastWateredHours = customWater2,
            onWaterPressed = {
                customWater2 = 0
                viewModel.waterPlant(15)
            }
        )
    }
}

// ==========================================
// 📷 SCAN TAB - Disease Detection and Soil Intelligence
// ==========================================
@Composable
fun DiagnosticScanTab(viewModel: EcoViewModel) {
    val scanProg by viewModel.scanProgress.collectAsState()
    val scannedPN by viewModel.scannedPlantName.collectAsState()
    val scanDiag by viewModel.scanDiagnostics.collectAsState()
    val scanTreat by viewModel.scanTreatment.collectAsState()

    // Soil intelligence states
    val activeSoilType by viewModel.soilType.collectAsState()
    val isSoilLoading by viewModel.isSoilLoading.collectAsState()
    val soilResult by viewModel.soilResult.collectAsState()

    var showCameraViewfinder by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Headline
        Text(
            text = "📷 Diagnostic Scanning Center",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = TextPrimary
        )

        // Feature 5: LEAF DISEASE DETECTION
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🍃 Leaf Disease Detection System", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text("Simulate image classification for instant disease cures", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))

                if (showCameraViewfinder) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x0F000000))
                            .border(2.dp, BrandGreen, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val hThird = size.width / 3
                            val vThird = size.height / 3
                            drawLine(Color(0x3316A34A), Offset(hThird, 0f), Offset(hThird, size.height), 1.5f)
                            drawLine(Color(0x3316A34A), Offset(hThird * 2, 0f), Offset(hThird * 2, size.height), 1.5f)
                            drawLine(Color(0x3316A34A), Offset(0f, vThird), Offset(size.width, vThird), 1.5f)
                            drawLine(Color(0x3316A34A), Offset(0f, vThird * 2), Offset(size.width, vThird * 2), 1.5f)
                        }

                        if (scanProg == null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Text("🍃 Viewfinder Target", color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Select a sample leaf image to scan below", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        } else {
                            Surface(
                                color = Color(0xD9FFFFFF),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    CircularProgressIndicator(color = BrandGreen)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = when(scanProg) {
                                            "INIT" -> "Calibrating smart lens camera sensor..."
                                            "SCANNING" -> "Mapping leafy surface chlorophyll structures..."
                                            "ANALYZING" -> "PrakritiMitra neural matching diagnostic..."
                                            else -> "Diagnostic Completed!"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Upload simulator choices
                if (scanProg == null) {
                    Text("Choose Leaf Photo scenario to upload/evaluate:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerLeafScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = TintCap, contentColor = BrandGreen),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("select_leaf_1"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Spotted Rose Leaf 🥀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.triggerLeafScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = TintCap, contentColor = BrandGreen),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("select_leaf_2"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Healthy Basil Leaf 🌿", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (scanProg == "RESULT_HEALTHY" || scanProg == "RESULT_DISEASE") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TintCap)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (scanProg == "RESULT_HEALTHY") Icons.Default.CheckCircle else Icons.Default.Warning,
                                    tint = if (scanProg == "RESULT_HEALTHY") BrandGreen else Color(0xFFBA1A1A),
                                    contentDescription = "status"
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = scannedPN,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = scanDiag, fontSize = 12.sp, color = TextPrimary, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = scanTreat, fontSize = 12.sp, color = BrandGreen, fontWeight = FontWeight.Bold, lineHeight = 16.sp)
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.clearScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F000000), contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Scan Another Leaf", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Feature 2: SOIL INTELLIGENCE
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔬 Soil Chemistry Intelligence", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text("Simulate soil health testing algorithms", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))

                Text("Select Soil Class/Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                var expandedSoilList by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedButton(
                        onClick = { expandedSoilList = true },
                        modifier = Modifier.fillMaxWidth().testTag("soil_type_selector_btn")
                    ) {
                        Text(activeSoilType, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    DropdownMenu(expanded = expandedSoilList, onDismissRequest = { expandedSoilList = false }) {
                        listOf("Loam/Garden", "Clayey", "Sandy", "Black Cotton").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    viewModel.soilType.value = item
                                    expandedSoilList = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.triggerSoilIntelligence() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("trigger_soil_intel_btn")
                ) {
                    Text("Analyze Soil Score & Nutrition 🔬", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (isSoilLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = BrandGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extracting soil minerals and moisture index...", fontSize = 11.sp, color = BrandGreen)
                    }
                }

                soilResult?.let { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TintCap)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🧪 **Soil Quality Analysis:**", fontSize = 13.sp, color = BrandGreen, fontWeight = FontWeight.Bold)
                            Text("• **Overall Health Score**: ${item["score"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **NPK Distribution**: ${item["nutrients"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **Custom Fertilizers**: ${item["fertilizer"]}", fontSize = 12.sp, color = TextPrimary)
                            Text("• **Gardening Tips**: ${item["tips"]}", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🤖 CHAT TAB - PrakritiMitra AI Trilingual Chatbot
// ==========================================
@Composable
fun WebChatTab(viewModel: EcoViewModel, speakOut: (String, String) -> Unit) {
    val chats by viewModel.chatEntries.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()
    val isVoiceRecording by viewModel.isVoiceRecording.collectAsState()
    val activeLang by viewModel.selectedLanguage.collectAsState()

    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Clickable prompt triggers based on active preferred language
    val promptExamples = when (activeLang) {
        "Hindi" -> listOf(
            "नीम के पौधे को कितना पानी देना चाहिए?",
            "उपयुक्त पौधे दिखाओ",
            "तुलसी में पीली पत्तियां क्यों होती हैं?"
        )
        "Telugu" -> listOf(
            "వేప మొక్కకు ఎంత నీరు పోయాలి?",
            "సరిపోయే మొక్కలు చూపించు",
            "తులసి ఆకులు ఎందుకు పసుపు రంగులోకి మారుతాయి?"
        )
        else -> listOf(
            "How often should I water a neem plant?",
            "Show suitable plants",
            "Why are my tulsi leaves turning yellow?"
        )
    }

    LaunchedEffect(chats.size) {
        if (chats.isNotEmpty()) {
            lazyListState.animateScrollToItem(chats.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // AI Prakriti Mitra Intro card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("PrakritiMitra AI Active", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(
                        text = when(activeLang) {
                            "Hindi" -> "पूछें: उपयुक्त पौधे, जल प्रबंधन, पत्ती रोग उपचार और संरक्षण।"
                            "Telugu" -> "మొక్కల పెంపకం, నీటి యాజమాన్యం మరియు తెగుళ్ళ గురించి నన్ను అడగండి."
                            else -> "Ask anything in English, Hindi, or Telugu about smart farming, water, and tree planting."
                        },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Chat lists
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(0.5.dp, BorderSage, RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            if (chats.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌿", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your conversation with PrakritiMitra is empty.", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Click on any example questions below to start gardening discussions!", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp))
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chats) { chat ->
                        BubbleMessageRow(chat, onSpeakClick = { text -> speakOut(text, chat.detectedLanguage) })
                    }

                    if (isAILoading) {
                        item {
                            PrakritiLoadingCard()
                        }
                    }
                }
            }
        }

        // Preset quick query options
        if (chats.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tap to Ask Mitra immediately:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                promptExamples.forEach { tag ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TintCap),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.chatInput.value = tag
                                viewModel.submitChatMessage()
                            },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            color = BrandGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        if (isVoiceRecording) {
            SimulatedWaveformBlock()
        }

        // Send bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speech activation mic
            IconButton(
                onClick = { viewModel.toggleVoiceRecording() },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isVoiceRecording) Color(0xFFFF5252) else TintCap)
                    .testTag("microphone_speech_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Simulated Dictation Mode",
                    tint = if (isVoiceRecording) Color.White else BrandGreen
                )
            }

            OutlinedTextField(
                value = chatInput,
                onValueChange = { viewModel.chatInput.value = it },
                placeholder = { Text("Ask PrakritiMitra AI sum...", fontSize = 13.sp) },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("chat_input_text_field"),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submitChatMessage()
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(BrandGreen)
                    .testTag("chat_send_message_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun BubbleMessageRow(chat: ChatEntry, onSpeakClick: (String) -> Unit) {
    val isUser = chat.sender == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) BrandGreen else Color(0xFFF1F5F1)
    val textC = if (isUser) Color.White else TextPrimary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 12.dp
                    )
                )
                .background(bgColor)
                .padding(10.dp)
        ) {
            Column {
                if (chat.detectedLanguage.isNotBlank() && !isUser) {
                    Text(
                        text = "Lang: ${chat.detectedLanguage}",
                        color = Color(0xFF16A34A).copy(alpha = 0.8f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = chat.text,
                    color = textC,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Voice output out loud audio toggle button inside chatbot response
                if (!isUser) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { onSpeakClick(chat.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Listen out-loud",
                                tint = BrandGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = if (isUser) "Student" else "PrakritiMitra AI",
            fontSize = 9.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
fun PrakritiLoadingCard() {
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
                .background(TintCap),
            contentAlignment = Alignment.Center
        ) {
            Text("🤖", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "PrakritiMitra is thinking...",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SimulatedWaveformBlock() {
    val transition = rememberInfiniteTransition(label = "wave")
    val sizeF by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "size"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF1EB))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Listening Speech Command...", color = Color(0xFFE65100), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.size(4.dp, 12.dp * sizeF).background(Color(0xFFE65100)))
        Spacer(modifier = Modifier.width(3.dp))
        Box(modifier = Modifier.size(4.dp, 20.dp * sizeF).background(Color(0xFFE65100)))
        Spacer(modifier = Modifier.width(3.dp))
        Box(modifier = Modifier.size(4.dp, 12.dp * sizeF).background(Color(0xFFE65100)))
    }
}

// ==========================================
// 👤 PROFILE TAB - Badges and Metrics
// ==========================================
@Composable
fun StudentProfileTab(viewModel: EcoViewModel) {
    val session by viewModel.sessionState.collectAsState()
    val badgesList = (session?.badges ?: "Green Starter").split(",").map { it.trim() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("👤 Student Academic Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // Details card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "🎓 Student Academic Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandGreen)
                Divider(color = BorderSage)
                
                ProfilePropertyRow(label = "Full Name", value = session?.fullName ?: "Rahul Kumar")
                ProfilePropertyRow(label = "School/Academy", value = session?.school ?: "Secunderabad Green College")
                ProfilePropertyRow(label = "Grade/Standard", value = session?.studentClass ?: "Class 10")
                ProfilePropertyRow(label = "City/District", value = session?.city ?: "Hyderabad")
                ProfilePropertyRow(label = "State", value = session?.state ?: "Telangana")
                ProfilePropertyRow(label = "Favorite Cultivar", value = session?.favoritePlant ?: "Organic Tulsi")
                ProfilePropertyRow(label = "Annual Goal Target", value = "${session?.plantationGoal ?: 5} Plants, ${session?.treesToGrow ?: 2} Trees")
            }
        }

        // Achievements / Medals
        Text("🥈 Unlocked Eco Badges", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        
        BadgeItemRow(
            title = "🥉 Green Starter",
            desc = "Automatically awarded on registration and cataloging your first green seedling.",
            unlocked = badgesList.contains("Green Starter")
        )

        BadgeItemRow(
            title = "🥈 Tree Guardian",
            desc = "Awarded once your registered campus garden reaches 1300 Eco Points.",
            unlocked = badgesList.contains("Tree Guardian")
        )

        BadgeItemRow(
            title = "🥇 Eco Champion",
            desc = "Highest student eco status. Awarded once you analyze diseases and hit 1500 Eco Points.",
            unlocked = badgesList.contains("Eco Champion")
        )

        // Standings leaderboard to motivate students
        Text("🏆 Local Campus Leaderboard", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSage, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                UserBoardRow(rank = 1, name = "Anjali Rao (Class 10)", points = 1650, isSelf = false)
                UserBoardRow(rank = 2, name = "${session?.fullName ?: "Rahul Kumar"} (You)", points = session?.ecoPoints ?: 1200, isSelf = true)
                UserBoardRow(rank = 3, name = "Vikram Singh (Class 9)", points = 950, isSelf = false)
            }
        }
    }
}

@Composable
fun ProfilePropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun BadgeItemRow(title: String, desc: String, unlocked: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) TintCap else Color(0x0A000000)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (unlocked) BrandGreen else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) BorderSage else Color(0x1F000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (unlocked) "🏅" else "🔒", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = if (unlocked) TextPrimary else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = desc,
                    color = if (unlocked) TextSecondary else Color.DarkGray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun UserBoardRow(rank: Int, name: String, points: Int, isSelf: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelf) TintCap else Color.Transparent)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("#$rank", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = if (rank == 1) Color(0xFFD38D10) else TextPrimary, modifier = Modifier.width(24.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(name, fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp, color = TextPrimary)
        }
        Text("$points XP", color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = BorderSage, shape = RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text("📍 $location", fontSize = 11.sp, color = TextSecondary)
                Text(
                    text = if (lastWateredHours == 0) "✅ Just Watered Now" else "⚠️ Last watered: $lastWateredHours hours ago",
                    fontSize = 11.sp,
                    color = if (lastWateredHours == 0) BrandGreen else Color(0xFFC26100),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Button(
                onClick = onWaterPressed,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("water_action_button_${name.take(4)}")
            ) {
                Text("Water 💧", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

