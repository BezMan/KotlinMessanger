package com.dev.silverchat.model.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatMessage(
    val messageId: String? = ""
    , val messageText: String = ""
    , val fromId: String? = ""
    , val toId: String? = ""
    , val timeStamp: Long = System.currentTimeMillis()
) :Parcelable