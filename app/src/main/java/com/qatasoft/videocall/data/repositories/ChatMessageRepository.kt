package com.qatasoft.videocall.data.repositories

import com.qatasoft.videocall.data.db.ChatMessageDatabase
import com.qatasoft.videocall.data.db.entities.ChatMessage

class ChatMessageRepository(private val db: ChatMessageDatabase) {
    suspend fun upsert(item: ChatMessage) = db.getChatMessageDao().upsert(item)

    suspend fun delete(item: ChatMessage) = db.getChatMessageDao().delete(item)

    suspend fun deleteAll(id: String) = db.getChatMessageDao().deleteAll(id)

    fun getAllChatMessageItems(id: String) = db.getChatMessageDao().getAllChatMessage(id)

    suspend fun update(item: ChatMessage) = db.getChatMessageDao().update(item)


}