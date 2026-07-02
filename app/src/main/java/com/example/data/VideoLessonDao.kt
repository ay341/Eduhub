package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoLessonDao {
    @Query("SELECT * FROM video_lessons ORDER BY timestamp DESC")
    fun getAllLessons(): Flow<List<VideoLesson>>

    @Query("SELECT * FROM video_lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: Long): VideoLesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: VideoLesson): Long

    @androidx.room.Update
    suspend fun updateLesson(lesson: VideoLesson)

    @Query("UPDATE video_lessons SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Long, isBookmarked: Boolean)

    @Query("UPDATE video_lessons SET isCompleted = :isCompleted, quizScore = :quizScore WHERE id = :id")
    suspend fun updateLessonCompletion(id: Long, isCompleted: Boolean, quizScore: Int)

    @Query("DELETE FROM video_lessons WHERE id = :id")
    suspend fun deleteLessonById(id: Long)

    @Query("DELETE FROM video_lessons")
    suspend fun deleteAllLessons()
}
