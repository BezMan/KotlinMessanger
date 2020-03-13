package com.dev.silverchat.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dev.silverchat.R
import com.dev.silverchat.model.entities.User
import com.dev.silverchat.views.activities.MessagesListActivity.Companion.currentUser
import com.dev.silverchat.views.activities.MessagesListActivity.Companion.firebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.compose_message_activity.*
import kotlinx.android.synthetic.main.user_row.view.*

class ComposeMessageActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_message_activity)

        supportActionBar?.title = "Select User"

        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem

            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(USER_KEY, userItem.user)
            startActivity(intent)
            finish()
        }
        recyclerview_compose_message.adapter = adapter

        fetchUsers()
    }


    private fun fetchUsers() {
        val ref = firebaseDatabase.getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
//Firebase DB changes
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d(className, it.toString())
                    val user = it.getValue(User::class.java)
                    if (user?.uid != currentUser?.uid){
                        adapter.add(UserItem(user))
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }


    companion object{
        const val USER_KEY = "USER_KEY"
    }

}


//Groupie alternative to RecyclerView Adapter class//
class UserItem(val user: User?) : Item() {

    override fun getLayout() = R.layout.user_row

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_textview_message_row.text = user?.userName
        Picasso.get().load(user?.imageUrl).placeholder(R.drawable.ic_face_profile).into(viewHolder.itemView.circleimageview_message_row)
    }
}

