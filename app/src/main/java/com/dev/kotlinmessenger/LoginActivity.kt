package com.dev.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginClicked(view: View) {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                launchMessagesActivity()
            }
            .addOnFailureListener {
                Log.d(className, "Failed to login: ${it.message}")
                Toast.makeText(this, "Failed to login: ${it.message}", Toast.LENGTH_LONG).show()

            }

    }


    private fun launchMessagesActivity() {
        val intent = Intent(this, MessagesListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    fun backToRegisterClicked(view: View) {
        finish()
    }
}
