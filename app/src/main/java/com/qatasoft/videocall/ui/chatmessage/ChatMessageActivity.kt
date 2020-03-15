package com.qatasoft.videocall.ui.chatmessage

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.util.Log
import android.util.Pair
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.qatasoft.videocall.R
import com.qatasoft.videocall.data.db.entities.ChatMessage
import com.qatasoft.videocall.data.db.entities.User
import com.google.firebase.database.*
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.qatasoft.videocall.ui.bottomfragments.messages.MessagesFragment.Companion.USER_KEY
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.ViewActivity
import com.qatasoft.videocall.data.db.entities.Tools
import com.qatasoft.videocall.request.FBaseControl
import com.qatasoft.videocall.videoCallRequests.SendVideoRequest
import com.qatasoft.videocall.ui.chatmessage.ChatFromItem.Companion.isMultiSelectActive
import com.qatasoft.videocall.ui.chatmessage.ChatFromItem.Companion.selectedList
import com.qatasoft.videocall.ui.chatmessage.ChatFromItem.Companion.selectedPositions
import com.qatasoft.videocall.ui.chatmessage.ChatFromItem.Companion.selectedViews
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_chat_log.toolbar
import org.kodein.di.KodeinAware
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class ChatLogActivity : AppCompatActivity(), OnChatItemClickListener, KodeinAware {
    companion object {
        const val logTAG = "ChatLogActivityLogs"
    }

    override val kodein by kodein()
    private val factory: ChatMessageViewModelFactory by instance()

    private var mActionMode: ActionMode? = null

    private lateinit var mFiles: ArrayList<MediaFile>
    private lateinit var toId: String
    private lateinit var user: User
    private lateinit var fromId: String
    private lateinit var viewModel: ChatMessageViewModel
    private lateinit var ref: DatabaseReference
    private lateinit var listener: ChildEventListener

    private var messageList = ArrayList<ChatMessage>()
    private var firebaseControl = FBaseControl()
    private val FILE_REQUEST_CODE = 24
    private val maxSize = 200000000
    val adapter = GroupAdapter<ViewHolder>()
    var mUser = MainActivity.mUser

    private var attachmentUrl: String = ""
    private var attachmentName: String = ""
    private var attachmentType: String = ""
    private var mesOps = MessageOps.REFRESH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        setSupportActionBar(toolbar)

        chat_videocall.setOnClickListener {
            val intent = Intent(this, SendVideoRequest::class.java)
            intent.putExtra(USER_KEY, user)
            startActivity(intent)
        }

        btn_send_chatlog.setOnClickListener {
            performSendMessage()
        }

        img_attachment_chatlog.setOnClickListener {
            performSendAttachment()
        }

        getChatInfo()
    }

    override fun onDestroy() {
        Log.d(logTAG, "onDestroy")

        // This method must be called on the main thread.
        Glide.get(this).clearMemory()

        Thread(Runnable {
            // This method must be called on a background thread.
            Glide.get(this).clearDiskCache()
        }).start()

        ref.removeEventListener(listener)
        super.onDestroy()
    }

    private fun getChatInfo() {
        viewModel = ViewModelProvider(this, factory).get(ChatMessageViewModel::class.java)

        recyclerview_chatlog.adapter = adapter

        //Parcelable Nesne Alma
        user = intent.getParcelableExtra(USER_KEY)!!

        fromId = mUser.uid
        toId = user.uid

        Glide.with(this).load(user.profileImageUrl).into(chat_userImage)

        chat_username.text = user.username

        viewModel.getAllChatMessageItems(toId).observe(this, androidx.lifecycle.Observer {
            when (mesOps) {
                MessageOps.ADD -> {
                    val addedItem = it[it.lastIndex]
                    messageList.add(addedItem)
                    if (mUser.uid == addedItem.fromId) {
                        if (addedItem.attachmentName.isEmpty()) {
                            firebaseControl.performSendMessage(addedItem)
                        }
                        mesOps = MessageOps.UPDATE
                        adapter.add(ChatFromItem(addedItem, applicationContext, viewModel, this))
                    } else {
                        adapter.add(ChatToItem(addedItem, applicationContext, viewModel, this))
                    }
                    Log.d(logTAG, "ADD Room Info : " + addedItem.text)

                    adapter.notifyItemInserted(it.lastIndex)
                    nested.fullScroll(View.FOCUS_DOWN)
                    //recyclerview_chatlog.smoothScrollToPosition(adapter.itemCount - 1)
                }

                MessageOps.REMOVE -> {
                    //Do Nothing
                }

                MessageOps.REFRESH -> {
                    adapter.clear()
                    messageList.clear()
                    it.forEach { item ->
                        messageList.add(item)
                        if (mUser.uid == item.fromId) {
                            adapter.add(ChatFromItem(item, applicationContext, viewModel, this))
                        } else {
                            adapter.add(ChatToItem(item, applicationContext, viewModel, this))
                        }

                        Log.d(logTAG, "REFRESH Room Info : " + item.text + " " + item.id)
                    }

                    adapter.notifyDataSetChanged()
                    nested.post {
                        nested.fullScroll(View.FOCUS_DOWN)
                    }
                    /*Handler().postDelayed({
                        nested.fullScroll(View.FOCUS_DOWN)
                    }, 500)*/

                    //recyclerview_chatlog.smoothScrollToPosition(adapter.itemCount - 1)
                }

                MessageOps.UPDATE -> {
                    //We dont have any process for this code part.
                    it.forEach { message ->
                        Log.d(logTAG, "UPDATE Room Info : " + message.text + " " + message.id)

                    }
                }
            }
        })

        fetchMessages()
    }

    private fun fetchMessages() {
        ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        listener = ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                var isSame = false

                if (chatMessage != null) {
                    messageList.forEach { message ->
                        if (message.id == chatMessage.id) {
                            isSame = true
                        }
                    }
                    if (isSame) {
                        Log.d(logTAG, "+ OnChildAdded : " + chatMessage.text + " " + chatMessage.id)
                    } else {
                        mesOps = MessageOps.ADD
                        viewModel.upsert(chatMessage)
                        Log.d(logTAG, "- OnChildAdded : " + chatMessage.text + " " + chatMessage.id)
                    }
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                Log.d(logTAG, "OnChildChanged : " + chatMessage!!.text)
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                Log.d(logTAG, "OnChildRemoved : " + chatMessage!!.text)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun performSendAttachment() {
        val intent = Intent(this, FilePickerActivity::class.java)
        intent.putExtra(FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .enableVideoCapture(true)
                .setShowImages(true)
                .setShowVideos(true)
                .setShowAudios(true)
                .setShowFiles(true)
                .enableImageCapture(true)
                .setMaxSelection(10)
                .setSkipZeroSizeFiles(true)
                .build())
        startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    private fun performSendMessage() {
        val text = et_message_chatlog.text.toString()

        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

        val chatMessage = ChatMessage(text, fromId, toId, mUser.username, user.username, sendingTime)

        if (fromId.isEmpty() || text.isEmpty() || toId.isEmpty()) {
            Log.d(logTAG, "There is an error while sending message")
            return
        }

        //Mesaj kısmını boşaltma
        mesOps = MessageOps.ADD
        viewModel.upsert(chatMessage)
        et_message_chatlog.text.clear()
    }

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.contextual_actionbar, menu)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.remove -> {
                    Toast.makeText(applicationContext, "Del ${selectedList.size}  ${selectedViews.size}", Toast.LENGTH_SHORT).show()

                    var i = 0
                    while (i < selectedList.size) {
                        val chatMessage: ChatMessage = selectedList[i]

                        Log.d(logTAG, "Item Info : ${adapter.itemCount} ${selectedViews.size} ${recyclerview_chatlog.size}")

                        selectedViews[i].setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorAero))

                        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${mUser.uid}/${user.uid}/${chatMessage.refKey}")
                        ref.removeValue().addOnSuccessListener {
                            mesOps = MessageOps.REMOVE
                            viewModel.delete(chatMessage)
                        }

                        i++
                    }

                    selectedPositions.sort()
                    selectedPositions.reverse()

                    selectedPositions.forEach {
                        Log.d(logTAG, "Position : $it")
                    }

                    selectedPositions.forEach {
                        if (it < adapter.itemCount) {
                            adapter.removeGroup(it)
                        }
                    }

                    selectedPositions.clear()
                    selectedList.clear()
                    selectedViews.clear()
                    isMultiSelectActive = false
                    mode.finish() // Action picked, so close the CAB

                    true
                }
                R.id.share -> {
                    Toast.makeText(applicationContext, "Share ${selectedList.size}  ${selectedViews.size}", Toast.LENGTH_SHORT).show()

                    selectedViews.forEach {
                        it.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorAero))
                    }

                    selectedList.clear()
                    selectedViews.clear()
                    mode.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            selectedViews.forEach {
                it.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorAero))
            }

            selectedList.clear()
            selectedViews.clear()

            mActionMode = null
            mode.finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FILE_REQUEST_CODE -> {

                if (data == null) return

                mFiles = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)!!
                var sumSize: Long = 0
                mFiles.forEach {
                    sumSize += it.size
                }

                if (sumSize <= maxSize) {
                    Toast.makeText(this, "Size of Files : $sumSize", Toast.LENGTH_SHORT).show()

                    mFiles.forEach { item ->
                        sumSize += item.size

                        Log.d(logTAG, "OK : " + item.mimeType + "  " + item.size + "  " + item.mediaType + "  " + item.name + " path: " + item.path + "  " + item.uri + "  " + sumSize)

                        attachmentType = getTypeOfFile(item.mimeType)
                        attachmentName = item.name

                        if (fromId.isEmpty() || attachmentName.isEmpty() || attachmentType.isEmpty() || item.uri == null) {
                            Log.d(logTAG, "There is an error while sending attachment")
                            return
                        }

                        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

                        val chatMessage = ChatMessage("", fromId, toId, mUser.username, user.username, sendingTime, "", attachmentName, attachmentType, item.path)

                        mesOps = MessageOps.ADD
                        viewModel.upsert(chatMessage)
                        Log.d(logTAG, "Attachment Child Added")
                    }

                } else {
                    Toast.makeText(this, "Files are bigger than 200 MB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getTypeOfFile(mimeType: String): String {
        return when {
            mimeType.contains(Tools.image) -> {
                Tools.Image
            }
            mimeType.contains(Tools.video) -> {
                Tools.Video
            }
            mimeType.contains(Tools.audio) -> {
                Tools.Audio
            }
            else -> {
                Tools.Document
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.remove_chatlog -> {
                val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${mUser.uid}/${user.uid}")

                ref.removeValue()
                mesOps = MessageOps.REFRESH
                viewModel.deleteAll(mUser.uid)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeEnterExitTransition() {
        val enter = ChangeBounds()
        enter.duration = 5000

        val exit = ChangeBounds()
        exit.interpolator = DecelerateInterpolator()
        exit.duration = 5000

        window.sharedElementEnterTransition = enter
        window.sharedElementReturnTransition = exit
    }

    override fun onItemClick(item: ChatMessage, position: Int, view: View) {
        Log.d(logTAG, "Click Info : ${item.attachmentType} ${item.attachmentName} $position")

        when (item.attachmentType) {
            Tools.Image, Tools.Video -> {
                val sharedIntent = Intent(applicationContext, ViewActivity::class.java)

                val transition = item.attachmentType + "Transition"

                val pairs = Pair<View, String>(view, transition)

                val options = ActivityOptions.makeSceneTransitionAnimation(this@ChatLogActivity, pairs)

                sharedIntent.putExtra(MainActivity.keyViewActivityUri, item.fileUri)
                sharedIntent.putExtra(MainActivity.keyViewActivityType, item.attachmentType)

                startActivity(sharedIntent, options.toBundle())
            }
            Tools.Document -> {

            }
        }
    }

    override fun onContextualState(isActive: Boolean) {
        if (isActive) {
            if (mActionMode == null) {
                mActionMode = startActionMode(actionModeCallback)
            }
        } else {
            if (mActionMode != null) {
                mActionMode!!.finish()
            }
            mActionMode = null
        }
    }
}

enum class ChatType {
    FROM,
    TO
}

enum class MessageOps {
    ADD,
    REMOVE,
    REFRESH,
    UPDATE
}