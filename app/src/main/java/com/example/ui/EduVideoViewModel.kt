package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.VideoLesson
import com.example.data.VideoLessonRepository
import com.example.service.Content
import com.example.service.GenerateContentRequest
import com.example.service.GenerationConfig
import com.example.service.Part
import com.example.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    Home,
    Generating,
    Player,
    Library
}

sealed interface GenerateState {
    object Idle : GenerateState
    data class Progress(val stage: String) : GenerateState
    object Success : GenerateState
    data class Error(val message: String) : GenerateState
}

class EduVideoViewModel(private val repository: VideoLessonRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Form inputs
    val studentClass = MutableStateFlow("Grade 5")
    val topic = MutableStateFlow("")
    val durationMinutes = MutableStateFlow(10) // default 10 minutes (represented in video timeline)

    // Active lesson being played
    private val _activeLesson = MutableStateFlow<VideoLesson?>(null)
    val activeLesson: StateFlow<VideoLesson?> = _activeLesson.asStateFlow()

    // Gemini call state
    private val _generateState = MutableStateFlow<GenerateState>(GenerateState.Idle)
    val generateState: StateFlow<GenerateState> = _generateState.asStateFlow()

    // History of generated lessons
    val lessonHistory: StateFlow<List<VideoLesson>> = repository.allLessons
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.Home) {
            _generateState.value = GenerateState.Idle
        }
    }

    fun selectLesson(lesson: VideoLesson) {
        _activeLesson.value = lesson
        setScreen(AppScreen.Player)
    }

    fun deleteLesson(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLesson(id)
            if (_activeLesson.value?.id == id) {
                _activeLesson.value = null
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
            _activeLesson.value = null
        }
    }

    fun generateVideoLesson() {
        val currentTopic = topic.value.trim()
        val currentClass = studentClass.value
        val currentDuration = durationMinutes.value

        if (currentTopic.isEmpty()) {
            _generateState.value = GenerateState.Error("Please enter a learning topic!")
            return
        }

        viewModelScope.launch {
            _currentScreen.value = AppScreen.Generating
            _generateState.value = GenerateState.Progress("Structuring lesson curriculum...")

            delay(1200) // smooth transition for visual stages
            _generateState.value = GenerateState.Progress("Designing visual animated slides...")

            delay(1200)
            _generateState.value = GenerateState.Progress("Generating voiceover narrative and quiz...")

            val systemInstruction = "You are an expert curriculum developer and creative school teacher who produces engaging, bite-sized visual video lessons."
            
            val prompt = """
                Create an educational lesson on the topic "$currentTopic" designed specifically for "$currentClass" level students.
                The virtual duration of the video lesson is $currentDuration minutes.
                Generate exactly 4 to 6 scenes that summarize this material in a highly engaging, visual slide-show style.
                
                For each scene, provide:
                1. A title (short and captivating, e.g. "What is Photosynthesis?").
                2. A visualPrompt describing what the illustration on the slide should look like (e.g. "An animation of plant leaves absorbing sunlight and water with glowing chemical formulas").
                3. A set of 2 to 4 clear, summarized bullet points (use standard bullet character '• ') to display as 'textOnScreen'.
                4. An engaging, friendly narrator script (2-4 sentences) as 'voiceoverText' that explains this scene clearly. It must sound like a real teacher speaking.
                5. A 'durationSeconds' of between 12 to 18 seconds for the playback of this slide.

                Also generate exactly 3 interactive multiple-choice questions for a quiz to test the student's knowledge after watching. Each question must have exactly 4 choices, a correctAnswerIndex (0 to 3), and a brief, encouraging 'explanation'.

                Strictly return a JSON object containing the exact structure below, with no wrapping or markdown formatting outside of the JSON block:
                {
                  "scenes": [
                    {
                      "title": "Scene Title",
                      "visualPrompt": "Visual details...",
                      "textOnScreen": "• Point one\n• Point two",
                      "voiceoverText": "Narrator transcript text...",
                      "durationSeconds": 15
                    }
                  ],
                  "quiz": [
                    {
                      "question": "What is ...?",
                      "options": ["Option A", "Option B", "Option C", "Option D"],
                      "correctAnswerIndex": 1,
                      "explanation": "Brief explanation of why Option B is correct."
                    }
                  ]
                }
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 1.0f
                ),
                systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
                    throw IllegalStateException("API Key is missing. Please configure it in the AI Studio Secrets panel.")
                }

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("No content received from Gemini.")

                Log.d("EduVideoVM", "Received JSON: $rawJson")

                val parsedResponse = RetrofitClient.lessonAdapter.fromJson(rawJson)
                    ?: throw Exception("Failed to parse the lesson content JSON.")

                if (parsedResponse.scenes.isEmpty()) {
                    throw Exception("No video scenes were generated. Please try again.")
                }

                // Create the Lesson Entity
                val newLesson = VideoLesson(
                    topic = currentTopic,
                    studentClass = currentClass,
                    durationMinutes = currentDuration,
                    scenes = parsedResponse.scenes,
                    quiz = parsedResponse.quiz
                )

                // Save in Database
                val insertedId = repository.insertLesson(newLesson)
                val savedLesson = newLesson.copy(id = insertedId)

                _activeLesson.value = savedLesson
                _generateState.value = GenerateState.Success
                _currentScreen.value = AppScreen.Player
                
                // Clear the topic input after successful generation
                topic.value = ""

            } catch (e: Exception) {
                Log.e("EduVideoVM", "Generation failed", e)
                _generateState.value = GenerateState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    class Factory(private val repository: VideoLessonRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EduVideoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EduVideoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
