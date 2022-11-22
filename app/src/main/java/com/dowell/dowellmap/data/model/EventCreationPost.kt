package com.dowell.dowellmap.data.model

import com.google.gson.annotations.SerializedName

data class EventCreationPost (
    @SerializedName("platformcode") val platformCode: String?=null,
    @SerializedName("citycode") val cityCode: String?=null,
    @SerializedName("daycode") val dayCode: String?=null,
    @SerializedName("dbcode") val dbCode: String?=null,
    @SerializedName("ip_address") val ipAddress: String?=null,
    @SerializedName("login_id") val loginID: String?=null,
    @SerializedName("session_id") val sessionID: String?=null,
    @SerializedName("processcode") val processCode: String?=null,
    @SerializedName("regional_time") val regionalTime: String?=null,
    @SerializedName("dowell_time") val dowellTime: String?=null,
    @SerializedName("location") val location: String?=null,
    @SerializedName("objectcode") val objectCode: String?=null,
    @SerializedName("instancecode") val instanceCode: String?=null,
    @SerializedName("context") val context: String?=null,
    @SerializedName("document_id") val documentID: String?=null,
    @SerializedName("rules") val rules: String?=null,
    @SerializedName("status") val status: String?=null,
        )