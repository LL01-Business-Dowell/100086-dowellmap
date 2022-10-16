package com.dowell.dowellmap.data.model


import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class DirectionResponse(
    var geocoded_waypoints: List<GeocodedWaypoint>? = null,
    var routes: List<Route>? = null,
    var status: String? = null
) : Parcelable {
    @Parcelize
    data class GeocodedWaypoint(
        var geocoder_status: String? = null,
        var place_id: String? = null,
        var types: List<String>? = null
    ) : Parcelable

    @Parcelize
    data class Route(
        var bounds: Bounds? = null,
        var copyrights: String? = null,
        var legs: List<Leg>? = null,
        var overview_polyline: OverviewPolyline? = null,
        var summary: String? = null,
        var waypoint_order: List<Int>? = null
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
        data class Leg(
            var distance: Distance? = null,
            var duration: Duration? = null,
            var end_address: String? = null,
            var end_location: EndLocation? = null,
            var start_address: String? = null,
            var start_location: StartLocation? = null,
            var steps: List<Step>? = null,
        ) : Parcelable {
            @Parcelize
            data class Distance(
                var text: String? = null,
                var value: Int? = null
            ) : Parcelable

            @Parcelize
            data class Duration(
                var text: String? = null,
                var value: Int? = null
            ) : Parcelable

            @Parcelize
            data class EndLocation(
                var lat: Double? = null,
                var lng: Double? = null
            ) : Parcelable

            @Parcelize
            data class StartLocation(
                var lat: Double? = null,
                var lng: Double? = null
            ) : Parcelable

            @Parcelize
            data class Step(
                var distance: Distance? = null,
                var duration: Duration? = null,
                var end_location: EndLocation? = null,
                var html_instructions: String? = null,
                var maneuver: String? = null,
                var polyline: Polyline? = null,
                var start_location: StartLocation? = null,
                var travel_mode: String? = null
            ) : Parcelable {
                @Parcelize
                data class Distance(
                    var text: String? = null,
                    var value: Int? = null
                ) : Parcelable

                @Parcelize
                data class Duration(
                    var text: String? = null,
                    var value: Int? = null
                ) : Parcelable

                @Parcelize
                data class EndLocation(
                    var lat: Double? = null,
                    var lng: Double? = null
                ) : Parcelable

                @Parcelize
                data class Polyline(
                    var points: String? = null
                ) : Parcelable

                @Parcelize
                data class StartLocation(
                    var lat: Double? = null,
                    var lng: Double? = null
                ) : Parcelable
            }
        }

        @Parcelize
        data class OverviewPolyline(
            var points: String? = null
        ) : Parcelable
    }
}