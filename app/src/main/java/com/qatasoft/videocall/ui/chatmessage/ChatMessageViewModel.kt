package com.qatasoft.videocall.ui.chatmessage

import androidx.lifecycle.ViewModel
import com.qatasoft.videocall.data.repositories.ChatMessageRepository
import com.qatasoft.videocall.data.db.entities.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatMessageViewModel(private val repository: ChatMessageRepository) : ViewModel() {
    fun upsert(item: ChatMessage) = CoroutineScope(Dispatchers.Main).launch {
        repository.upsert(item)
    }

    fun delete(item: ChatMessage) = CoroutineScope(Dispatchers.Main).launch {
        repository.delete(item)
    }

    fun update(item: ChatMessage) = CoroutineScope(Dispatchers.Main).launch {
        repository.update(item)
    }

    fun deleteAll(id: String) = CoroutineScope(Dispatchers.Main).launch {
        repository.deleteAll(id)
    }

    fun getAllChatMessageItems(id: String) = repository.getAllChatMessageItems(id)
}