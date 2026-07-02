package com.example.ui

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.util.AppLogger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.VideoLesson
import com.example.data.VideoLessonRepository
import com.example.data.VideoScene
import com.example.data.QuizQuestion
import com.example.data.KeyConcept
import com.example.data.MindMapNode
import com.example.data.Flashcard
import com.example.service.Content
import com.example.service.GenerateContentRequest
import com.example.service.GenerationConfig
import com.example.service.Part
import com.example.service.RetrofitClient
import com.example.service.GeneratedLessonResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    Auth,
    Dashboard,
    Home,
    Generating,
    Player,
    Library,
    Profile,
    LearningJourney
}

sealed interface GenerateState {
    object Idle : GenerateState
    data class Progress(val stage: String) : GenerateState
    object Success : GenerateState
    data class Error(val message: String) : GenerateState
}

data class TutorMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class EduVideoViewModel(
    private val repository: VideoLessonRepository,
    private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("loomiedu_prefs", Context.MODE_PRIVATE)

    // Screen State - Default to Auth for user identity
    private val _currentScreen = MutableStateFlow(AppScreen.Auth)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Form inputs (Advanced)
    val topic = MutableStateFlow("")
    val studentClass = MutableStateFlow("Middle School")
    val durationMinutes = MutableStateFlow(10)
    val subject = MutableStateFlow("Science")
    val board = MutableStateFlow("CBSE")
    val language = MutableStateFlow("English")
    val difficulty = MutableStateFlow("Intermediate")
    val teachingStyle = MutableStateFlow("Visual Storyteller")

    // Smart Content Import inputs
    val importUrl = MutableStateFlow("")
    val isUploading = MutableStateFlow(false)
    val uploadedFileName = MutableStateFlow("")

    // User Profile / Gamification States (Persistent in SharedPreferences)
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Guest Student") ?: "Guest Student")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString("user_email", "guest@loomiedu.com") ?: "guest@loomiedu.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userXP = MutableStateFlow(prefs.getInt("user_xp", 150)) // Start with 150 welcome XP!
    val userXP: StateFlow<Int> = _userXP.asStateFlow()

    private val _userStreak = MutableStateFlow(prefs.getInt("user_streak", 3)) // Default 3 day streak for motivation!
    val userStreak: StateFlow<Int> = _userStreak.asStateFlow()

    private val _completedLessonsCount = MutableStateFlow(prefs.getInt("completed_lessons_count", 2))
    val completedLessonsCount: StateFlow<Int> = _completedLessonsCount.asStateFlow()

    private val _totalStudyTimeMinutes = MutableStateFlow(prefs.getInt("total_study_time", 25))
    val totalStudyTimeMinutes: StateFlow<Int> = _totalStudyTimeMinutes.asStateFlow()

    private val _averageQuizScorePercent = MutableStateFlow(prefs.getInt("average_quiz_score", 85))
    val averageQuizScorePercent: StateFlow<Int> = _averageQuizScorePercent.asStateFlow()

    private val _unlockedBadges = MutableStateFlow(
        prefs.getStringSet("unlocked_badges", setOf("First Login", "Welcome Bonus")) ?: setOf("First Login", "Welcome Bonus")
    )
    val unlockedBadges: StateFlow<Set<String>> = _unlockedBadges.asStateFlow()

    private val _isDarkMode = MutableStateFlow(
        if (prefs.contains("is_dark_mode")) {
            prefs.getBoolean("is_dark_mode", false)
        } else {
            val uiMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs.edit().putBoolean("is_dark_mode", newValue).apply()
    }

    // Active lesson being played
    private val _activeLesson = MutableStateFlow<VideoLesson?>(null)
    val activeLesson: StateFlow<VideoLesson?> = _activeLesson.asStateFlow()

    // Socratic AI Tutor states
    private val _tutorMessages = MutableStateFlow<List<TutorMessage>>(emptyList())
    val tutorMessages: StateFlow<List<TutorMessage>> = _tutorMessages.asStateFlow()

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

    // --- Authentication Actions ---
    fun loginSimulated(name: String, email: String, isGoogle: Boolean) {
        viewModelScope.launch {
            _userName.value = name.ifBlank { "Loomi Scholar" }
            _userEmail.value = email.ifBlank { "scholar@loomiedu.com" }
            _isLoggedIn.value = true
            
            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("user_name", _userName.value)
                putString("user_email", _userEmail.value)
                apply()
            }
            
            addXP(50)
            unlockBadge("Verified Learner")
            if (isGoogle) {
                unlockBadge("Google Scholar")
            }
            setScreen(AppScreen.Dashboard)
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            apply()
        }
        setScreen(AppScreen.Auth)
    }

    // --- Gamification Helpers ---
    fun addXP(amount: Int) {
        val newXp = _userXP.value + amount
        _userXP.value = newXp
        prefs.edit().putInt("user_xp", newXp).apply()
        
        // Dynamic badges based on XP milestones
        if (newXp >= 500) {
            unlockBadge("Halfway Hero")
        }
        if (newXp >= 1000) {
            unlockBadge("Elite Scholar")
        }
    }

    fun incrementStreak() {
        val newStreak = _userStreak.value + 1
        _userStreak.value = newStreak
        prefs.edit().putInt("user_streak", newStreak).apply()
        
        if (newStreak >= 5) {
            unlockBadge("High Five Streak")
        }
    }

    fun unlockBadge(badgeName: String) {
        val current = _unlockedBadges.value.toMutableSet()
        if (current.add(badgeName)) {
            _unlockedBadges.value = current
            prefs.edit().putStringSet("unlocked_badges", current).apply()
        }
    }

    // --- Lesson Operations ---
    fun selectLesson(lesson: VideoLesson) {
        _activeLesson.value = lesson
        initTutorForLesson(lesson)
        setScreen(AppScreen.LearningJourney)
    }

    fun toggleBookmark(lesson: VideoLesson) {
        val newBookmarkState = !lesson.isBookmarked
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBookmark(lesson.id, newBookmarkState)
            // Update active lesson if it is the one bookmarked
            if (_activeLesson.value?.id == lesson.id) {
                _activeLesson.value = _activeLesson.value?.copy(isBookmarked = newBookmarkState)
            }
            if (newBookmarkState) {
                unlockBadge("Curated Knowledge")
                addXP(10)
            }
        }
    }

    fun recordQuizCompletion(lesson: VideoLesson, score: Int) {
        val maxScore = lesson.quiz.size
        val scorePercent = if (maxScore > 0) (score * 100) / maxScore else 0
        
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLessonCompletion(lesson.id, isCompleted = true, quizScore = score)
            
            // Update stats
            val currentLessonsCount = _completedLessonsCount.value + 1
            _completedLessonsCount.value = currentLessonsCount
            prefs.edit().putInt("completed_lessons_count", currentLessonsCount).apply()

            val currentStudyTime = _totalStudyTimeMinutes.value + lesson.durationMinutes
            _totalStudyTimeMinutes.value = currentStudyTime
            prefs.edit().putInt("total_study_time", currentStudyTime).apply()

            // Running average score
            val currentAvg = _averageQuizScorePercent.value
            val newAvg = if (currentAvg > 0) (currentAvg + scorePercent) / 2 else scorePercent
            _averageQuizScorePercent.value = newAvg
            prefs.edit().putInt("average_quiz_score", newAvg).apply()

            // Update active state
            if (_activeLesson.value?.id == lesson.id) {
                _activeLesson.value = _activeLesson.value?.copy(isCompleted = true, quizScore = score)
            }

            // Award XP & Badges
            addXP(100) // Huge quiz bonus!
            if (scorePercent == 100) {
                unlockBadge("Perfect Mastery")
                addXP(50)
            }
            unlockBadge("Trivia Conqueror")
        }
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

    // --- Content Import Simulated Upload ---
    fun triggerSimulatedUpload(fileName: String) {
        viewModelScope.launch {
            isUploading.value = true
            delay(1500) // realistic spinner
            uploadedFileName.value = fileName
            // Auto-fill topic with cleaned file name
            val cleanedTopic = fileName
                .substringBeforeLast(".")
                .replace("_", " ")
                .replace("-", " ")
                .capitalize()
            topic.value = cleanedTopic
            isUploading.value = false
            unlockBadge("Cloud Importer")
        }
    }

    // --- Socratic AI Tutor Chat Engine ---
    fun initTutorForLesson(lesson: VideoLesson) {
        _tutorMessages.value = listOf(
            TutorMessage(
                text = "Hello! I am your Socratic AI Tutor. Ask me any questions about our lesson on '${lesson.topic}'. I can help clarify complex mechanisms, simplify explanations, or test your retention!",
                isUser = false
            )
        )
    }

    fun askTutor(question: String) {
        val active = _activeLesson.value ?: return
        if (question.isBlank()) return
        
        // Append user prompt
        val currentLogs = _tutorMessages.value.toMutableList()
        currentLogs.add(TutorMessage(text = question, isUser = true))
        _tutorMessages.value = currentLogs
        
        viewModelScope.launch {
            // Add typing indicator placeholder
            val typingMessage = TutorMessage(text = "...", isUser = false)
            _tutorMessages.value = _tutorMessages.value + typingMessage
            
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
                    throw IllegalStateException("API Key is missing. Check AI Studio Secrets Panel.")
                }
                
                val systemPrompt = """
                    You are Loomi, an expert academic Socratic AI Tutor teaching a student about "${active.topic}".
                    The student class level is "${active.studentClass}".
                    The teaching style chosen is "${active.teachingStyle ?: "Visual Storyteller"}".
                    Your goal is to answer the student's question based strictly on the current lesson topic.
                    Be engaging, clear, Socratic, and concise. Format with bullet points if helpful. 
                    If the question is completely unrelated to "${active.topic}", politely steer them back.
                """.trimIndent()
                
                val prompt = "Student asks: $question"
                
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.8f),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )
                
                val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "I'm having trouble analyzing that question. Let's try rephrasing!"
                
                // Replace typing indicator with real response
                val finishedLogs = _tutorMessages.value.toMutableList()
                if (finishedLogs.isNotEmpty() && finishedLogs.last().text == "...") {
                    finishedLogs.removeAt(finishedLogs.lastIndex)
                }
                finishedLogs.add(TutorMessage(text = answer, isUser = false))
                _tutorMessages.value = finishedLogs
                
                addXP(15)
                unlockBadge("Tutor Prodigy")
                
            } catch (e: Exception) {
                val finishedLogs = _tutorMessages.value.toMutableList()
                if (finishedLogs.isNotEmpty() && finishedLogs.last().text == "...") {
                    finishedLogs.removeAt(finishedLogs.lastIndex)
                }
                finishedLogs.add(TutorMessage(text = "AI Connection Error: ${e.message ?: "Please verify your network."}", isUser = false))
                _tutorMessages.value = finishedLogs
            }
        }
    }

    // --- AI Lesson Generator Engine ---
    fun generateVideoLesson() {
        val currentTopic = topic.value.trim()
        val currentClass = studentClass.value
        val currentDuration = durationMinutes.value
        val currentSubject = subject.value
        val currentBoard = board.value
        val currentLanguage = language.value
        val currentDifficulty = difficulty.value
        val currentTeachingStyle = teachingStyle.value
        val activeUrl = importUrl.value.trim()
        val activeFile = uploadedFileName.value.trim()

        if (currentTopic.isEmpty()) {
            _generateState.value = GenerateState.Error("Please enter a learning topic or import some content!")
            return
        }

        viewModelScope.launch {
            _currentScreen.value = AppScreen.Generating
            _generateState.value = GenerateState.Progress("Structuring lesson curriculum...")

            delay(1200) // visual stage sequence
            _generateState.value = GenerateState.Progress("Designing visual animated slides...")

            delay(1200)
            _generateState.value = GenerateState.Progress("Generating voiceover narrative and quiz...")

            val systemInstruction = "You are an expert curriculum developer and creative school teacher who produces engaging, bite-sized visual video lessons."
            
            val prompt = """
                Create a highly comprehensive, custom educational lesson on the topic "$currentTopic" under the subject "$currentSubject".
                This lesson is designed for "$currentClass" level students under the "$currentBoard" board, in "$currentLanguage" language.
                The difficulty of the content is "$currentDifficulty", and the teaching style is "$currentTeachingStyle".
                The virtual duration of the video lesson is $currentDuration minutes.
                
                ${if (activeUrl.isNotEmpty()) "CRITICAL: The lesson must be summarized/structured based on the content of the following source link: $activeUrl" else ""}
                ${if (activeFile.isNotEmpty()) "CRITICAL: The lesson must be summarized/structured based on the uploaded lecture document: $activeFile" else ""}

                Generate exactly 4 to 6 scenes that summarize this material in a highly engaging, visual slide-show style.
                
                For each scene, provide:
                1. A title (short and captivating, e.g. "What is Photosynthesis?").
                2. A visualPrompt describing what the illustration on the slide should look like (e.g. "An animation of plant leaves absorbing sunlight and water with glowing chemical formulas").
                3. A set of 2 to 4 clear, summarized bullet points (use standard bullet character '• ') to display as 'textOnScreen'.
                4. An engaging, friendly narrator script (2-4 sentences) as 'voiceoverText' that explains this scene clearly. It must sound like a real teacher speaking.
                5. A 'durationSeconds' of between 12 to 18 seconds for the playback of this slide.

                Also generate exactly 5 quiz questions. Support multiple types: at least 2 Multiple Choice Questions (options is a list of 4 choices, correctAnswerIndex is 0 to 3), at least 1 True/False question (options is exactly ['True', 'False'], correctAnswerIndex is 0 or 1), and at least 1 Fill in the Blanks question (options is a list of 4 choices where one is the correct answer to fill in, or just options with answers, correctAnswerIndex is the index of the correct answer, and the question should contain a blank designated by '_______'). Each question must have a 'type' property set to 'MULTIPLE_CHOICE', 'TRUE_FALSE', or 'FILL_IN_BLANKS' respectively, and a brief, encouraging 'explanation'.

                In addition, you MUST generate the following comprehensive lesson materials to complete the school curriculum:
                1. "lessonPlan": A complete structural lesson plan walkthrough (syllabus mapping, learning objectives, timing checklist). Format this as a clean markdown/text string with clear sections.
                2. "detailedNotes": Detailed conceptual study notes detailing all key terms, definitions, formulas, and core principles. Format this as a rich markdown/text string with clear sub-headers.
                3. "chapterSummary": A bulleted, fast-reading chapter summary of all scenes. Format this as a clean markdown/text string.
                4. "dpp": A list of exactly 3 Daily Practice Problems (homework/self-test questions) for the student to solve on paper. Format each string as "Q[X]: Question Text... \nSolution: Solution details...".
                5. "pyqs": A list of exactly 3 Previous Year Questions (from standard school exams or boards) matching this topic. Format each string as "PYQ [Year]: Question Text... \nAnswer: Step-by-step answer details...".
                6. "shortExplanation": A concise 1-2 sentence high-level summary of the entire topic.
                7. "detailedExplanation": A robust 2-3 paragraph deep-dive explanation of the topic.
                8. "realLifeExamples": A list of exactly 3 interesting real-world scenarios or phenomena illustrating this topic.
                9. "commonMistakes": A list of exactly 3 typical misconceptions or calculation errors students make.
                10. "practicalApplications": A list of exactly 3 direct field/industry applications of this topic.
                11. "keyConcepts": A list of 5 to 7 key concepts. Each concept must be an object with "title", "description" (short, 1 sentence), and "detailedExplanation" (comprehensive paragraph).
                12. "mindMapNodes": A list of mind map nodes representing a clear hierarchy. The first node must be the root (representing the main topic) with parentId as null. The next nodes are sub-concepts (with parentId pointing to parent's id). Each node must have "id" (unique string like "n1", "n2"), "label" (short concept name), and "description" (brief definition).
                13. "flashcards": A list of 6 to 8 flashcards. Each card must have "front" (question/concept) and "back" (answer/definition).

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
                      "explanation": "Brief explanation...",
                      "type": "MULTIPLE_CHOICE"
                    }
                  ],
                  "lessonPlan": "Lesson Plan Markdown/Text...",
                  "detailedNotes": "Detailed Notes Markdown/Text...",
                  "chapterSummary": "Chapter Summary Markdown/Text...",
                  "dpp": [
                    "Q1: Question...\nSolution: ...",
                    "Q2: Question...\nSolution: ...",
                    "Q3: Question...\nSolution: ..."
                  ],
                  "pyqs": [
                    "PYQ 2024: Question...\nAnswer: ...",
                    "PYQ 2023: Question...\nAnswer: ...",
                    "PYQ 2022: Question...\nAnswer: ..."
                  ],
                  "shortExplanation": "Concise summary...",
                  "detailedExplanation": "Deep dive...",
                  "realLifeExamples": ["Ex 1", "Ex 2", "Ex 3"],
                  "commonMistakes": ["Mistake 1", "Mistake 2", "Mistake 3"],
                  "practicalApplications": ["App 1", "App 2", "App 3"],
                  "keyConcepts": [
                    {
                      "title": "Concept Title",
                      "description": "Short explanation",
                      "detailedExplanation": "Detailed explanation..."
                    }
                  ],
                  "mindMapNodes": [
                    {
                      "id": "root",
                      "label": "Main Topic",
                      "description": "Definition of main topic",
                      "parentId": null
                    },
                    {
                      "id": "n1",
                      "label": "Subconcept",
                      "description": "Description",
                      "parentId": "root"
                    }
                  ],
                  "flashcards": [
                    {
                      "front": "Question...",
                      "back": "Answer..."
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

                var parsedResponse: GeneratedLessonResponse? = null
                var lastException: Exception? = null

                AppLogger.event("lesson_generation_started", mapOf("topic" to currentTopic, "subject" to currentSubject, "difficulty" to currentDifficulty))

                // Try Gemini 3.1 Pro preview first as it is the standard complex task model
                try {
                    AppLogger.d("EduVideoVM", "Attempting generation with gemini-3.1-pro-preview...")
                    val response = RetrofitClient.service.generateContent("gemini-3.1-pro-preview", apiKey, request)
                    val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!rawJson.isNullOrBlank()) {
                        AppLogger.d("EduVideoVM", "Received JSON from gemini-3.1-pro-preview: $rawJson")
                        val cleanedJson = cleanJsonString(rawJson)
                        parsedResponse = RetrofitClient.lessonAdapter.lenient().fromJson(cleanedJson)
                    }
                } catch (e: Exception) {
                    AppLogger.w("EduVideoVM", "gemini-3.1-pro-preview generation or parsing failed, will retry with gemini-3.5-flash...", e)
                    lastException = e
                }

                // Fallback to Gemini 3.5 Flash if Pro fails or returns unparseable content
                if (parsedResponse == null) {
                    try {
                        AppLogger.d("EduVideoVM", "Attempting fallback generation with gemini-3.5-flash...")
                        val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                        val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!rawJson.isNullOrBlank()) {
                            AppLogger.d("EduVideoVM", "Received JSON from gemini-3.5-flash: $rawJson")
                            val cleanedJson = cleanJsonString(rawJson)
                            parsedResponse = RetrofitClient.lessonAdapter.lenient().fromJson(cleanedJson)
                        } else {
                            throw Exception("No content received from Gemini (Flash fallback).")
                        }
                    } catch (e: Exception) {
                        AppLogger.e("EduVideoVM", "gemini-3.5-flash fallback also failed.", e)
                        throw Exception("Generation failed on all models. Pro error: ${lastException?.message}. Flash error: ${e.message}")
                    }
                }

                if (parsedResponse == null) {
                    throw Exception("Failed to parse the generated lesson content JSON on all models.")
                }

                val domainScenes = parsedResponse.scenes?.map { apiScene ->
                    VideoScene(
                        title = apiScene.title ?: "Slide",
                        visualPrompt = apiScene.visualPrompt ?: "An educational presentation slide.",
                        textOnScreen = apiScene.textOnScreen ?: "",
                        voiceoverText = apiScene.voiceoverText ?: "Let's learn about this topic.",
                        durationSeconds = apiScene.durationSeconds ?: 15
                    )
                } ?: emptyList()

                val domainQuiz = parsedResponse.quiz?.map { apiQuiz ->
                    QuizQuestion(
                        question = apiQuiz.question ?: "Question",
                        options = apiQuiz.options ?: listOf("Choice A", "Choice B", "Choice C", "Choice D"),
                        correctAnswerIndex = apiQuiz.correctAnswerIndex ?: 0,
                        explanation = apiQuiz.explanation ?: "",
                        type = apiQuiz.type ?: "MULTIPLE_CHOICE"
                    )
                } ?: emptyList()

                val domainKeyConcepts = parsedResponse.keyConcepts?.map { apiConcept ->
                    KeyConcept(
                        title = apiConcept.title ?: "Key Concept",
                        description = apiConcept.description ?: "A brief summary of this concept.",
                        detailedExplanation = apiConcept.detailedExplanation ?: "An in-depth explanation of this concept."
                    )
                } ?: emptyList()

                val domainMindMapNodes = parsedResponse.mindMapNodes?.map { apiNode ->
                    MindMapNode(
                        id = apiNode.id ?: "node_${System.currentTimeMillis()}_${(0..1000).random()}",
                        label = apiNode.label ?: "Node",
                        description = apiNode.description ?: "",
                        parentId = apiNode.parentId
                    )
                } ?: emptyList()

                val domainFlashcards = parsedResponse.flashcards?.mapIndexed { index, apiCard ->
                    Flashcard(
                        id = index,
                        front = apiCard.front ?: "Question?",
                        back = apiCard.back ?: "Answer details."
                    )
                } ?: emptyList()

                if (domainScenes.isEmpty()) {
                    throw Exception("No video scenes were generated. Please try again.")
                }

                // Create the Lesson Entity with all our amazing custom fields!
                val newLesson = VideoLesson(
                    topic = currentTopic,
                    studentClass = currentClass,
                    durationMinutes = currentDuration,
                    scenes = domainScenes,
                    quiz = domainQuiz,
                    subject = currentSubject,
                    board = currentBoard,
                    language = currentLanguage,
                    difficulty = currentDifficulty,
                    teachingStyle = currentTeachingStyle,
                    lessonPlan = parsedResponse.lessonPlan ?: "Syllabus mapping and classroom agenda overview.",
                    detailedNotes = parsedResponse.detailedNotes ?: "Detailed lecture study notes explaining the topic.",
                    chapterSummary = parsedResponse.chapterSummary ?: "Quick high-yield chapter overview.",
                    dpp = parsedResponse.dpp ?: listOf("Q1: Complete lesson self-check question.\nSolution: Standard review of material."),
                    pyqs = parsedResponse.pyqs ?: listOf("PYQ 2024: Review question from standard boards.\nAnswer: Formulated concept verification."),
                    shortExplanation = parsedResponse.shortExplanation ?: "Overview of $currentTopic.",
                    detailedExplanation = parsedResponse.detailedExplanation ?: "Deep-dive study explanation.",
                    realLifeExamples = parsedResponse.realLifeExamples ?: emptyList(),
                    commonMistakes = parsedResponse.commonMistakes ?: emptyList(),
                    practicalApplications = parsedResponse.practicalApplications ?: emptyList(),
                    keyConcepts = domainKeyConcepts,
                    mindMapNodes = domainMindMapNodes,
                    flashcards = domainFlashcards
                )

                // Save in Database
                val insertedId = repository.insertLesson(newLesson)
                val savedLesson = newLesson.copy(id = insertedId)

                _activeLesson.value = savedLesson
                _generateState.value = GenerateState.Success
                initTutorForLesson(savedLesson)
                
                AppLogger.event("lesson_generation_success", mapOf("lesson_id" to insertedId, "topic" to currentTopic))

                // Clear inputs
                topic.value = ""
                importUrl.value = ""
                uploadedFileName.value = ""

                addXP(50) // Generate reward!
                unlockBadge("Curriculum Explorer")
                if (currentDuration == 60) {
                    unlockBadge("Deep Diver")
                }

                _currentScreen.value = AppScreen.LearningJourney

            } catch (e: Exception) {
                AppLogger.e("EduVideoVM", "Generation failed", e)
                AppLogger.event("lesson_generation_failed", mapOf("topic" to currentTopic, "error" to (e.message ?: "Unknown")))
                _generateState.value = GenerateState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    private fun cleanJsonString(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```json").removePrefix("```JSON").removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        cleaned = cleaned.trim()

        val firstBrace = cleaned.indexOf('{')
        val lastBrace = cleaned.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1)
        }
        
        // Remove trailing commas before closing braces/brackets to avoid parsing crashes
        cleaned = cleaned.replace(Regex(",\\s*([}\\]])"), "$1")
        
        return cleaned
    }

    // --- Interactive Playback & Study Suit Helpers ---
    val ttsSpeechRate = MutableStateFlow(prefs.getFloat("tts_speech_rate", 1.0f))
    val ttsPitch = MutableStateFlow(prefs.getFloat("tts_pitch", 1.0f))

    fun updateTtsSpeechRate(rate: Float) {
        ttsSpeechRate.value = rate
        prefs.edit().putFloat("tts_speech_rate", rate).apply()
    }

    fun updateTtsPitch(pitch: Float) {
        ttsPitch.value = pitch
        prefs.edit().putFloat("tts_pitch", pitch).apply()
    }

    fun getPersonalNotes(lessonId: Long): String {
        return prefs.getString("personal_notes_$lessonId", "") ?: ""
    }

    fun savePersonalNotes(lessonId: Long, notes: String) {
        prefs.edit().putString("personal_notes_$lessonId", notes).apply()
    }

    fun recordFlashcardSpacedRepetition(lessonId: Long, cardIndex: Int, rating: String) {
        val xpReward = when (rating) {
            "Easy" -> 15
            "Good" -> 10
            else -> 5
        }
        addXP(xpReward)
        prefs.edit().putString("flashcard_rating_${lessonId}_$cardIndex", rating).apply()
    }

    fun getFlashcardRating(lessonId: Long, cardIndex: Int): String {
        return prefs.getString("flashcard_rating_${lessonId}_$cardIndex", "") ?: ""
    }

    // --- Onboarding Dashboard Checklist Helpers ---
    fun isChecklistItemCompleted(itemId: String): Boolean {
        return prefs.getBoolean("checklist_completed_$itemId", false)
    }

    fun toggleChecklistItem(itemId: String, xpReward: Int) {
        val current = isChecklistItemCompleted(itemId)
        val next = !current
        prefs.edit().putBoolean("checklist_completed_$itemId", next).apply()
        
        if (next) {
            addXP(xpReward)
            if (itemId == "first_lesson") {
                unlockBadge("Curriculum Explorer")
            } else if (itemId == "perfect_mastery") {
                unlockBadge("Perfect Mastery")
            } else if (itemId == "ai_coach") {
                unlockBadge("Tutor Prodigy")
            }
        } else {
            addXP(-xpReward)
        }
    }

    // --- Learning Journey & Platform Persistence Actions ---
    fun updateLessonProgress(
        lessonId: Long,
        videoCompleted: Boolean? = null,
        summaryRead: Boolean? = null,
        flashcardsStudied: Boolean? = null,
        newQuizScore: Int? = null,
        timeSpentIncrementSeconds: Int = 0,
        flashcards: List<Flashcard>? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLesson = repository.getLessonById(lessonId) ?: return@launch
            
            val updatedVideo = videoCompleted ?: currentLesson.isVideoCompleted
            val updatedSummary = summaryRead ?: currentLesson.isSummaryRead
            val updatedFlashcardsStudied = flashcardsStudied ?: currentLesson.isFlashcardsStudied
            val updatedScore = newQuizScore ?: currentLesson.quizScore
            val updatedTime = currentLesson.totalTimeSpentSeconds + timeSpentIncrementSeconds
            val updatedFlashcards = flashcards ?: currentLesson.flashcards
            
            // Calculate completion percentage
            // 4 milestones: Video watched (25%), Summary read (25%), Flashcards studied (25%), Quiz completed (25%)
            var percentage = 0
            if (updatedVideo) percentage += 25
            if (updatedSummary) percentage += 25
            if (updatedFlashcardsStudied) percentage += 25
            if (updatedScore != null) percentage += 25
            
            val isCompleted = percentage == 100
            
            val updatedLesson = currentLesson.copy(
                isVideoCompleted = updatedVideo,
                isSummaryRead = updatedSummary,
                isFlashcardsStudied = updatedFlashcardsStudied,
                quizScore = updatedScore,
                totalTimeSpentSeconds = updatedTime,
                completionPercentage = percentage,
                isCompleted = isCompleted,
                flashcards = updatedFlashcards
            )
            
            repository.updateLesson(updatedLesson)
            
            // Update UI State on Main thread
            launch(Dispatchers.Main) {
                if (_activeLesson.value?.id == lessonId) {
                    _activeLesson.value = updatedLesson
                }
            }
        }
    }

    class Factory(
        private val repository: VideoLessonRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EduVideoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EduVideoViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
