package com.dev.kotlinmessenger

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun registerClicked(view: View) {
        Log.d("MainActivity", name_edittext_register.text.toString())
        Log.d("MainActivity", email_edittext_register.text.toString())
        Log.d("MainActivity", password_edittext_register.text.toString())
    }

    fun alreadyHaveAccountClicked(view: View) {}
}
