package com.dev.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessagesListActivity : AppCompatActivity() {


    companion object{
        var currentUser: User? = null
    }

    private val className: String = this.javaClass.simpleName
    private val firebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_list)

        fetchCurrentUser()

        verifyUserLoggedIn()
    }



    private fun fetchCurrentUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${firebaseAuth.uid}")
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
        if (firebaseAuth.uid == null) {
            launchRegister()
        }
    }

    private fun launchRegister() {
        val registerIntent = Intent(this, RegisterActivity::class.java)
        registerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(registerIntent)
    }


}
