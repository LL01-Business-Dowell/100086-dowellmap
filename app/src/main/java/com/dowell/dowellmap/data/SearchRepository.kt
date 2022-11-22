package com.dowell.dowellmap.data

import com.dowell.dowellmap.data.model.CustomApiPost
import com.dowell.dowellmap.data.model.EventCreationPost
import com.dowell.dowellmap.data.model.LogPost
import com.dowell.dowellmap.data.network.ApiService.Companion.getAPISearchInstance
import com.dowell.dowellmap.data.network.ApiService.Companion.getEventCreationInstance
import com.dowell.dowellmap.data.network.ApiService.Companion.getGoogleApiInstance
import com.dowell.dowellmap.data.network.ApiService.Companion.getLogInstance
import com.dowell.dowellmap.data.network.SafeApiCall


class SearchRepository: SafeApiCall {

    suspend fun getSearchPrediction(input:String) = safeApiCall{
        getGoogleApiInstance().getPredictions(input)
    }

    suspend fun getLocationDetail(place_id:String) = safeApiCall {
        getGoogleApiInstance().getLocationDetail(place_id = place_id)
    }

    suspend fun getDirection(origin: String?, destination:String, waypoints:String) = safeApiCall {
        getGoogleApiInstance().getRouteDirections(
            origin = origin,
            destination =destination,
            waypoints = waypoints
        )
    }

    suspend fun getGeocodeDetail(address:String) = safeApiCall {
        getGoogleApiInstance().getGeocodeDetail(
            address = address
        )
    }

    suspend fun getTextSearch(query: String, location: String, radius: Int) = safeApiCall {
        getGoogleApiInstance().textSearch(
            radius = radius,
            location = location,
            query = query,
        )
    }

    suspend fun makeLog(logPost: LogPost) = safeApiCall {
        getLogInstance().logUser(
            logPost
        )
    }

    suspend fun makeEventCreation(eventCreationPost: EventCreationPost) = safeApiCall {
        getEventCreationInstance().eventCreation(
            eventCreationPost
        )
    }

suspend fun sendData(apiSearchPost: CustomApiPost) = safeApiCall {
    getAPISearchInstance().apiSearch(
            apiSearchPost
        )
    }


}