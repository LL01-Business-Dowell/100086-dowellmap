package com.dowell.dowellmap.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class APISearchPost (
    @SerializedName("cluster") val cluster: String?,
    @SerializedName("database") val database: String?,
    @SerializedName("collection") val collection: String?,
    @SerializedName("document") val document: String?,
    @SerializedName("team_member_ID") val teamMemberID: String?,
    @SerializedName("function_ID") val functionID: String?,
    @SerializedName("command") val command: String?,
    @SerializedName("field") val test_data: List<testData>?,
    @SerializedName("update_field") val updateField: String?,
    @SerializedName("platform") val order_nos: List<orderNos>?,
        )

data class testData(
    @SerializedName("start_location") val startLocation: String?,
    @SerializedName("stop_location") val stopLocation: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("query_text") val queryText: String?,
    @SerializedName("radius_distance") val radiusDistance: String?,
)

data class orderNos(
    @SerializedName("order_nos") val orderNos: String?
)