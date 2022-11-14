package com.dowell.dowellmap.data.model


import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class ApiSearchResponse(
    var isSuccess: String? = null,
    var inserted_id: String? = null
) : Parcelable