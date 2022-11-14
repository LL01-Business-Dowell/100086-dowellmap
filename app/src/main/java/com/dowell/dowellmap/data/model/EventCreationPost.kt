package com.dowell.dowellmap.data.model

import com.google.gson.annotations.SerializedName

data class EventCreationPost (
    @SerializedName("platformcode") val platformCode: String?,
    @SerializedName("citycode") val cityCode: String?,
    @SerializedName("daycode") val dayCode: String?,
    @SerializedName("dbcode") val dbCode: String?,
    @SerializedName("ip_address") val ipAddress: String?,
    @SerializedName("login_id") val loginID: String?,
    @SerializedName("session_id") val sessionID: String?,
    @SerializedName("processcode") val processCode: String?,
    @SerializedName("regional_time") val regionalTime: String?,
    @SerializedName("dowell_time") val dowellTime: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("objectcode") val objectCode: String?,
    @SerializedName("instancecode") val instanceCode: String?,
    @SerializedName("context") val context: String?,
    @SerializedName("document_id") val documentID: String?,
    @SerializedName("rules") val rules: String?,
    @SerializedName("status") val status: String?,
        )