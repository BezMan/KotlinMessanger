package com.dev.silverchat

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.dev.silverchat.views.activities.MainListActivity.Companion.USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MyApplication : Application(), LifecycleObserver {

    companion object {
        fun isInForeground(): Boolean {
            return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }

        fun updateUserOnline(isOnline: Boolean) {
            val onlineStateMap = HashMap<String, Any>()
            onlineStateMap["last_seen"] = System.currentTimeMillis().toString()
            onlineStateMap["online"] = isOnline
            val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
            if(!currentUserID.isNullOrEmpty()) {
                FirebaseDatabase.getInstance().reference.child(USERS).child(currentUserID)
                    .updateChildren(onlineStateMap)
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() { // App in foreground
        updateUserOnline(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() { //App in background
        updateUserOnline(false)
    }

}


