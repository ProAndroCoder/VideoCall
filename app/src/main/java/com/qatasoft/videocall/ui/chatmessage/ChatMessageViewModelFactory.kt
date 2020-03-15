package com.qatasoft.videocall.ui.chatmessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qatasoft.videocall.data.repositories.ChatMessageRepository

@Suppress("UNCHECKED_CAST")
class ChatMessageViewModelFactory(private val repository: ChatMessageRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatMessageViewModel(repository) as T
    }

}