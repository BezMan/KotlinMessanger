package com.dev.silverchat.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.dev.silverchat.R
import com.dev.silverchat.model.entities.ChatMessage
import com.dev.silverchat.model.entities.UnreadMessages
import com.dev.silverchat.model.entities.User
import com.dev.silverchat.views.activities.MessagesListActivity.Companion.firebaseDatabase
import com.dev.silverchat.views.activities.MessagesListActivity.Companion.myId
import com.dev.silverchat.views.helpers.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.latest_messages_list_activity.*

class MessagesListActivity : AppCompatActivity() {


    companion object{
        var currentUser: User? = null
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()
        var myId: String? = null
    }

    private val className: String = this.javaClass.simpleName
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val latestMessagesMap = HashMap<String?, ChatMessage?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.latest_messages_list_activity)

        fetchCurrentUser()

        verifyUserLoggedIn()
    }



    override fun onStart() {
        super.onStart()
        setupList()
    }

    override fun onRestart() {
        super.onRestart()
        adapter.notifyDataSetChanged()
    }


    private fun setupList() {
        recyclerview_messages_list.adapter = adapter
        recyclerview_messages_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val messageItem = item as LatestMessageItem

            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(ComposeMessageActivity.USER_KEY, messageItem.partnerUser)
            startActivity(intent)
        }

        val ref = firebaseDatabase.getReference("/latest-messages/$myId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                refreshListMessages(p0)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                refreshListMessages(p0)
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }


    private fun refreshListMessages(p0: DataSnapshot){
        val chatMessage = p0.getValue(ChatMessage::class.java)
        latestMessagesMap[p0.key] = chatMessage
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageItem(it))
        }
    }


    private fun fetchCurrentUser() {
        myId = firebaseAuth.uid
        val ref = firebaseDatabase.getReference("/users/$myId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d(className, currentUser.toString())
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.latest_messages_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_find_users -> {
                launchComposeMessage()
                }
            R.id.menu_sign_out -> {
                firebaseAuth.signOut()
                launchRegister()
                }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun launchComposeMessage() {
        val composeIntent = Intent(this, ComposeMessageActivity::class.java)
        startActivity(composeIntent)
    }


    private fun verifyUserLoggedIn() {
        if (myId == null) {
            launchRegister()
        }
    }

    private fun launchRegister() {
        val registerIntent = Intent(this, RegisterActivity::class.java)
        registerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(registerIntent)
    }


}


//Adapter for RecyclerView item
class LatestMessageItem(private val chatMessage: ChatMessage?) : Item(){

    var partnerUser: User? = null
    var unreadMessages: UnreadMessages? = null

    override fun getLayout(): Int = R.layout.latest_message_row

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.last_message_textview_message_row.text = chatMessage?.messageText ?: ""

        val partnerId = if (chatMessage?.fromId == myId)
            chatMessage?.toId else chatMessage?.fromId

        val ref = firebaseDatabase.getReference("/users/$partnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                partnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_textview_message_row.text = partnerUser?.userName
                viewHolder.itemView.time_latest_message_textview.text = DateUtils.getFormattedTimeLatestMessage(chatMessage?.timeStamp!!)

                val targetImageView = viewHolder.itemView.imageview_message_row
                Picasso.get().load(partnerUser?.profileImageUrl).into(targetImageView)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

        val refUnread = firebaseDatabase.getReference("/unread-messages/$myId/$partnerId")
        refUnread.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                unreadMessages = p0.getValue(UnreadMessages::class.java)
                viewHolder.itemView.unread_count_latest_message.text = unreadMessages?.count.toString()
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }

}