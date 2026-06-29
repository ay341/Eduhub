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
    val explanation: String
)

@Entity(tableName = "video_lessons")
data class VideoLesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val studentClass: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val scenes: List<VideoScene>,
    val quiz: List<QuizQuestion>
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
}
