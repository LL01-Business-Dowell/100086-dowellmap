package com.dowell.dowellmap.data.model


import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class UserLogResponse(
    var qrid: String? = null
) : Parcelable