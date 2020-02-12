package com.dev.kotlinmessenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_row_item_from.view.*
import kotlinx.android.synthetic.main.chat_row_item_to.view.*

class ChatLogActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>(ComposeMessageActivity.USER_KEY)
        supportActionBar?.title = user?.userName

        setupRecyclerAdapterData()
    }


    private fun setupRecyclerAdapterData() {
        adapter.add(ChatItemFrom("From Message\n..."))
        adapter.add(ChatItemTo("longer longer longer longer longer \n Message message message message..."))
        adapter.add(ChatItemFrom("From Message\n..."))
        adapter.add(ChatItemTo("longer longer longer longer longer \n Message message message message..."))
        adapter.add(ChatItemFrom("From Message\n..."))
        adapter.add(ChatItemTo("longer longer longer longer longer \n Message message message message..."))
        adapter.add(ChatItemFrom("From Message\n..."))
        adapter.add(ChatItemTo("longer longer longer longer longer \n Message message message message..."))

        recyclerview_chat_log.adapter = adapter
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