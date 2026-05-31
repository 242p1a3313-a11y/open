package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<OpenAiMessage>
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    @Json(name = "message") val message: OpenAiMessage?
)

@JsonClass(generateAdapter = true)
data class OpenAiResponse(
    @Json(name = "choices") val choices: List<OpenAiChoice>?
)

interface OpenAiApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}

interface GenericVercelApiService {
    @POST
    suspend fun postJson(
        @Url url: String,
        @Header("Authorization") authHeader: String?,
        @Body body: okhttp3.RequestBody
    ): okhttp3.ResponseBody
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val VERCEL_BASE_URL = "https://open-theta-snowy.vercel.app/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    val vercelGeminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(VERCEL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    val vercelOpenAiService: OpenAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(VERCEL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenAiApiService::class.java)
    }

    val genericVercelService: GenericVercelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(VERCEL_BASE_URL)
            .client(okHttpClient)
            .build()
            .create(GenericVercelApiService::class.java)
    }
}
