package com.dowell.dowellmap.data.network

import android.accounts.NetworkErrorException
import android.util.Log
import com.dowell.dowellmap.data.model.ApiError
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException

interface SafeApiCall {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        val errorMessage = getErrorApiError(throwable)
                        Resource.Failure(false, throwable.code(), errorMessage)

                    }

                    is NetworkErrorException ->{
                        Resource.Failure(true, null, null)
                    }

                    is ConnectException ->{
                        Resource.Failure(true, null, "Check your connection")
                    }

                    else -> {
                        throwable.message.let { Log.i("Exception1", it.toString()) }
                        Resource.Failure(false, null,  throwable.message)
                    }
                }
            }
        }
    }

    private fun getErrorApiError(httpException: HttpException): String? {
        var errorMessage: String? = null
        try {
            val body = httpException.response()?.errorBody()
            val adapter = Gson().getAdapter(ApiError::class.java)
            val errorParser = adapter.fromJson(body?.string())
            errorMessage = errorParser.status.toString()
                .split("_")[0].first().uppercase()

            Log.i("ErrorCheck",errorMessage.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            return errorMessage
        }
    }
}