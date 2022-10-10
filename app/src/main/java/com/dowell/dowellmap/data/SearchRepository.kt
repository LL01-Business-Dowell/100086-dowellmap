package com.dowell.dowellmap.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository {

    suspend fun getSearchPrediction(input:String) = run {
        withContext(Dispatchers.IO){
            try {
                ApiService.getInstance().getPredictions(input)
            } catch (e: Exception) {
               e.message.toString()
            }
        }
    }
}