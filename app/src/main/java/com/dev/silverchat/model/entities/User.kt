package com.dev.silverchat.model.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val uid: String = ""
    , val userName: String = ""
    , val imageUrl: String = ""
    , val timeJoined: String = ""
    , val tokenId: String = ""
    , val aboutMe: String = "using SilverChat!"
    , val isOnline: Boolean = false
) : Parcelable