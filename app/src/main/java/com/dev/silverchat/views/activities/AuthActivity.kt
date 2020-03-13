package com.dev.silverchat.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dev.silverchat.model.entities.User
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName

    companion object {
        const val REQUEST_CODE_SIGN_IN = 111
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openAuthUI()
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
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val displayName = user.displayName
                    val uid = user.uid
                    saveUserInDatabase(uid, displayName)
                }

            } else { // Sign in failed. If response is null the user canceled the
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
            .addOnSuccessListener {
                Log.d(className, "saved user to Firebase database")
                launchMessagesActivity()
            }
            .addOnFailureListener {
                Log.d(className, "Failed to save user: ${it.message}")
            }

    }


    private fun launchMessagesActivity() {
        val intent = Intent(this, MessagesListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


}