package com.dev.kotlinmessenger.model.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val uid: String = ""
                , val userName: String = ""
                , val profileImageUrl: String = "")
    :Parcelable