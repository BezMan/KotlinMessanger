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
import com.squareup.picasso.Picasso
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
    private var fromId: String? = null
    private var toId: String? = null
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        selectedUser = intent.getParcelableExtra(ComposeMessageActivity.USER_KEY)
        fromId = FirebaseAuth.getInstance().uid
        toId = selectedUser?.uid

        supportActionBar?.title = selectedUser?.userName

        recyclerview_chat_log.adapter = adapter

        listenForMessages()

    }


    //also initial list load and also on children added:
    private fun listenForMessages() {
        val ref = firebaseDatabase.getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if(chatMessage != null) {
                    Log.d(className, chatMessage.messageText)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatItemFrom(chatMessage.messageText, MessagesListActivity.currentUser))
                    } else{
                        adapter.add(ChatItemTo(chatMessage.messageText, selectedUser))

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

        val messageText = edittext_input_chat_log.text.toString()

        val refSender = firebaseDatabase.getReference("/user-messages/$fromId/$toId").push()
        val refReceiver = firebaseDatabase.getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(refSender.key, messageText, fromId, toId)

        refSender.setValue(chatMessage)
        refReceiver.setValue(chatMessage)
    }


}



// Multiple Adapters for multiple recycler item layouts

//adapter 1
class ChatItemFrom(private val messageText: String, private val user: User?): Item(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_chat_row_from.text = messageText
        val targetImageView = viewHolder.itemView.circleimageview_chat_row_from
        Picasso.get().load(user?.profileImageUrl).into(targetImageView)
    }

    override fun getLayout() = R.layout.chat_row_item_from
}

//adapter 2
class ChatItemTo(private val messageText: String, private val user: User?): Item(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_chat_row_to.text = messageText
        val targetImageView = viewHolder.itemView.circleimageview_chat_row_to
        Picasso.get().load(user?.profileImageUrl).into(targetImageView)
    }

    override fun getLayout() = R.layout.chat_row_item_to
}