package com.dev.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_messages_list.*
import kotlinx.android.synthetic.main.message_row.view.*

class MessagesListActivity : AppCompatActivity() {


    companion object{
        var currentUser: User? = null
    }

    private val className: String = this.javaClass.simpleName
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val fromId = firebaseAuth.uid
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val latestMessagesMap = HashMap<String?, ChatMessage?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_list)

        fetchCurrentUser()

        verifyUserLoggedIn()

        setupList()
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

        val ref = firebaseDatabase.getReference("/latest-messages/$fromId")
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
        val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")
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
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_new_message -> {
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
        if (fromId == null) {
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

    override fun getLayout(): Int = R.layout.message_row

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.last_message_textview_message_row.text = chatMessage?.messageText ?: ""

        val partnerId: String? = if (chatMessage?.fromId == FirebaseAuth.getInstance().uid)
            chatMessage?.toId else chatMessage?.fromId

        val ref = FirebaseDatabase.getInstance().getReference("/users/$partnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                partnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_textview_message_row.text = partnerUser?.userName

                val targetImageView = viewHolder.itemView.imageview_message_row
                Picasso.get().load(partnerUser?.profileImageUrl).into(targetImageView)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }

}