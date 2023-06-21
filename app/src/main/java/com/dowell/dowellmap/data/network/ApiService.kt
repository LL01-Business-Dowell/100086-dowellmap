package com.dowell.dowellmap.data.network

import com.dowell.dowellmap.BuildConfig
import com.dowell.dowellmap.data.model.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("place/autocomplete/json")
    suspend fun getPredictions(
        @Query("input") input : String,
        @Query("key") key : String = BuildConfig.MAPS_API_KEY
    ) : LocationModel

 @GET("place/textsearch/json")
    suspend fun textSearch(
     @Query("query") query: String,
     @Query("location") location: String,
     @Query("radius") radius: Int,
     @Query("key") key: String = BuildConfig.MAPS_API_KEY
    ) : InputSearchModel

    @GET("place/details/json")
    suspend fun getLocationDetail(
        @Query("place_id") place_id : String,
        @Query("key") key : String = BuildConfig.MAPS_API_KEY
    ) : PlaceDetail

    @GET("directions/json")
    suspend fun getRouteDirections(
        @Query("origin") origin: String?,
        @Query("destination") destination: String,
        @Query("mode") mode: String="driving",
        @Query("waypoints") waypoints: String?=null,
        @Query("key") key: String = BuildConfig.MAPS_API_KEY
    ) : DirectionResponse

    @GET("geocode/json")
    suspend fun getGeocodeDetail(
        @Query("address") address : String,
        @Query("key") key : String = BuildConfig.MAPS_API_KEY
    ) : GeocodeModel


    @POST("linkbased/")
    suspend fun logUser(@Body logpost: LogPost): UserLogResponse

    @POST("create_event/")
    suspend fun eventCreation(@Body eventCreationPost: EventCreationPost): ResponseBody

    @POST("/")
    suspend fun apiSearch(@Body customApiPost: CustomApiPost): CustomApiResponse

    companion object {
        fun getGoogleApiInstance(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(getRetrofitClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        fun getLogInstance() : ApiService {
            return Retrofit.Builder()
                .baseUrl("https://100014.pythonanywhere.com/api/")
                .client(getRetrofitClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        fun getEventCreationInstance() : ApiService {
            return Retrofit.Builder()
                .baseUrl("https://uxlivinglab.pythonanywhere.com/")
                .client(getRetrofitClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        fun getAPISearchInstance() : ApiService {
            return Retrofit.Builder()
                .baseUrl("http://100002.pythonanywhere.com")
                .client(getRetrofitClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        private fun getRetrofitClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder().also {
                            it.addHeader("Content-Type", "application/json")
                        }.build()
                    )
                }.also { client ->
                    val logging = HttpLoggingInterceptor()
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)

                    client.connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                        .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                        .readTimeout(5, TimeUnit.MINUTES)
                }.build()

        }
    }
}