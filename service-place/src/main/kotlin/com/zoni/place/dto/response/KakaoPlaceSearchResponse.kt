package com.zoni.place.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

/** 카카오 로컬 API 장소 검색 응답 */
data class KakaoPlaceSearchResponse(
    val documents: List<KakaoPlace> = emptyList(),
    val meta: Meta = Meta()
) {
    data class KakaoPlace(
        val id: String = "",

        @JsonProperty("place_name")
        val placeName: String = "",

        @JsonProperty("category_name")
        val categoryName: String = "",

        @JsonProperty("address_name")
        val addressName: String = "",

        @JsonProperty("road_address_name")
        val roadAddressName: String = "",

        val phone: String = "",

        @JsonProperty("place_url")
        val placeUrl: String = "",

        /** 경도 */
        val x: String = "",

        /** 위도 */
        val y: String = "",

        val distance: String = ""
    )

    data class Meta(
        @JsonProperty("total_count")
        val totalCount: Int = 0,

        @JsonProperty("pageable_count")
        val pageableCount: Int = 0,

        @JsonProperty("is_end")
        val isEnd: Boolean = true
    )
}