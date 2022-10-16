package com.dowell.dowellmap.data.model


import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class ApiError(
    var error_message: String? = null,
    var status: String? = null
) : Parcelable