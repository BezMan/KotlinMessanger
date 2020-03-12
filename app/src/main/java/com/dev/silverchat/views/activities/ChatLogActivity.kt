package com.dev.silverchat.views.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dev.silverchat.R
import com.dev.silverchat.model.entities.ChatMessage
import com.dev.silverchat.model.entities.UnreadMessages
import com.dev.silverchat.model.entities.User
import com.dev.silverchat.views.activities.MessagesListActivity.Companion.firebaseDatabase
import com.dev.silverchat.views.activities.MessagesListActivity.Companion.myId
import com.dev.silverchat.views.helpers.DateUtils
import com.dev.silverchat.views.helpers.DateUtils.getFormattedTimeChatLog
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.chat_activity.*
import kotlinx.android.synthetic.main.chat_row_from.view.*
import kotlinx.android.synthetic.main.chat_row_to.view.*
import kotlinx.android.synthetic.main.custom_chat_bar.*

class ChatLogActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var selectedUser: User? = null
    private var toId: String? = null
    private var toName: String? = null
    private var toImageUrl: String? = null
    private var toAbout: String? = null
    private var toTimeJoined: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)

        getUserInfo()

        initViews()

        recyclerview_chat_log.adapter = adapter

        listenForMessages()

        adjustListToKeyboard()

    }

    private fun getUserInfo() {
        selectedUser = intent.getParcelableExtra(ComposeMessageActivity.USER_KEY)
        toId = selectedUser?.uid
        toName = selectedUser?.userName
        toImageUrl = selectedUser?.imageUrl
        toAbout = selectedUser?.aboutMe
        toTimeJoined = selectedUser?.timeJoined
    }


    private fun initViews() {
        //ACTIONBAR views//
        val actionBar = supportActionBar
        actionBar?.setDisplayShowCustomEnabled(true)
        val actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null)
        actionBar?.customView = actionBarView

        custom_profile_name.text = toName
        Picasso.get().load(toImageUrl).placeholder(R.drawable.ic_face_profile).into(custom_profile_image)
        displayLastSeen()
    }

    private fun displayLastSeen() {
        firebaseDatabase.reference.child("users").child(toId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild("online")) {
                        val isOnline= dataSnapshot.child("online").value
                        val lastSeen = dataSnapshot.child("last_seen").value.toString()

                        val formattedLastSeen = DateUtils.getFormattedTimeLatestMessage(lastSeen.toLong())
                        if (isOnline == true) {
                            custom_user_last_seen.text = "online"
                        } else {
                            custom_user_last_seen.text = "Last Seen: $formattedLastSeen"
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }



    override fun onPause() {
        super.onPause()
        resetUnreadMessages()
    }


    private fun resetUnreadMessages() {
        firebaseDatabase.getReference("/unread-messages/$myId/$toId")
            .setValue(UnreadMessages(0))
    }


    /** pushes up recycler view when softkeyboard popups up */
    private fun adjustListToKeyboard() {
        recyclerview_chat_log.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerview_chat_log.postDelayed({
                    recyclerview_chat_log.scrollToPosition(
                        recyclerview_chat_log.adapter!!.itemCount - 1
                    )
                }, 100)
            }
        }
    }


    /** populate list on initial load and also on children added */
    private fun listenForMessages() {
        val ref = firebaseDatabase.getReference("/user-messages/$myId/$toId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if(chatMessage != null) {

                    if(chatMessage.fromId == myId) {
                        adapter.add(ChatItemFrom(chatMessage.messageText, chatMessage.timeStamp))
                    } else{
                        adapter.add(ChatItemTo(chatMessage.messageText, chatMessage.timeStamp))
                    }
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }


    fun sendMessageButtonClicked(view: View) {
        performSendMessage()
    }


    private fun performSendMessage() {

        val messageText = edittext_input_chat_log.text.toString()

        if (messageText.trim().isNotEmpty()) {

            val refMessageListSender = firebaseDatabase.getReference("/user-messages/$myId/$toId").push() //push == add
            val refMessageListReceiver = firebaseDatabase.getReference("/user-messages/$toId/$myId").push() //push == add
            val refLatestMessageSender = firebaseDatabase.getReference("/latest-messages/$myId/$toId") //no push == replace
            val refLatestMessageReceiver = firebaseDatabase.getReference("/latest-messages/$toId/$myId") //no push == replace

            val chatMessage = ChatMessage(
                refMessageListSender.key,
                messageText,
                myId,
                toId
            )

            refMessageListSender.setValue(chatMessage)
            refMessageListReceiver.setValue(chatMessage)
            refLatestMessageSender.setValue(chatMessage)
            refLatestMessageReceiver.setValue(chatMessage)

            edittext_input_chat_log.text.clear()

            incrementUnreadMessagesCount()

        }

    }

    private fun incrementUnreadMessagesCount() {
        val refIncrementUnread = firebaseDatabase.getReference("/unread-messages/$toId/$myId") //no push == replace

        refIncrementUnread.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val unreadMessages = p0.getValue(UnreadMessages::class.java)
                refIncrementUnread.setValue(UnreadMessages(unreadMessages?.count?.plus(1)))
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

}



// Multiple Adapters for multiple recycler item layouts

//adapter 1
class ChatItemFrom(private val messageText: String, private val timestamp: Long): Item(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_chat_row_from.text = messageText
        viewHolder.itemView.time_chat_row_from.text = getFormattedTimeChatLog(timestamp)
    }

    override fun getLayout() =
        R.layout.chat_row_from
}

//adapter 2
class ChatItemTo(private val messageText: String, private val timestamp: Long): Item(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_chat_row_to.text = messageText
        viewHolder.itemView.time_chat_row_to.text = getFormattedTimeChatLog(timestamp)
    }

    override fun getLayout() = R.layout.chat_row_to
}