package com.qatasoft.videocall.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.qatasoft.videocall.data.db.entities.ChatMessage

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ChatMessage)

    @Delete
    suspend fun delete(item: ChatMessage)

    @Update
    suspend fun update(item: ChatMessage)

    @Query("DELETE FROM chat_items WHERE item_from_id=:id or item_to_id=:id")
    suspend fun deleteAll(id: String)

    @Query("SELECT * FROM chat_items WHERE item_from_id=:id or item_to_id=:id ORDER BY item_sending_time ASC")
    fun getAllChatMessage(id: String): LiveData<List<ChatMessage>>
}