package com.dowell.dowellmap.data.model
import com.google.gson.annotations.SerializedName

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class CustomApiPost(
    @SerializedName("cluster")
    val cluster: String?=null,
    @SerializedName("collection")
    val collection: String?=null,
    @SerializedName("command")
    val command: String?=null,
    @SerializedName("database")
    val database: String?=null,
    @SerializedName("document")
    val document: String?=null,
    @SerializedName("field")
    val field: Field?=null,
    @SerializedName("function_ID")
    val functionID: String?=null,
    @SerializedName("platform")
    val platform: String?=null,
    @SerializedName("team_member_ID")
    val teamMemberID: String?=null,
    @SerializedName("update_field")
    val updateField: UpdateField?=null
) : Parcelable {
    @Parcelize
    data class Field(
        @SerializedName("test_data")
        val testData: TestData?=null
    ) : Parcelable {
        @Parcelize
        data class TestData(
            @SerializedName("eventId") val eventId: String?=null,
            @SerializedName("req_id") val reqId: String?=null,
            @SerializedName("address") val address: String?=null,
            @SerializedName("lat_lon") val responseLatLon: String?=null,
            @SerializedName("data") val data: String?=null,
            @SerializedName("is_error") val is_error: Boolean?=null,
            @SerializedName("error") val error: String?=null,

            //place request custom data
            @SerializedName("start_address") val startAddress: String?=null,
            @SerializedName("start_lat_lon") val startLocation: String?=null,
            @SerializedName("url") val url: String?=null,
            @SerializedName("query_text") val queryText: String?=null,
            @SerializedName("radius_distance") val radiusDistance: String?=null,

            //log custom data
            @SerializedName("req_type") val reqType: String?=null,
            @SerializedName("data_time_done") val dataTimeDone: String?=null,
            @SerializedName("user_name") val userName: String?=null,
            @SerializedName("session_id") val sessionId: String?=null,
            @SerializedName("location_done") val locationDone: String?=null,
        ) : Parcelable
    }

    @Parcelize
    data class UpdateField(
        @SerializedName("order_nos")
        val orderNos: Int?=0
    ) : Parcelable
}