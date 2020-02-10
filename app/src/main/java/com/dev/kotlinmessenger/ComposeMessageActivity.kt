package com.dev.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_compose_message.*
import kotlinx.android.synthetic.main.user_row.view.*

class ComposeMessageActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose_message)

        supportActionBar?.title = "Select User"

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
//            intent.putExtra()
            startActivity(intent)
        }
        recyclerview_compose_message.adapter = adapter

        fetchUsers()
    }


    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
//Firebase DB changes
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d(className, it.toString())
                    val user = it.getValue(User::class.java)
                    adapter.add(UserItem(user))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }


}


//Groupie alternative to RecyclerView Adapter class//
class UserItem(private val user: User?) : Item() {

    override fun getLayout() = R.layout.user_row

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_textview_message_row.text = user?.userName
        Picasso.get().load(user?.profileImageUrl).into(viewHolder.itemView.circleimageview_message_row)
    }
}

