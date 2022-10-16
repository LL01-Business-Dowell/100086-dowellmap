package com.dowell.dowellmap.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationModel(
    var predictions: List<Prediction?>?,
    var status: String?
) : Parcelable {
    @Parcelize
    data class Prediction(
        var description: String? = null,
        var place_id: String? = null,
        var reference: String? = null,
        var structured_formatting: StructuredFormatting? = null,
        var terms: List<Term?>? = null,
        var types: List<String?>? = null
    ) : Parcelable {
        @Parcelize
        data class MatchedSubstring(
            var length: Int? = null,
            var offset: Int? = null
        ) : Parcelable

        @Parcelize
        data class StructuredFormatting(
            var main_text: String? = null,
            var secondary_text: String? = null
        ) : Parcelable

        @Parcelize
        data class Term(
            var offset: Int? = null,
            var value: String? = null
        ) : Parcelable
    }
}