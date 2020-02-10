package com.dev.kotlinmessenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_compose_message.*

class ComposeMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose_message)

        supportActionBar?.title = "Select User"

        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.add(UserItem())
        adapter.add(UserItem())
        adapter.add(UserItem())
        recyclerview_compose_message.adapter = adapter

        fetchUsers()
    }

    private fun fetchUsers() {

    }
}

class UserItem(/*private val song: User*/) : Item() {

    override fun getLayout() = R.layout.user_row

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        viewHolder.title.text = song.title
//        viewHolder.artist.text = song.artist
    }
}

