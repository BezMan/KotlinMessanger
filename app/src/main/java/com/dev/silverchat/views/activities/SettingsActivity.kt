package com.dev.silverchat.views.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.silverchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.settings_activity.*
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private var currentUserID: String? = null
    private var databaseRoot: DatabaseReference? = null
    private var storageRoot: StorageReference? = null
    private var loadingBar: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        databaseRoot = FirebaseDatabase.getInstance().reference
        storageRoot = FirebaseStorage.getInstance().reference.child("images")
        initViews()
        retrieveUserInfo()
        initClickEvents()
    }


    private fun initClickEvents() {
        update_settings_button.setOnClickListener {
            updateSettings()
        }
        set_profile_image.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, CROP_IMAGE_REQUEST_CODE)
        }
    }

    private fun initViews() {
        loadingBar = ProgressDialog(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.title = "Account Settings"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri).start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                loadingBar!!.setTitle("Set Profile Image")
                loadingBar!!.setMessage("Please wait, your profile image is updating...")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()
                val resultUri = result.uri
                //save image in Storage and getDownloadUrl to save image url in Database,
                val storageRefImage = storageRoot!!.child("$currentUserID.jpg")
                storageRefImage.putFile(resultUri)
                    .addOnSuccessListener {
                        storageRefImage.downloadUrl
                            .addOnSuccessListener { uri ->
                                databaseRoot!!.child("users").child(currentUserID!!)
                                    .child("imageUrl").setValue(uri.toString())
                            }
                    }
                    .addOnCompleteListener {
                        loadingBar!!.dismiss()
                    }
            }
        }
    }

    private fun updateSettings() {
        val setUserName = set_user_name.text.toString()
        val setStatus = set_profile_status.text.toString()
        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show()
        } else {
            val profileMap = HashMap<String, Any?>()
            profileMap["uid"] = currentUserID
            profileMap["userName"] = setUserName
            profileMap["statusAbout"] = setStatus
            databaseRoot!!.child("users").child(currentUserID!!).updateChildren(profileMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        sendUserToMainActivity()
                        Toast.makeText(this@SettingsActivity, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show()
                    } else {
                        val message = task.exception.toString()
                        Toast.makeText(this@SettingsActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun retrieveUserInfo() {
        databaseRoot!!.child("users").child(currentUserID!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("userName")) {
                            set_user_name.setText(dataSnapshot.child("userName").value.toString())
                        }
                        if (dataSnapshot.hasChild("imageUrl")) {
                            Picasso.get().load(dataSnapshot.child("imageUrl").value.toString()).placeholder(R.drawable.ic_face_profile).into(set_profile_image)
                        }
                        if (dataSnapshot.hasChild("statusAbout")) {
                            set_profile_status.setText(dataSnapshot.child("statusAbout").value.toString())
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this, MessagesListActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    companion object {
        private const val CROP_IMAGE_REQUEST_CODE = 1
    }
}