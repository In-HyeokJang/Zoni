package com.zoni.place.repository

import com.zoni.common.BenefitType
import com.zoni.place.domain.Place
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PlaceRepository : JpaRepository<Place, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): Place?

    fun findByUserIdAndIsDeletedFalse(userId: Long, pageable: Pageable): Page<Place>

    fun existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse(kakaoPlaceId: String, userId: Long): Boolean

    /** 인기 장소: kakaoPlaceId 저장 횟수 많은 순, ONLY_FULL_GROUP_BY 호환 서브쿼리 방식 */
    @Query(
        value = """
            SELECT p.* FROM places p
            INNER JOIN (
                SELECT kakao_place_id, COUNT(*) AS cnt, MAX(id) AS max_id
                FROM places
                WHERE is_deleted = false
                GROUP BY kakao_place_id
            ) t ON p.id = t.max_id
            ORDER BY t.cnt DESC, p.created_at DESC
            LIMIT :size
        """,
        nativeQuery = true
    )
    fun findPopularPlaces(@Param("size") size: Int): List<Place>

    // ── Phase 3-B: 혜택 필터 쿼리 ────────────────────────────────────────

    /** 특정 혜택 유형으로 등록된 장소 목록 (최신순) */
    fun findByBenefitTypeAndIsDeletedFalse(benefitType: BenefitType, pageable: Pageable): Page<Place>

    /** 혜택 정보가 있는 장소 전체 목록 (최신순) */
    fun findByBenefitTypeIsNotNullAndIsDeletedFalse(pageable: Pageable): Page<Place>

    /**
     * 나이 기반 혜택 장소 필터
     * - targetAgeMin <= age <= targetAgeMax 범위에 포함되는 장소
     * - targetAgeMin 또는 targetAgeMax 가 null 인 경우 해당 조건은 무시
     */
    @Query("""
        SELECT p FROM Place p
        WHERE p.isDeleted = false
          AND p.benefitType IS NOT NULL
          AND (:age IS NULL
               OR (p.targetAgeMin IS NULL OR p.targetAgeMin <= :age)
               AND (p.targetAgeMax IS NULL OR p.targetAgeMax >= :age))
        ORDER BY p.createdAt DESC
    """)
    fun findBenefitPlacesForAge(@Param("age") age: Int?, pageable: Pageable): Page<Place>
}
