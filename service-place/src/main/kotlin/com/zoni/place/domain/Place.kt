package com.zoni.place.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "places",
    indexes = [
        Index(name = "idx_places_user_id", columnList = "user_id"),
        Index(name = "idx_places_kakao_place_id", columnList = "kakao_place_id")
    ]
)
class Place(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** 카카오 장소 고유 ID */
    @Column(name = "kakao_place_id", nullable = false)
    val kakaoPlaceId: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val category: String,

    @Column(nullable = false)
    val address: String,

    @Column(name = "road_address", nullable = false)
    val roadAddress: String,

    val phone: String? = null,

    @Column(name = "place_url", length = 500)
    val placeUrl: String? = null,

    /** 경도 */
    @Column(nullable = false)
    val x: Double,

    /** 위도 */
    @Column(nullable = false)
    val y: Double,

    /** 저장한 사용자 (JWT에서 추출, 비정규화) */
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val nickname: String,

    var viewCount: Int = 0,
    var isDeleted: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)