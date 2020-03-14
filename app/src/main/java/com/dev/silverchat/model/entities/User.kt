package com.dev.silverchat.model.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val uid: String = ""
    , val userName: String? = ""
    , val timeJoined: String = ""
    , val imageUrl: String? = ""
    , val tokenId: String = ""
    , val statusText: String = "using SilverChat!"
    , val isOnline: Boolean = true
) : Parcelable