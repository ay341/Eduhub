package com.example.service

import com.example.data.QuizQuestion
import com.example.data.VideoScene
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// --- Gemini REST API Data Classes ---

data class Part(
    val text: String? = null
)

data class Content(
    val parts: List<Part>
)

data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Candidate(
    val content: Content?
)

data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

// --- Robust API-specific response models to prevent parsing exceptions ---
data class ApiVideoScene(
    val title: String? = null,
    val visualPrompt: String? = null,
    val textOnScreen: String? = null,
    val voiceoverText: String? = null,
    val durationSeconds: Int? = null
)

data class ApiQuizQuestion(
    val question: String? = null,
    val options: List<String>? = null,
    val correctAnswerIndex: Int? = null,
    val explanation: String? = null,
    val type: String? = null
)

data class ApiKeyConcept(
    val title: String? = null,
    val description: String? = null,
    val detailedExplanation: String? = null
)

data class ApiMindMapNode(
    val id: String? = null,
    val label: String? = null,
    val description: String? = null,
    val parentId: String? = null
)

data class ApiFlashcard(
    val front: String? = null,
    val back: String? = null
)

// --- Custom wrapper for the output we expect from Gemini ---
data class GeneratedLessonResponse(
    val scenes: List<ApiVideoScene>? = null,
    val quiz: List<ApiQuizQuestion>? = null,
    val lessonPlan: String? = null,
    val detailedNotes: String? = null,
    val chapterSummary: String? = null,
    val dpp: List<String>? = null,
    val pyqs: List<String>? = null,
    
    // Learning Journey fields
    val shortExplanation: String? = null,
    val detailedExplanation: String? = null,
    val realLifeExamples: List<String>? = null,
    val commonMistakes: List<String>? = null,
    val practicalApplications: List<String>? = null,
    val keyConcepts: List<ApiKeyConcept>? = null,
    val mindMapNodes: List<ApiMindMapNode>? = null,
    val flashcards: List<ApiFlashcard>? = null
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit Client ---

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    val lessonAdapter = moshi.adapter(GeneratedLessonResponse::class.java)
}
