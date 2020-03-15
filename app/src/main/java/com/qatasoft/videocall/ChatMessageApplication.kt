package com.qatasoft.videocall

import android.app.Application
import com.qatasoft.videocall.data.db.ChatMessageDatabase
import com.qatasoft.videocall.data.repositories.ChatMessageRepository
import com.qatasoft.videocall.ui.chatmessage.ChatMessageViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class ChatMessageApplication : Application(), KodeinAware {
    override val kodein: Kodein = Kodein.lazy {
        import(androidXModule(this@ChatMessageApplication))
        bind() from singleton { ChatMessageDatabase(instance()) }
        bind() from singleton { ChatMessageRepository(instance()) }
        bind() from provider { ChatMessageViewModelFactory(instance()) }
    }
}