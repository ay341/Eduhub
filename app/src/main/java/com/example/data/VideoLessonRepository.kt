package com.example.data

import kotlinx.coroutines.flow.Flow

class VideoLessonRepository(private val videoLessonDao: VideoLessonDao) {
    val allLessons: Flow<List<VideoLesson>> = videoLessonDao.getAllLessons()

    suspend fun getLessonById(id: Long): VideoLesson? {
        return videoLessonDao.getLessonById(id)
    }

    suspend fun insertLesson(lesson: VideoLesson): Long {
        return videoLessonDao.insertLesson(lesson)
    }

    suspend fun updateLesson(lesson: VideoLesson) {
        videoLessonDao.updateLesson(lesson)
    }

    suspend fun updateBookmark(id: Long, isBookmarked: Boolean) {
        videoLessonDao.updateBookmark(id, isBookmarked)
    }

    suspend fun updateLessonCompletion(id: Long, isCompleted: Boolean, quizScore: Int) {
        videoLessonDao.updateLessonCompletion(id, isCompleted, quizScore)
    }

    suspend fun deleteLesson(id: Long) {
        videoLessonDao.deleteLessonById(id)
    }

    suspend fun clearHistory() {
        videoLessonDao.deleteAllLessons()
    }
}
