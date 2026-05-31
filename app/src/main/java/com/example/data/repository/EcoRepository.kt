package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ChatDao
import com.example.data.local.UserSessionDao
import com.example.data.model.ChatEntry
import com.example.data.model.UserSession
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EcoRepository(
    private val userSessionDao: UserSessionDao,
    private val chatDao: ChatDao
) {
    val sessionFlow: Flow<UserSession?> = userSessionDao.getSessionFlow()
    val allChatsFlow: Flow<List<ChatEntry>> = chatDao.getAllChatsFlow()

    suspend fun getSession(): UserSession? {
        return userSessionDao.getSession()
    }

    suspend fun saveSession(session: UserSession) {
        userSessionDao.saveSession(session)
    }

    suspend fun updateLoginState(isLoggedIn: Boolean) {
        userSessionDao.updateLoginState(isLoggedIn)
    }

    suspend fun updateEcoPoints(points: Int) {
        userSessionDao.updateEcoPoints(points)
    }

    suspend fun updateStreak(streak: Int) {
        userSessionDao.updateStreak(streak)
    }

    suspend fun updateBadges(badges: String) {
        userSessionDao.updateBadges(badges)
    }

    suspend fun insertChat(chat: ChatEntry) {
        chatDao.insertChat(chat)
    }

    suspend fun clearSessionAndChats() {
        userSessionDao.clearSession()
        chatDao.clearChats()
    }

    suspend fun sendMessageToPrakritiMitra(
        userInput: String,
        detectedLang: String,
        chatHistory: List<ChatEntry>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("EcoRepository", "Gemini API key is blank or placeholder, using offline simulated response.")
            return@withContext getOfflineSimulatedResponse(userInput, detectedLang)
        }

        // Construct history contents for Gemini API:
        // Filter out very old entries to keep within constraints
        val limitedHistory = chatHistory.takeLast(10)
        val contents = mutableListOf<GeminiContent>()
        
        limitedHistory.forEach { entry ->
            contents.add(GeminiContent(parts = listOf(GeminiPart(text = entry.text))))
        }
        
        // Append user's current message
        contents.add(GeminiContent(parts = listOf(GeminiPart(text = userInput))))

        val systemPrompt = """
            You are PrakritiMitra, an EcoFriend student-focused AI companion.
            
            Rules:
            1. Answer in the user's language (the input language is detected as $detectedLang). If the user asks in English, reply in English. If in Hindi, reply in Hindi. If in Telugu, reply in Telugu.
            2. Support English, Hindi and Telugu.
            3. Give plant recommendations, plantation guidance, explain diseases, and suggest treatments.
            4. Be friendly, educational, motivating, and student-focused!
            5. Use bullet points and appropriate emojis (like 🌱, 🌿, 🌳, 🌸, 💧, 🐛, 🔥) to make it highly visual.
            6. Keep answers concise, clear, and relevant.
        """.trimIndent()

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!reply.isNullOrBlank()) {
                return@withContext reply
            } else {
                return@withContext getOfflineSimulatedResponse(userInput, detectedLang)
            }
        } catch (e: Exception) {
            Log.e("EcoRepository", "Network call failed, using offline simulation fallback", e)
            return@withContext getOfflineSimulatedResponse(userInput, detectedLang)
        }
    }

    /**
     * Highly responsive offline fallback simulator matching user's commands and language
     */
    private fun getOfflineSimulatedResponse(userInput: String, language: String): String {
        val queryLower = userInput.lowercase()
        return when (language) {
            "Hindi" -> {
                when {
                    queryLower.contains("पौधे") || queryLower.contains("plant") || queryLower.contains("पेड़") || queryLower.contains("tree") -> 
                        "🌱 **PrakritiMitra की पौधे सलाह!**\n\n1. **तुलसी (Holy Basil)**: हर घर के लिए सबसे उत्तम। यह हवा शुद्ध करता है।\n2. **करी पत्ता**: गर्मियों में आसानी से उगता है।\n3. **नीम (Neem)**: बहुत छायादार और प्राकृतिक कीटनाशक।\n\n*छात्र युक्ति*: घर के कोने में छोटे गमले से शुरुआत करें! 🌳"
                    
                    queryLower.contains("पानी") || queryLower.contains("water") || queryLower.contains("कितना") -> 
                        "💧 **जल मार्गदर्शिका (Watering Guide):**\n\n1. हमेशा मिट्टी छूकर देखें! अगर सूखी हो, तभी पानी दें।\n2. गर्मियों में प्रतिदिन सुबह या शाम को पानी दें।\n3. गमले के नीचे जल निकासी (drainage) छेद होना जरूरी है।\n\nखुश बागवानी! 🌸"
                    
                    queryLower.contains("बीमारी") || queryLower.contains("पत्ती") || queryLower.contains("रोग") || queryLower.contains("scan") || queryLower.contains("disease") -> 
                        "🍂 **रोग सूचक और उपचार:**\n\n1. **पीली पत्तियां**: अक्सर अधिक पानी (Over-watering) या नाइट्रोजन की कमी से होता है।\n2. **सफेद फफूंदी (Powdery Mildew)**: बेकिंग सोडा पानी और नीम तेल का स्प्रे छिड़कें।\n3. **कीट हमला**: साबुन के झाग वाले पानी से धोएं।\n\nस्वस्थ रहें, हरा-भरा रहें! 🌿"
                    
                    else -> 
                        "🌱 नमस्ते! मैं हूँ **PrakritiMitra**।\n\nमैं हिंदी, अंग्रेजी और तेलुगु समझता हूँ।\nपर्यावरण अनुकूल पौधे लगाने, पानी की सलाह और पौधों की बीमारी के उपचार के लिए मुझसे कुछ भी पूछें! 🌳"
                }
            }
            "Telugu" -> {
                when {
                    queryLower.contains("మొక్క") || queryLower.contains("plant") || queryLower.contains("చెట్టు") || queryLower.contains("tree") -> 
                        "🌱 **ప్రకృతిమిత్ర మొక్కల సిఫార్సు!**\n\n1. **తులసి మొక్క (Tulsi)**: ప్రతి ఇంట్లో ఉండాల్సిన పవిత్ర మరియు గాలి శుద్ధి చేసే మొక్క.\n2. **కరివేపాకు (Curry Leaf)**: ఎండ కాలంలో ఎంతో బాగా పెరుగుతుంది.\n3. **వేప చెట్టు (Neem)**: చల్లని నీడ మరియు సహజ రక్షణ ఇస్తుంది.\n\n*విద్యార్థి టిప్*: చిన్న కుండీతో మీ తోటపని ప్రయాణాన్ని ప్రారంభించండి! 🌳"
                    
                    queryLower.contains("నీరు") || queryLower.contains("నీటి") || queryLower.contains("water") || queryLower.contains("ఎంత") -> 
                        "💧 **నీటి యాజమాన్య పద్ధతులు (Watering Guide):**\n\n1. మట్టిని తాకి చూడండి! ఎండిపోయినప్పుడే నీరు పోయండి.\n2. ఎండాకాలంలో రోజూ ఉదయం లేదా సాయంత్రం నీరు పోయాలి.\n3. కుండీ కింద నీరు పోయే రంధ్రం ఉండేలా చూసుకోండి.\n\nహ్యాపీ గార్డెనింగ్! 🌸"
                    
                    queryLower.contains("వ్యాధి") || queryLower.contains("ఆకు") || queryLower.contains("scan") || queryLower.contains("disease") -> 
                        "🍂 **మొక్కల వ్యాధులు - నివారణ:**\n\n1. **ఆకులు పసుపు రంగులోకి మారడం**: ఎక్కువ నీరు పోయడం లేదా నత్రజని లోపం వల్ల జరుగుతుంది.\n2. **బూజు తెగులు (Powdery Mildew)**: వేప నూనె లేదా బేకింగ్ సోడా నీటిని స్ప్రే చేయండి.\n3. **పురుగులు పట్టడం**: సబ్బు నీటితో ఆకులను శుభ్రం చేయండి.\n\nమొక్కలను కాపాడదాం, పచ్చదనం పెంచుదాం! 🌿"
                    
                    else -> 
                        "🌱 నమస్తే! నేను **ప్రకృతిమిత్ర** ను.\n\nనేను తెలుగు, హిందీ మరియు ఇంగ్లీష్ భాషలను మాట్లాడగలను.\nమొక్కల పెంపకం, ఆకు తెగుళ్ళు మరియు పర్యావరణ పరిరక్షణ గురించి నన్ను అడగండి! 🌳"
                }
            }
            else -> { // English by default
                when {
                    queryLower.contains("plant") || queryLower.contains("recommend") || queryLower.contains("suitable") || queryLower.contains("tree") || queryLower.contains("summer") -> 
                        "🌱 **PrakritiMitra Plant Recommendations!**\n\n1. **Aloe Vera / Snake Plant**: Excellent air purifiers, highly resilient for student study spaces.\n2. **Mint / Coriander**: Easy edible herbs to grow right on a sunny classroom window sill!\n3. **Marigold**: Beautiful flowers that act as natural pest repellents.\n\n*Student Tip*: Write a daily plant journal to keep track of growth! 🌳"
                    
                    queryLower.contains("water") || queryLower.contains("how much") || queryLower.contains("requirement") -> 
                        "💧 **PrakritiMitra Water Guidance:**\n\n1. **The Finger Test**: Push your finger 1 inch into the soil. If dry, it's time to water!\n2. **Timing**: Early morning or late evening prevents quick evaporation.\n3. **Avoid Overwatering**: Ensure pots have proper drainage to avoid root rot.\n\nKeep splashing! 🌸"
                    
                    queryLower.contains("disease") || queryLower.contains("leaf") || queryLower.contains("scan") || queryLower.contains("insect") || queryLower.contains("check") -> 
                        "🍂 **PrakritiMitra Disease & Pest Recovery Guide:**\n\n1. **Yellowing Leaves**: Sign of over-watering or nitrogen deficiency.\n2. **Leaf Spots (Fungal)**: Cut off infected parts. Spray homemade baking soda solution or Neem oil.\n3. **White Powdery Film**: Improve air circulation around the plants.\n\nProtect your green friends! 🌿"
                    
                    else -> 
                        "🌱 Hello! I am **PrakritiMitra**, your EcoFriend companion.\n\nI can speak and understand **English, Hindi, and Telugu**.\nAsk me questions about growing suitable plants, watering timers, or leaf diseases! 🌳"
                }
            }
        }
    }
}
