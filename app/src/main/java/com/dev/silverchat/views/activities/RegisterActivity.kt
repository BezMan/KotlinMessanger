package com.dev.silverchat.views.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.silverchat.R
import com.dev.silverchat.model.entities.User
import com.dev.silverchat.views.helpers.BitmapResolver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.register_activity.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private val className: String = this.javaClass.simpleName
    private var selectedPhotoUri: Uri? = null

//LIFECYCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            displayImage(data)
        }
    }


//CLICK EVENTS
    fun registerClicked(view: View) {
        performRegister()
    }


    fun alreadyHaveAccountClicked(view: View) {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }


    fun imageSelectClicked(view: View) {
        val photoIntent = Intent(Intent.ACTION_PICK)
        photoIntent.type = "image/*"
        startActivityForResult(photoIntent, 0)
    }


//METHODS
    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "please enter valid input", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                //else if successful
                Log.d(className, "created user with uid: ${it.result?.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d(className, "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }


    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { it ->
                Log.d(className, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it)
                }
            }
            .addOnFailureListener {
                Log.d(className, "Failed to upload image: ${it.message}")
            }

    }


    private fun saveUserToFirebaseDatabase(profileImageUrl: Uri) {
        Log.d(className, "File location: $profileImageUrl")

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val userName = name_edittext_register.text.toString()

        val user = User(
            uid,
            userName,
            profileImageUrl.toString()
        )
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
    }


    private fun displayImage(data: Intent) {
        selectedPhotoUri = data.data
        val bitmapResolver = BitmapResolver()
        val bitmap = bitmapResolver.getBitmap(contentResolver, selectedPhotoUri)
        photo_circleimageview_register.setImageBitmap(bitmap)
        photo_select_button.alpha = 0f //we still want it to be in front, and clickable
    }


}


