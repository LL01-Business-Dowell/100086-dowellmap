package com.dowell.dowellmap.data.model

import com.google.gson.annotations.SerializedName

data class LogPost(
    @SerializedName("Username") val userName: String?,
    @SerializedName("OS") val os: String?,
    @SerializedName("Device") val device: String?,
    @SerializedName("Browser") val browser: String?,
    @SerializedName("Location") val location: String?,
    @SerializedName("Time") val time: String?,
    @SerializedName("Connection") val connection: String?,
    @SerializedName("IP") val ip: String?
)
