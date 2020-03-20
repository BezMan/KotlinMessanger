package com.dev.silverchat.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.dev.silverchat.MyApplication
import com.dev.silverchat.R
import com.dev.silverchat.helpers.DateUtils
import com.dev.silverchat.model.entities.ChatMessage
import com.dev.silverchat.model.entities.UnreadMessages
import com.dev.silverchat.model.entities.User
import com.dev.silverchat.views.activities.MainListActivity.Companion.firebaseDatabase
import com.dev.silverchat.views.activities.MainListActivity.Companion.myId
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.main_list_activity.*
import kotlinx.android.synthetic.main.main_list_message_row.view.*

class MainListActivity : AppCompatActivity() {


    companion object{
        const val REQUEST_CODE_SIGN_IN = 111
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
        setContentView(R.layout.main_list_activity)

        verifyUserLoggedIn()
    }

    override fun onStart() {
        super.onStart()
        setupList()
    }

    override fun onBackPressed() {
        //dont destroy, for fast return to app.
        moveTaskToBack(true)
    }


    private fun setupList() {
        recyclerview_messages_list.adapter = adapter
        recyclerview_messages_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val messageItem = item as MainListItem

            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(FindFriendsActivity.USER_KEY, messageItem.partnerUser)
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
            adapter.add(MainListItem(it))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_find_users -> {
                launchFindFriends()
                }
            R.id.menu_settings -> {
                openSettings()
                }
            R.id.menu_sign_out -> {
                MyApplication.updateUserOnline(false)
                firebaseAuth.signOut()
                openAuthUI()
                }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openSettings() {
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        startActivity(settingsIntent)

    }


    private fun launchFindFriends() {
        val findFriendsIntent = Intent(this, FindFriendsActivity::class.java)
        startActivity(findFriendsIntent)
    }


    private fun verifyUserLoggedIn() {
        myId = firebaseAuth.uid
        if (myId == null) {
            openAuthUI()
        }else{
            setupList()
        }
    }

    private fun openAuthUI() {
        // Choose authentication providers
        val providers: List<AuthUI.IdpConfig> = listOf(
            AuthUI.IdpConfig.EmailBuilder().build()
            , AuthUI.IdpConfig.GoogleBuilder().build()
        )

// Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            REQUEST_CODE_SIGN_IN
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) { // Successfully signed in
                val user = firebaseAuth.currentUser
                if (user != null) {
                    setupList()
                    saveUserInDatabase(user.uid, user.displayName)
                }

            } else {
                finish()
                // Sign in failed. If response is null the user canceled the
// sign-in flow using the back button. Otherwise check
// response.getError().getErrorCode() and handle the error.
// ...
            }
        }
    }


    private fun saveUserInDatabase(uid: String, userName: String?) {
        val user = User(uid, userName, System.currentTimeMillis().toString())
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.setValue(user)
    }


}


//Adapter for RecyclerView item
class MainListItem(private val chatMessage: ChatMessage?) : Item(){

    var partnerUser: User? = null
    var unreadMessages: UnreadMessages? = null

    override fun getLayout(): Int = R.layout.main_list_message_row

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.main_list_textview_message_row.text = chatMessage?.messageText ?: ""

        val partnerId = if (chatMessage?.fromId == myId)
            chatMessage?.toId else chatMessage?.fromId

        val ref = firebaseDatabase.getReference("/users/$partnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                partnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_textview_message_row.text = partnerUser?.userName
                viewHolder.itemView.main_list_time_textview.text = DateUtils.getFormattedTimeLatestMessage(chatMessage?.timeStamp!!)

                val targetImageView = viewHolder.itemView.imageview_message_row
                if(!partnerUser?.imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(partnerUser?.imageUrl).into(targetImageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

        val refUnread = firebaseDatabase.getReference("/unread-messages/$myId/$partnerId")
        refUnread.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                unreadMessages = p0.getValue(UnreadMessages::class.java)
                if (unreadMessages?.count != 0 && unreadMessages != null) {
                    viewHolder.itemView.main_list_unread_count.visibility = View.VISIBLE
                    viewHolder.itemView.main_list_unread_count.text = unreadMessages?.count.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }

}