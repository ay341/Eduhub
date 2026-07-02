package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class VideoScene(
    val title: String,
    val visualPrompt: String,
    val textOnScreen: String,
    val voiceoverText: String,
    val durationSeconds: Int
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val type: String = "MULTIPLE_CHOICE" // "MULTIPLE_CHOICE", "TRUE_FALSE", "FILL_IN_BLANKS"
)

data class KeyConcept(
    val title: String,
    val description: String,
    val detailedExplanation: String
)

data class MindMapNode(
    val id: String,
    val label: String,
    val description: String,
    val parentId: String? = null
)

data class Flashcard(
    val id: Int,
    val front: String,
    val back: String,
    val isLearned: Boolean = false,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "video_lessons")
data class VideoLesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val studentClass: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val scenes: List<VideoScene>,
    val quiz: List<QuizQuestion>,
    val subject: String? = null,
    val board: String? = null,
    val language: String? = null,
    val difficulty: String? = null,
    val teachingStyle: String? = null,
    val lessonPlan: String? = null,
    val detailedNotes: String? = null,
    val chapterSummary: String? = null,
    val dpp: List<String>? = null,
    val pyqs: List<String>? = null,
    val isBookmarked: Boolean = false,
    val isCompleted: Boolean = false,
    val quizScore: Int? = null,
    
    // AI Learning Journey additions
    val shortExplanation: String = "",
    val detailedExplanation: String = "",
    val realLifeExamples: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val practicalApplications: List<String> = emptyList(),
    val keyConcepts: List<KeyConcept> = emptyList(),
    val mindMapNodes: List<MindMapNode> = emptyList(),
    val flashcards: List<Flashcard> = emptyList(),
    
    // Progress Tracking additions
    val isVideoCompleted: Boolean = false,
    val isSummaryRead: Boolean = false,
    val isFlashcardsStudied: Boolean = false,
    val totalTimeSpentSeconds: Int = 0,
    val completionPercentage: Int = 0
)

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromSceneList(scenes: List<VideoScene>?): String? {
        if (scenes == null) return null
        val type = Types.newParameterizedType(List::class.java, VideoScene::class.java)
        val adapter = moshi.adapter<List<VideoScene>>(type)
        return adapter.toJson(scenes)
    }

    @TypeConverter
    fun toSceneList(json: String?): List<VideoScene>? {
        if (json == null) return null
        val type = Types.newParameterizedType(List::class.java, VideoScene::class.java)
        val adapter = moshi.adapter<List<VideoScene>>(type)
        return adapter.fromJson(json)
    }

    @TypeConverter
    fun fromQuizList(quiz: List<QuizQuestion>?): String? {
        if (quiz == null) return null
        val type = Types.newParameterizedType(List::class.java, QuizQuestion::class.java)
        val adapter = moshi.adapter<List<QuizQuestion>>(type)
        return adapter.toJson(quiz)
    }

    @TypeConverter
    fun toQuizList(json: String?): List<QuizQuestion>? {
        if (json == null) return null
        val type = Types.newParameterizedType(List::class.java, QuizQuestion::class.java)
        val adapter = moshi.adapter<List<QuizQuestion>>(type)
        return adapter.fromJson(json)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        if (json == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.fromJson(json)
    }

    @TypeConverter
    fun fromKeyConceptList(value: List<KeyConcept>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, KeyConcept::class.java)
        val adapter = moshi.adapter<List<KeyConcept>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toKeyConceptList(json: String?): List<KeyConcept>? {
        if (json == null) return null
        val type = Types.newParameterizedType(List::class.java, KeyConcept::class.java)
        val adapter = moshi.adapter<List<KeyConcept>>(type)
        return adapter.fromJson(json)
    }

    @TypeConverter
    fun fromMindMapNodeList(value: List<MindMapNode>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, MindMapNode::class.java)
        val adapter = moshi.adapter<List<MindMapNode>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toMindMapNodeList(json: String?): List<MindMapNode>? {
        if (json == null) return null
        val type = Types.newParameterizedType(List::class.java, MindMapNode::class.java)
        val adapter = moshi.adapter<List<MindMapNode>>(type)
        return adapter.fromJson(json)
    }

    @TypeConverter
    fun fromFlashcardList(value: List<Flashcard>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, Flashcard::class.java)
        val adapter = moshi.adapter<List<Flashcard>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toFlashcardList(json: String?): List<Flashcard>? {
        if (json == null) return null
        val type = Types.newParameterizedType(List::class.java, Flashcard::class.java)
        val adapter = moshi.adapter<List<Flashcard>>(type)
        return adapter.fromJson(json)
    }
}
