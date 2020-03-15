package com.qatasoft.videocall.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.qatasoft.videocall.data.db.entities.ChatMessage

@Database(
        entities = [ChatMessage::class],
        version = 2
)

abstract class ChatMessageDatabase : RoomDatabase() {
    abstract fun getChatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var instance: ChatMessageDatabase? = null

        private val LOCK = Any()

        operator fun invoke(context: Context) = instance
                ?: synchronized(LOCK) {
                    instance ?: createDatabase(context).also { instance = it }
                }

        private fun createDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        ChatMessageDatabase::class.java, "ChatMessageDB.db").fallbackToDestructiveMigration().build()
    }
}