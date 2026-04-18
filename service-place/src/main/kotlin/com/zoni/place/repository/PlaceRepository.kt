package com.zoni.place.repository

import com.zoni.place.domain.Place
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PlaceRepository : JpaRepository<Place, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): Place?

    fun findByUserIdAndIsDeletedFalse(userId: Long, pageable: Pageable): Page<Place>

    /** 인기 장소: 저장 횟수(같은 kakaoPlaceId) 많은 순 TOP N */
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

    fun existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse(kakaoPlaceId: String, userId: Long): Boolean
}