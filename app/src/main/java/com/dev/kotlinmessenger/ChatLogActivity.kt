package com.dev.kotlinmessenger

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_row_item_from.view.*
import kotlinx.android.synthetic.main.chat_row_item_to.view.*

class ChatLogActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var selectedUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        selectedUser = intent.getParcelableExtra(ComposeMessageActivity.USER_KEY)
        supportActionBar?.title = selectedUser?.userName

        recyclerview_chat_log.adapter = adapter

        listenForMessages()


    }

    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if(chatMessage != null) {
                    Log.d(className, chatMessage.messageText)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatItemFrom(chatMessage.messageText))
                    } else{
                        adapter.add(ChatItemTo(chatMessage.messageText))

                    }

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


    fun sendMessageClicked(view: View) {
        performSendMessage()
    }


    private fun performSendMessage() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()

        val messageId = ref.key
        val messageText = edittext_input_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = selectedUser?.uid

        val chatMessage = ChatMessage(messageId, messageText, fromId, toId)
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(className, "saved our chat message, with id: $messageId")
            }

    }


}



// Multiple Adapters for multiple recycler item layouts
class ChatItemFrom(private val messageText: String): Item(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_chat_row_from.text = messageText
    }

    override fun getLayout() = R.layout.chat_row_item_from
}


class ChatItemTo(private val messageText: String): Item(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_chat_row_to.text = messageText
    }

    override fun getLayout() = R.layout.chat_row_item_to
}