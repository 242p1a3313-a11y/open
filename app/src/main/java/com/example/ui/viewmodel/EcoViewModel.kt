package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EcoDatabase
import com.example.data.model.ChatEntry
import com.example.data.model.UserSession
import com.example.data.repository.EcoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class EcoScreen {
    LOGIN,
    REGISTER_S1,
    REGISTER_S2,
    REGISTER_S3,
    VERIFICATION,
    PROFILE_SETUP,
    MAIN_DASHBOARD
}

class EcoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EcoRepository

    // Base Database flows
    val sessionState: StateFlow<UserSession?>
    val chatEntries: StateFlow<List<ChatEntry>>

    // Current screen
    private val _currentScreen = MutableStateFlow(EcoScreen.LOGIN)
    val currentScreen: StateFlow<EcoScreen> = _currentScreen

    // Selected Language
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    // Login Form Inputs
    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    private val _loginError = MutableStateFlow("")
    val loginError: StateFlow<String> = _loginError

    // Step 1 Reg Inputs
    val regName = MutableStateFlow("")
    val regEmail = MutableStateFlow("")
    val regPassword = MutableStateFlow("")
    val regConfirmPassword = MutableStateFlow("")
    private val _regS1Error = MutableStateFlow("")
    val regS1Error: StateFlow<String> = _regS1Error

    // Step 2 Reg Inputs
    val regSchool = MutableStateFlow("")
    val regClass = MutableStateFlow("")
    val regCity = MutableStateFlow("")
    val regState = MutableStateFlow("")
    private val _regS2Error = MutableStateFlow("")
    val regS2Error: StateFlow<String> = _regS2Error

    // Phone / OTP Verification Inputs
    val phoneInput = MutableStateFlow("")
    val otpSentCode = MutableStateFlow("")
    val otpTypedCode = MutableStateFlow("")
    private val _otpStatus = MutableStateFlow("") // "IDLE", "SENT", "VERIFIED", "ERROR"
    val otpStatus: StateFlow<String> = _otpStatus

    // Email verification simulated status
    private val _emailVerified = MutableStateFlow(false)
    val emailVerified: StateFlow<Boolean> = _emailVerified

    // CAPTCHA Bot Protection Variables
    private val _captchaNum1 = MutableStateFlow(0)
    val captchaNum1: StateFlow<Int> = _captchaNum1
    private val _captchaNum2 = MutableStateFlow(0)
    val captchaNum2: StateFlow<Int> = _captchaNum2
    val captchaUserInput = MutableStateFlow("")
    private val _captchaSuccess = MutableStateFlow(false)
    val captchaSuccess: StateFlow<Boolean> = _captchaSuccess
    private val _captchaError = MutableStateFlow("")
    val captchaError: StateFlow<String> = _captchaError

    // Profile Setup inputs
    val favoritePlant = MutableStateFlow("")
    val setupLocation = MutableStateFlow("")
    val setupGoal = MutableStateFlow("5")
    val setupTrees = MutableStateFlow("2")

    // Primary Chat / Prompt State
    val chatInput = MutableStateFlow("")
    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading

    // Voice Command / Simulated Speech
    private val _isVoiceRecording = MutableStateFlow(false)
    val isVoiceRecording: StateFlow<Boolean> = _isVoiceRecording

    // Leaf Scanner State
    private val _scanProgress = MutableStateFlow<String?>(null) // "INIT", "SCANNING", "ANALYZING", "RESULT_DISEASE", "RESULT_HEALTHY", null
    val scanProgress: StateFlow<String?> = _scanProgress
    private val _scannedPlantName = MutableStateFlow("")
    val scannedPlantName: StateFlow<String> = _scannedPlantName
    private val _scanDiagnostics = MutableStateFlow("")
    val scanDiagnostics: StateFlow<String> = _scanDiagnostics
    private val _scanTreatment = MutableStateFlow("")
    val scanTreatment: StateFlow<String> = _scanTreatment

    // 1. Smart Plantation recommendation state
    val recLocation = MutableStateFlow("")
    val recSeason = MutableStateFlow("Summer")
    val recSoil = MutableStateFlow("Loamy")
    val recSpace = MutableStateFlow("Medium Bed")
    private val _recResult = MutableStateFlow<Map<String, String>?>(null)
    val recResult: StateFlow<Map<String, String>?> = _recResult
    private val _isRecLoading = MutableStateFlow(false)
    val isRecLoading: StateFlow<Boolean> = _isRecLoading

    // 2. Soil Intelligence state
    val soilType = MutableStateFlow("Loam")
    private val _soilResult = MutableStateFlow<Map<String, String>?>(null)
    val soilResult: StateFlow<Map<String, String>?> = _soilResult
    private val _isSoilLoading = MutableStateFlow(false)
    val isSoilLoading: StateFlow<Boolean> = _isSoilLoading

    // 3. Water calculation state
    val waterPlantType = MutableStateFlow("Organic Tulsi")
    val waterWeather = MutableStateFlow("Sunny")
    private val _waterResult = MutableStateFlow<String>("")
    val waterResult: StateFlow<String> = _waterResult

    // 4. Climate Suitability state
    val climateTemp = MutableStateFlow("32")
    val climateRain = MutableStateFlow("150")
    val climateHumidity = MutableStateFlow("60")
    private val _climateResult = MutableStateFlow<String>("")
    val climateResult: StateFlow<String> = _climateResult

    // 5. Growth Prediction AI state
    val growthPlantName = MutableStateFlow("Organic Tulsi")
    val growthDays = MutableStateFlow("30")
    private val _growthResult = MutableStateFlow<Map<String, String>?>(null)
    val growthResult: StateFlow<Map<String, String>?> = _growthResult
    private val _isGrowthLoading = MutableStateFlow(false)
    val isGrowthLoading: StateFlow<Boolean> = _isGrowthLoading

    // Active Dashboard Tab
    private val _activeTab = MutableStateFlow("Home") // "Home", "Plants", "Scan", "Chat", "Profile"
    val activeTab: StateFlow<String> = _activeTab

    init {
        val database = EcoDatabase.getDatabase(application)
        repository = EcoRepository(database.userSessionDao(), database.chatDao())
        sessionState = repository.sessionFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        chatEntries = repository.allChatsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize CAPTCHA
        generateNewCaptcha()

        // Sync local DB session check
        viewModelScope.launch {
            val session = repository.getSession()
            if (session != null && session.isLoggedIn) {
                _selectedLanguage.value = session.preferredLanguage
                _currentScreen.value = EcoScreen.MAIN_DASHBOARD
            } else {
                _currentScreen.value = EcoScreen.LOGIN
            }
        }
    }

    // CAPTCHA Generation
    fun generateNewCaptcha() {
        _captchaNum1.value = Random.nextInt(1, 10)
        _captchaNum2.value = Random.nextInt(1, 10)
        captchaUserInput.value = ""
        _captchaSuccess.value = false
        _captchaError.value = ""
    }

    fun verifyCaptcha(): Boolean {
        val expected = _captchaNum1.value + _captchaNum2.value
        val typed = captchaUserInput.value.toIntOrNull()
        return if (typed == expected) {
            _captchaSuccess.value = true
            _captchaError.value = ""
            true
        } else {
            _captchaSuccess.value = false
            _captchaError.value = "Incorrect. Try again!"
            false
        }
    }

    // Password strength check
    fun checkPasswordStrength(password: String): String {
        if (password.isEmpty()) return "None"
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it.isLetter() && !it.isLetterOrDigit() || "@#$%^&+=!".contains(it) }) score++
        return when (score) {
            0, 1 -> "Weak"
            2 -> "Medium"
            3 -> "Strong"
            else -> "Very Strong"
        }
    }

    // Nav Helpers
    fun navigateTo(screen: EcoScreen) {
        _currentScreen.value = screen
    }

    fun setDashboardTab(tab: String) {
        _activeTab.value = tab
    }

    fun selectLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    // Login Action
    fun performLogin() {
        if (loginEmail.value.isBlank() || loginPassword.value.isBlank()) {
            _loginError.value = "Please fill in all email and password fields"
            return
        }
        if (!loginEmail.value.contains("@")) {
            _loginError.value = "Please enter a valid email address"
            return
        }

        viewModelScope.launch {
            _loginError.value = ""
            // Mock authentication, create user session
            val session = UserSession(
                isLoggedIn = true,
                email = loginEmail.value,
                fullName = loginEmail.value.substringBefore("@").replaceFirstChar { it.uppercase() },
                school = "Green Academy High",
                studentClass = "Grade 9",
                city = "Hyderabad",
                state = "Telangana",
                preferredLanguage = "English"
            )
            repository.saveSession(session)
            _selectedLanguage.value = "English"
            _currentScreen.value = EcoScreen.MAIN_DASHBOARD
        }
    }

    // Direct Login for Social options (Google, Microsoft)
    fun performSocialLogin(provider: String) {
        viewModelScope.launch {
            val email = if (provider == "Google") "student_google@cbit.edu.in" else "student_ms@cbit.edu.in"
            val session = UserSession(
                isLoggedIn = true,
                email = email,
                fullName = "Rahul Kumar",
                school = "CBIT College",
                studentClass = "A Grade",
                city = "Hyderabad",
                state = "Telangana",
                preferredLanguage = "English",
                ecoPoints = 1200,
                streak = 15,
                badges = "Green Starter,Tree Guardian"
            )
            repository.saveSession(session)
            _selectedLanguage.value = "English"
            _currentScreen.value = EcoScreen.MAIN_DASHBOARD
        }
    }

    // Phone / OTP Verification Simulators
    fun sendOTP() {
        if (phoneInput.value.length < 10) {
            _otpStatus.value = "ERROR"
            return
        }
        // Mock sending
        _otpStatus.value = "SENT"
        otpSentCode.value = "123456" // Simple constant OTP
    }

    fun verifyOTP() {
        if (otpTypedCode.value == otpSentCode.value) {
            _otpStatus.value = "VERIFIED"
            // Auto sign in via phone OTP
            viewModelScope.launch {
                val session = UserSession(
                    isLoggedIn = true,
                    email = "otp_user@ecofriend.com",
                    fullName = "Phone User",
                    school = "Green School of Hyderabad",
                    studentClass = "Grade 10",
                    city = "Secunderabad",
                    state = "Telangana",
                    preferredLanguage = "Telugu",
                    ecoPoints = 250,
                    streak = 1
                )
                repository.saveSession(session)
                _selectedLanguage.value = "Telugu"
                _currentScreen.value = EcoScreen.MAIN_DASHBOARD
            }
        } else {
            _otpStatus.value = "ERROR"
        }
    }

    // Registration Flow Steps
    fun submitRegS1() {
        if (regName.value.isBlank() || regEmail.value.isBlank() || regPassword.value.isBlank()) {
            _regS1Error.value = "All fields are required"
            return
        }
        if (!regEmail.value.contains("@")) {
            _regS1Error.value = "Invalid email format"
            return
        }
        if (regPassword.value != regConfirmPassword.value) {
            _regS1Error.value = "Passwords do not match"
            return
        }
        if (checkPasswordStrength(regPassword.value) == "Weak") {
            _regS1Error.value = "Password is too weak. Make it stronger!"
            return
        }

        _regS1Error.value = ""
        // Proceed to Step 2
        _currentScreen.value = EcoScreen.REGISTER_S2
    }

    fun submitRegS2() {
        if (regSchool.value.isBlank() || regClass.value.isBlank() || regCity.value.isBlank() || regState.value.isBlank()) {
            _regS2Error.value = "All school/academic details are required"
            return
        }
        _regS2Error.value = ""
        _currentScreen.value = EcoScreen.REGISTER_S3
    }

    fun completeRegistration() {
        // Step 1, 2, 3 complete. Trigger Email Verification screen.
        _currentScreen.value = EcoScreen.VERIFICATION
    }

    fun simulateEmailActivation() {
        // User clicks "Simulated Verification Link"
        _emailVerified.value = true
        // Redirect to profile setup
        _currentScreen.value = EcoScreen.PROFILE_SETUP
    }

    fun completeProfileSetup() {
        viewModelScope.launch {
            val goalInt = setupGoal.value.toIntOrNull() ?: 5
            val treesInt = setupTrees.value.toIntOrNull() ?: 2
            val session = UserSession(
                isLoggedIn = true,
                email = regEmail.value.ifBlank { "new_student@ecofriend.com" },
                fullName = regName.value.ifBlank { "Rahul" },
                school = regSchool.value.ifBlank { "CBIT Campus" },
                studentClass = regClass.value.ifBlank { "Grade 9" },
                city = regCity.value.ifBlank { "Hyderabad" },
                state = regState.value.ifBlank { "Telangana" },
                preferredLanguage = _selectedLanguage.value,
                favoritePlant = favoritePlant.value.ifBlank { "Tulsi" },
                location = setupLocation.value.ifBlank { "College Balcony" },
                plantationGoal = goalInt,
                treesToGrow = treesInt,
                streak = 1,
                ecoPoints = 50, // Starting point reward!
                badges = "Green Starter"
            )
            repository.saveSession(session)
            _currentScreen.value = EcoScreen.MAIN_DASHBOARD
        }
    }

    // Primary Chat System using direct language tracking
    fun detectLanguage(text: String): String {
        var containsHindi = false
        var containsTelugu = false
        for (char in text) {
            val code = char.code
            if (code in 0x0900..0x097F) {
                containsHindi = true
            } else if (code in 0x0C00..0x0C7F) {
                containsTelugu = true
            }
        }
        return when {
            containsTelugu -> "Telugu"
            containsHindi -> "Hindi"
            else -> "English"
        }
    }

    fun submitChatMessage() {
        val message = chatInput.value
        if (message.isBlank()) return

        chatInput.value = ""
        val userLang = detectLanguage(message)

        val cleanMsg = message.trim().lowercase().removeSuffix(".")
        val isSuitableCheck = cleanMsg.contains("suitable plants") || 
            cleanMsg.contains("show suitable plants") || 
            cleanMsg.contains("उपयुक्त पौधे दिखाओ") || 
            cleanMsg.contains("సరిపోయే మొక్కలు చూపించు") ||
            cleanMsg.contains("మొక్కలు చూపించు")

        if (isSuitableCheck) {
            _activeTab.value = "Plants"
            viewModelScope.launch {
                val userChat = ChatEntry(
                    sender = "user",
                    text = message,
                    detectedLanguage = userLang
                )
                repository.insertChat(userChat)
                
                val redirectMsg = when (userLang) {
                    "Hindi" -> "🌿 जी हुज़ूर! आपकी आवाज़ की कमान को माना गया है। आपको उपयुक्त पौधे की सिफारिशों वाले अनुभाग (Plants) में निर्देशित कर दिया गया है। कृपया फ़ॉर्म भरकर सही पौधे का चुनाव करें।"
                    "Telugu" -> "🌿 చిత్తం! మీ వాయిస్ కమాండ్ ప్రకారం సంపూర్ణ మొక్కల సిఫార్సు విభాగంలో (Plants) మిమ్మల్ని చేర్చాను. దయచేసి వివరాలు నింపండి."
                    else -> "🌿 Understood! Processing your voice command. Redirecting you to the Smart Plantation Recommendation page under the Plants tab."
                }
                
                val aiChat = ChatEntry(
                    sender = "prakriti_mitra",
                    text = redirectMsg,
                    detectedLanguage = userLang
                )
                repository.insertChat(aiChat)
                
                val currentPoints = sessionState.value?.ecoPoints ?: 0
                repository.updateEcoPoints(currentPoints + 20)
            }
            return
        }

        viewModelScope.launch {
            // First save user message in chat entries database
            val userChat = ChatEntry(
                sender = "user",
                text = message,
                detectedLanguage = userLang
            )
            repository.insertChat(userChat)

            _isAILoading.value = true

            // Send to PrakritiMitra repository handler
            val replyText = repository.sendMessageToPrakritiMitra(
                userInput = message,
                detectedLang = userLang,
                chatHistory = chatEntries.value
            )

            // Save reply in chat entries
            val aiChat = ChatEntry(
                sender = "prakriti_mitra",
                text = replyText,
                detectedLanguage = userLang
            )
            repository.insertChat(aiChat)

            // Add Eco Points for active learning with PrakritiMitra!
            val currentPoints = sessionState.value?.ecoPoints ?: 0
            repository.updateEcoPoints(currentPoints + 20)

            _isAILoading.value = false
        }
    }

    // Voice Commands simulation (shows speech recognition matching the language code)
    fun toggleVoiceRecording() {
        if (!_isVoiceRecording.value) {
            _isVoiceRecording.value = true
        } else {
            _isVoiceRecording.value = false
            // Complete speaking, inject high-quality command match based on language code
            val presetVoices = when (_selectedLanguage.value) {
                "Hindi" -> listOf(
                    "उपयुक्त पौधे दिखाओ",
                    "एक पेड़ सुझाओ",
                    "आज कितना पानी देना है?",
                    "पत्ती स्कैन करो"
                )
                "Telugu" -> listOf(
                    "సరిపోయే మొక్కలు చూపించు",
                    "ఒక చెట్టును సూచించు",
                    "ఈరోజు ఎంత నీరు పోయాలి?",
                    "ఆకును స్కాన్ చేయండి"
                )
                else -> listOf(
                    "Show suitable plants",
                    "Recommend a tree",
                    "Best plant for summer",
                    "Water requirements",
                    "Scan leaf"
                )
            }
            val simulatedSpeechText = presetVoices.random()
            chatInput.value = simulatedSpeechText
            submitChatMessage()
        }
    }

    // Simulated Leaf Diagnostic Scanner
    fun triggerLeafScan() {
        viewModelScope.launch {
            _scanProgress.value = "INIT"
            kotlinx.coroutines.delay(1000)
            _scanProgress.value = "SCANNING"
            kotlinx.coroutines.delay(1500)
            _scanProgress.value = "ANALYZING"
            kotlinx.coroutines.delay(1500)

            val isHealthy = Random.nextBoolean()
            val lang = _selectedLanguage.value

            if (isHealthy) {
                _scanProgress.value = "RESULT_HEALTHY"
                if (lang == "Hindi") {
                    _scannedPlantName.value = "तुलसी (Holy Basil)"
                    _scanDiagnostics.value = "पत्ती की स्थिति: उत्तम और पूरी तरह स्वस्थ! प्रकाश संश्लेषण बिल्कुल सही लय में हो रहा है।"
                    _scanTreatment.value = "उपचार: नियमित रूप से प्रतिदिन सुबह थोड़ी मात्रा में पानी दें और कोमल पत्तों को सीधी तेज धूप से बचाएं।"
                } else if (lang == "Telugu") {
                    _scannedPlantName.value = "తులసి మొక్క (Holy Basil)"
                    _scanDiagnostics.value = "ఆకు కండిషన్: చాలా ఆరోగ్యంగా ఉంది! సూర్యకాంతి గ్రహింపు మరియు పెరుగుదల అద్భుతంగా ఉన్నాయి."
                    _scanTreatment.value = "చికిత్స: క్రమం తప్పకుండా ప్రతిరోజూ ఉదయం నీరు పోయండి, ఎక్కువ ఎండ నుండి రక్షించండి."
                } else {
                    _scannedPlantName.value = "Holy Basil (Tulsi)"
                    _scanDiagnostics.value = "Leaf Condition: Excellent & Vibrant! Photosynthesis is active, leaf veins show optimal chlorophyll distribution."
                    _scanTreatment.value = "Care Tips: Maintain present watering cycles. Add organic manure once in a month; keep in semi-shade."
                }
            } else {
                _scanProgress.value = "RESULT_DISEASE"
                if (lang == "Hindi") {
                    _scannedPlantName.value = "गुलाब (Rose Bush)"
                    _scanDiagnostics.value = "बीमारी का पता चला: पत्ती धब्बा संक्रमण (Leaf Spot Fungal Disease - अल्टरनेरिया)"
                    _scanTreatment.value = "उपचार: प्रभावित पत्तों को काटें। एक लीटर पानी में एक चम्मच नीम का तेल और थोड़ा बेकिंग सोडा मिलाकर स्प्रे तैयार करें और पत्तों पर छिड़कें।"
                } else if (lang == "Telugu") {
                    _scannedPlantName.value = "గులాబీ మొక్క (Rose Bush)"
                    _scanDiagnostics.value = "గుర్తించిన వ్యాధి: బ్లాక్ స్పాట్ ఫంగల్ వ్యాధి (Fungal Spot Disease)"
                    _scanTreatment.value = "చికిత్స: వ్యాధి సోకిన ఆకులను కత్తిరించండి. వేప నూనెను నీటిలో కలిపి వారానికి ఒకసారి ఆకులపై పిచికారీ చేయండి."
                } else {
                    _scannedPlantName.value = "Rose Bush"
                    _scanDiagnostics.value = "Detected Disease: Black Spot Fungal Infection (Diplocarpon rosae)"
                    _scanTreatment.value = "Treatment Plan: Remove the spotted leaves. Spray organic fungicide or a homemade solution of 1 tbsp baking soda + 1 tsp horticulture neem oil in 1 liter of warm water."
                }
            }

            // Reward student with Eco points for diagnostic analysis!
            val session = sessionState.value
            if (session != null) {
                val newPoints = session.ecoPoints + 100
                repository.updateEcoPoints(newPoints)
                // If streak not updated today, we can increment streak!
                repository.updateStreak(session.streak + 1)

                // Check and unlock new badges!
                val badgesList = session.badges.split(",").map { it.trim() }.toMutableList()
                if (newPoints >= 1500 && !badgesList.contains("Eco Champion")) {
                    badgesList.add("Eco Champion")
                    repository.updateBadges(badgesList.joinToString(","))
                } else if (newPoints >= 1300 && !badgesList.contains("Tree Guardian")) {
                    badgesList.add("Tree Guardian")
                    repository.updateBadges(badgesList.joinToString(","))
                }
            }
        }
    }

    fun clearScan() {
        _scanProgress.value = null
        _scannedPlantName.value = ""
        _scanDiagnostics.value = ""
        _scanTreatment.value = ""
    }

    // Water plant and update eco points & badge unlock accomplishments
    fun waterPlant(pointsAdded: Int = 15) {
        viewModelScope.launch {
            val session = sessionState.value
            if (session != null) {
                val newPoints = session.ecoPoints + pointsAdded
                repository.updateEcoPoints(newPoints)

                val badgesList = session.badges.split(",").map { it.trim() }.toMutableList()
                if (newPoints >= 1500 && !badgesList.contains("Eco Champion")) {
                    badgesList.add("Eco Champion")
                    repository.updateBadges(badgesList.joinToString(","))
                } else if (newPoints >= 1300 && !badgesList.contains("Tree Guardian")) {
                    badgesList.add("Tree Guardian")
                    repository.updateBadges(badgesList.joinToString(","))
                }
            }
        }
    }

    fun triggerSmartRecommendation() {
        viewModelScope.launch {
            _isRecLoading.value = true
            kotlinx.coroutines.delay(1200)
            val loc = recLocation.value.ifBlank { "Balcony Studio" }
            val seas = recSeason.value
            val soil = recSoil.value
            val space = recSpace.value

            val plantResult = when {
                seas == "Summer" && soil == "Sandy" -> mapOf(
                    "plant" to "Aloe Vera 🌵",
                    "growth" to "60 - 90 Days",
                    "maintenance" to "Low (Water weekly)",
                    "benefits" to "Air purification, skin healing, and low water strain."
                )
                seas == "Summer" -> mapOf(
                    "plant" to "Organic Tulsi 🌱",
                    "growth" to "45 - 60 Days",
                    "maintenance" to "Medium (Keep soil moist)",
                    "benefits" to "High Oxygen yield, medicinal property herb, mosquito repelling."
                )
                seas == "Winter" -> mapOf(
                    "plant" to "Golden Marigold 🌼",
                    "growth" to "50 - 70 Days",
                    "maintenance" to "Medium (Needs daily sun)",
                    "benefits" to "Beautification, pollinators magnet, and organic pest repelling."
                )
                seas == "Monsoon" || seas == "Rainy" -> mapOf(
                    "plant" to "Vibrant Mint Leaf 🌿",
                    "growth" to "30 - 45 Days",
                    "maintenance" to "Low (Thrives in wet soil)",
                    "benefits" to "Culinary uses, delicious mild aroma, fast propagation."
                )
                else -> mapOf(
                    "plant" to "Neem Tree Sapling 🌳",
                    "growth" to "120 - 180 Days (Early stage)",
                    "maintenance" to "Low (Highly resilient)",
                    "benefits" to "Gigantic carbon sequestration, natural insect pesticide, cooling shade."
                )
            }
            _recResult.value = plantResult
            _isRecLoading.value = false
        }
    }

    fun triggerSoilIntelligence() {
        viewModelScope.launch {
            _isSoilLoading.value = true
            kotlinx.coroutines.delay(1000)
            val st = soilType.value
            val result = when (st) {
                "Clayey" -> mapOf(
                    "score" to "65/100 ⚠️ Heavy Clay",
                    "nutrients" to "Nitrogen (Medium), Phosphorus (Low), Potassium (High)",
                    "fertilizer" to "Mix Organic Vermicompost with sand to loosen soil structure.",
                    "tips" to "Water slowly to prevent pooling. Great moisture retention but poor aeration."
                )
                "Sandy" -> mapOf(
                    "score" to "58/100 ⚠️ Low Retentive",
                    "nutrients" to "Nitrogen (Very Low), Phosphorus (Low), Potassium (Low)",
                    "fertilizer" to "Add thick layers of decomposed cow manure or peat and leaf mold.",
                    "tips" to "Incorporate mulching to avoid swift moisture loss. Add nutrients frequently."
                )
                "Loamy", "Loam", "Loam/Garden" -> mapOf(
                    "score" to "92/100  Highly Fertile!",
                    "nutrients" to "Nitrogen (High), Phosphorus (Medium), Potassium (High)",
                    "fertilizer" to "Light compost dressing or organic seaweed extract once a month.",
                    "tips" to "Ideal garden soil! Balanced drainage and nutrition retention."
                )
                "Black Cotton" -> mapOf(
                    "score" to "80/100  Highly Nutrient Rich",
                    "nutrients" to "Nitrogen (Low), Phosphorus (Medium), Calcium & Iron (High)",
                    "fertilizer" to "Use nitrogen-rich organic oilcakes (Neem Cake) and organic fertilizers.",
                    "tips" to "Provides deep moisture retention. Aerate soil occasionally to let roots breathe."
                )
                else -> mapOf(
                    "score" to "75/100 👍 Standard Soil",
                    "nutrients" to "Nitrogen (Medium), Phosphorus (Medium), Potassium (Medium)",
                    "fertilizer" to "Apply dry compost manure every 3 weeks to keep nutrient flow stable.",
                    "tips" to "Rake periodically to enhance top surface oxygen levels."
                )
            }
            _soilResult.value = result
            _isSoilLoading.value = false
        }
    }

    fun triggerWaterCalculation() {
        val type = waterPlantType.value.lowercase()
        val weather = waterWeather.value
        val amount = when {
            type.contains("tulsi") || type.contains("basil") -> if (weather == "Sunny") 350 else 200
            type.contains("mint") -> if (weather == "Sunny") 400 else 250
            type.contains("neem") -> if (weather == "Sunny") 600 else 300
            type.contains("rose") -> if (weather == "Sunny") 500 else 300
            else -> if (weather == "Sunny") 450 else 250
        }
        val reminder = when (weather) {
            "Sunny" -> "☀️ Peak Heat Alert: Soil will evaporate water 40% faster. Water early in the morning (before 9 AM) or evening (after 5 PM) to protect roots."
            "Cloudy" -> "☁️ Sky Covered: Low evaporation rates. Do second watering check only if topmost soil is dry. Saves water!"
            "Rainy" -> "🌧️ Natural Rains Active: Skip watering entirely today. Move balcony plants outside to collect clean, chemical-free rainwater!"
            else -> "🌤️ Pleasant Day: Standard moisture rate. Give one normal splash today."
        }
        _waterResult.value = "🌱 **Daily Water Calculation Result:**\n" +
                "• Target Plant: ${waterPlantType.value}\n" +
                "• Recommended Amount: **$amount ml 💧**\n\n" +
                "**Weather Reminders:**\n" +
                "$reminder\n\n" +
                "**Eco Water Tips:**\n" +
                "1. Use left-over water from washing rice/veggies to enrich soil with natural starches!\n" +
                "2. Try drip watering by keeping a bottle with a pin-hole nearby."
    }

    fun triggerClimateAnalysis() {
        val tempVal = climateTemp.value.toIntOrNull() ?: 30
        val rainVal = climateRain.value.toIntOrNull() ?: 120
        val humVal = climateHumidity.value.toIntOrNull() ?: 60

        val tempFeedback = when {
            tempVal > 38 -> "❌ Extreme Heat ($tempVal°C): High risk of transpiration shock!"
            tempVal in 20..35 -> "✅ Ideal warm temperature range for tropical/subtropical herbs."
            else -> "⚠️ Mild Cool ($tempVal°C): Plant metabolic process slows down."
        }
        val rainFeedback = when {
            rainVal > 250 -> "⚠️ Excess Rain ($rainVal mm/mon): Avoid clayey soils, roots might drown."
            rainVal in 80..200 -> "✅ Balanced Rainfall: Ideal natural irrigation levels."
            else -> "⚠️ Arid Rain ($rainVal mm/mon): Relies completely on manual irrigation."
        }
        val humidityFeedback = when {
            humVal > 80 -> "⚠️ High Humidity ($humVal%): Excellent for ferns but increases fungal risk."
            humVal < 40 -> "⚠️ Dry Air ($humVal%): Accelerates leaf drying. Mist plants occasionally."
            else -> "✅ Perfect humidity index ($humVal%) for general plant breathing."
        }

        _climateResult.value = """
            🌡️ **Climate Suitability Summary:**
            • $tempFeedback
            • $rainFeedback
            • $humidityFeedback
            
            🗓️ **Season Action Plan:**
            Current combination represents a ${if (tempVal in 20..35 && humVal in 40..80) "Highly Friendly" else "Challenging"} atmosphere. Plan water supply meticulously!
        """.trimIndent()
    }

    fun triggerGrowthPrediction() {
        viewModelScope.launch {
            _isGrowthLoading.value = true
            kotlinx.coroutines.delay(1200)
            val plant = growthPlantName.value
            val daysStr = growthDays.value.toIntOrNull() ?: 30
            
            val rate = when {
                plant.lowercase().contains("tulsi") -> 0.7f
                plant.lowercase().contains("neem") -> 1.2f
                plant.lowercase().contains("mint") -> 0.9f
                else -> 0.8f
            }
            val height = (daysStr * rate).coerceAtLeast(2.0f)
            val stage = when {
                daysStr < 10 -> "🌱 Seedling / Sprout Stage"
                daysStr < 30 -> "🌿 Active Vegetative Growth"
                daysStr < 60 -> "🌸 Flowering & Budding Stage"
                else -> "🌳 Fully Mature / Harvest-Ready"
            }
            val yield = (height * 8.5f).toInt()
            
            _growthResult.value = mapOf(
                "height" to String.format("%.1f cm 📏", height),
                "stage" to stage,
                "harvest" to "Estimated inside ${(90 - daysStr).coerceAtLeast(7)} days",
                "yield" to "Estimated leaf harvest weight: ~${yield}g 🍃"
            )
            _isGrowthLoading.value = false
        }
    }

    // Sign Out / Clear Database
    fun signOut() {
        viewModelScope.launch {
            repository.clearSessionAndChats()
            _currentScreen.value = EcoScreen.LOGIN
        }
    }
}
