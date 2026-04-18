package com.zoni.place.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PlaceSaveRequest(
    @field:NotBlank(message = "카카오 장소 ID는 필수입니다")
    val kakaoPlaceId: String,

    @field:NotBlank(message = "장소명은 필수입니다")
    val name: String,

    @field:NotBlank(message = "카테고리는 필수입니다")
    val category: String,

    @field:NotBlank(message = "주소는 필수입니다")
    val address: String,

    val roadAddress: String = "",
    val phone: String? = null,
    val placeUrl: String? = null,

    @field:NotNull(message = "경도(x)는 필수입니다")
    val x: Double,

    @field:NotNull(message = "위도(y)는 필수입니다")
    val y: Double
)