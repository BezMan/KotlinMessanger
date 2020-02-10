package com.dev.kotlinmessenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>(ComposeMessageActivity.USER_KEY)
        supportActionBar?.title = user?.userName

        adapter.add(ChatItemFrom())
        adapter.add(ChatItemTo())
        adapter.add(ChatItemFrom())
        adapter.add(ChatItemTo())

        recyclerview_chat_log.adapter = adapter
    }
}



// Multiple Adapters for multiple recycler item layouts
class ChatItemFrom: Item(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }

    override fun getLayout() = R.layout.chat_row_item_from
}


class ChatItemTo: Item(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }

    override fun getLayout() = R.layout.chat_row_item_to
}