package com.dowell.dowellmap.data.model


import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

@Parcelize
data class PlaceDetail(
    var result: Result? = null,
    var status: String? = null
) : Parcelable {
    @Parcelize
    data class Result(
        var address_components: List<AddressComponent>? = null,
        var adr_address: String? = null,
        var formatted_address: String? = null,
        var geometry: Geometry? = null,
        var icon: String? = null,
        var icon_background_color: String? = null,
        var icon_mask_base_uri: String? = null,
        var name: String? = null,
        var photos: List<Photo?>? = null,
        var place_id: String? = null,
        var reference: String? = null,
        var types: List<String?>? = null,
        var url: String? = null,
        var utc_offset: Int? = null,
        var vicinity: String? = null
    ) : Parcelable {
        @Parcelize
        data class AddressComponent(
            var long_name: String? = null,
            var short_name: String? = null,
            var types: List<String>? = null
        ) : Parcelable

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
                fun getLatLngToString(): String {
                    return lat?.let {lat->
                        lng?.let { lng ->
                            lat.toString().plus(",").plus(lng)
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

        @Parcelize
        data class Photo(
            var height: Int? = null,
            var html_attributions: List<String>? = null,
            var photo_reference: String? = null,
            var width: Int? = null
        ) : Parcelable
    }
}