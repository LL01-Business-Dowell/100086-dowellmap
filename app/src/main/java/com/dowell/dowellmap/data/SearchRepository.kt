package com.dowell.dowellmap.data

import com.dowell.dowellmap.data.model.LogPost
import com.dowell.dowellmap.data.network.ApiService
import com.dowell.dowellmap.data.network.ApiService.Companion.getInstance
import com.dowell.dowellmap.data.network.ApiService.Companion.getLogInstance
import com.dowell.dowellmap.data.network.SafeApiCall


class SearchRepository: SafeApiCall {

    suspend fun getSearchPrediction(input:String) = safeApiCall{
        getInstance().getPredictions(input)
    }

    suspend fun getLocationDetail(place_id:String) = safeApiCall {
        getInstance().getLocationDetail(place_id = place_id)
    }

    suspend fun getDirection(origin: String?, destination:String, waypoints:String) = safeApiCall {
        getInstance().getRouteDirections(
            origin = origin,
            destination =destination,
            waypoints = waypoints
        )
    }

    suspend fun getGeocodeDetail(address:String) = safeApiCall {
        getInstance().getGeocodeDetail(
            address = address
        )
    }

    suspend fun getTextSearch(query: String, location: String, radius: Int) = safeApiCall {
        getInstance().textSearch(
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


}