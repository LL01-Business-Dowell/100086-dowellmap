package com.dowell.dowellmap.data.model


import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

@Parcelize
data class GeocodeModel(
    var results: List<Result>? = null,
    var status: String? = null
) : Parcelable {
    @Parcelize
    data class Result(
        var address_components: List<AddressComponent>? = null,
        var formatted_address: String? = null,
        var geometry: Geometry? = null,
        var partial_match: Boolean? = null,
        var place_id: String? = null,
        var types: List<String>? = null
    ) : Parcelable {
        @Parcelize
        data class AddressComponent(
            var long_name: String? = null,
            var short_name: String? = null,
            var types: List<String>? = null
        ) : Parcelable

        @Parcelize
        data class Geometry(
            var bounds: Bounds? = null,
            var location: Location? = null,
            var location_type: String? = null,
            var viewport: Viewport? = null
        ) : Parcelable {
            @Parcelize
            data class Bounds(
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

            @Parcelize
            data class Location(
                var lat: Double? = null,
                var lng: Double? = null
            ) : Parcelable {
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