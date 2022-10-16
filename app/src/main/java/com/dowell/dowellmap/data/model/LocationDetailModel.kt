package com.dowell.dowellmap.data.model


import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

@Parcelize
data class LocationDetailModel(
    var result: Result? = null,
    var status: String? = null
) : Parcelable {
    @Parcelize
    data class Result(
        var geometry: Geometry? = null
    ) : Parcelable {
        @Parcelize
        data class Geometry(
            var location: Location? = null,
            var viewport: Viewport? = null
        ) : Parcelable {
            @Parcelize
            data class Location(
                var lat: Double? = null,
                var lng: Double? = null
            ) : Parcelable{
                fun getLatLng(): LatLng {
                    return lat?.let {lat->
                        lng?.let { lng ->
                            LatLng(lat, lng)
                        }
                    }!!
                }
            }

            @Parcelize
            data class Viewport(
                var northeast: Northeast? = null,
                var southwest: Southwest? = null
            ) : Parcelable {
                @Parcelize
                data class Northeast(
                    var lat: Double? = null,
                    var lng: Double? = null
                ) : Parcelable

                @Parcelize
                data class Southwest(
                    var lat: Double? = null,
                    var lng: Double? = null
                ) : Parcelable
            }
        }
    }
}