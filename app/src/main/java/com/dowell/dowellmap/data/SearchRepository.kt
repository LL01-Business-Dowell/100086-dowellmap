package com.dowell.dowellmap.data

import com.dowell.dowellmap.data.network.ApiService
import com.dowell.dowellmap.data.network.SafeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository: SafeApiCall {

    suspend fun getSearchPrediction(input:String) = safeApiCall{
        ApiService.getInstance().getPredictions(input)
    }

    suspend fun getLocationDetail(place_id:String) = safeApiCall {
        ApiService.getInstance().getLocationDetail(place_id = place_id)
    }

    suspend fun getDirection(origin: String?, destination:String, waypoints:String) = safeApiCall {
        ApiService.getInstance().getRouteDirections(
            origin = origin,
            destination =destination,
            waypoints = waypoints
        )
    }

    suspend fun getGeocodeDetail(address:String) = safeApiCall {
        ApiService.getInstance().getGeocodeDetail(
            address = address
        )
    }

}