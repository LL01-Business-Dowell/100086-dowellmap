package com.dowell.dowellmap.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class InputSearchModel(
    var next_page_token: String?,
    var results : List<Results>?,
    var status: String?
) : Parcelable {
    @Parcelize
    data class Results(
        var business_status: String?,
        var formatted_address: String?,
        var radius: Int?= 0 ,
        var geometry: Geometry?,
        var icon: String?,
        var icon_background_color: String?,
        var icon_mask_base_uri: String?,
        var name: String?,
        var place_id: String?,
        var plus_code: PlusCode?,
        var price_level: String?,
        var rating: String?,
        var reference: String?,
        var user_ratings_total: String?,
    ) : Parcelable  {
        @Parcelize
        data class Geometry(
            var location: Location? = null,
            var viewport: Viewport? = null

        ) : Parcelable {
            @Parcelize
            data class Viewport(
                var northeast: Northeast? = null,
                var southwest: Southwest? = null

            ) : Parcelable {

                @Parcelize
                data class Southwest(
                    var lat: String?,
                    var lng: String?,
                ) : Parcelable

                @Parcelize
                data class Northeast(
                    var lat: String?,
                    var lng: String?,
                ) : Parcelable
            }
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
        }
        @Parcelize
        data class PlusCode(
            var compound_code: String?,
            var global_code: String?,
        ) : Parcelable

    }

}