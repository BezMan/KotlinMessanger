package com.dev.silverchat.model.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UnreadMessages(val count: Int? = 0)
    :Parcelable