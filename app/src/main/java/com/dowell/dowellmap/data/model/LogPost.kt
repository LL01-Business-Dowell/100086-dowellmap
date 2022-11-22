package com.dowell.dowellmap.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LogPost(
    var Browser: String? = null,
    var Connection: String? = null,
    var Device: String? = null,
    var IP: String? = null,
    var Location: String? = null,
    var OS: String? = null,
    var Time: String? = null,
    var Username: String? = null
) : Parcelable
