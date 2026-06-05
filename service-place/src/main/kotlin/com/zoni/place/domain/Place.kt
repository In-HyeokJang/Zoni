package com.zoni.place.domain

import com.zoni.common.BenefitType
import com.zoni.common.PlaceDataSource
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 장소 엔티티
 *
 * 카카오맵 API로 검색된 장소를 저장하며,
 * Phase 3-B 피보팅 이후에는 청년 혜택 정보(benefitType, discountInfo 등)를 함께 보유할 수 있다.
 * 혜택 관련 필드는 모두 nullable — 혜택 없는 일반 장소도 저장 가능.
 */
@Entity
@Table(
    name = "places",
    indexes = [
        Index(name = "idx_places_user_id", columnList = "user_id"),
        Index(name = "idx_places_kakao_place_id", columnList = "kakao_place_id"),
        // [Phase 3-B] 혜택 유형별 필터링 성능을 위해 인덱스 추가
        Index(name = "idx_places_benefit_type", columnList = "benefit_type")
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

    // ── Phase 3-B: 청년 혜택 필드 ─────────────────────────────────────────

    /**
     * 혜택 유형 (null = 혜택 정보 없음)
     * YOUTH_CULTURE_PASS / LOCAL_YOUTH_DISCOUNT / USER_VERIFIED / PUBLIC_API
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type")
    var benefitType: BenefitType? = null,

    /**
     * 할인/혜택 내용 텍스트 (예: "청년문화패스 50% 할인", "청년 20% 할인 가맹점")
     * benefitType 이 존재할 때만 유효
     */
    @Column(name = "discount_info", length = 300)
    var discountInfo: String? = null,

    /** 혜택 대상 연령 하한 (예: 19 → 19세 이상) */
    @Column(name = "target_age_min")
    var targetAgeMin: Int? = null,

    /** 혜택 대상 연령 상한 (예: 34 → 34세 이하) */
    @Column(name = "target_age_max")
    var targetAgeMax: Int? = null,

    /**
     * 데이터 출처 유형 (null = KAKAO_MAP 기본)
     * KAKAO_MAP / PUBLIC_DATA / USER_INPUT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_source")
    var dataSource: PlaceDataSource? = PlaceDataSource.KAKAO_MAP,

    /** 혜택 출처 URL (공공데이터 API URL 또는 사용자가 제공한 증빙 링크) */
    @Column(name = "source_url", length = 500)
    var sourceUrl: String? = null,

    // ─────────────────────────────────────────────────────────────────────

    var viewCount: Int = 0,
    var isDeleted: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
