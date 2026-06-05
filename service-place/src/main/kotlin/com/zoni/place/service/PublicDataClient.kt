package com.zoni.place.service

import com.zoni.common.BenefitType
import com.zoni.common.PlaceDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * 공공데이터포털(data.go.kr) API 클라이언트
 *
 * 청년 혜택 관련 공공데이터를 수집한다.
 * 우선순위:
 *   1) 문화체육관광부 청년문화패스 가맹점 (공연·전시 할인)
 *   2) 지자체 청년 할인 가맹점 (지역별 음식점·카페·쇼핑)
 *
 * ※ 실제 API 엔드포인트는 data.go.kr 에서 신청 후 확인 필요.
 *   `public-data.api-key` 가 미설정 상태이면 모든 조회를 스킵한다.
 */
@Component
class PublicDataClient(
    private val restTemplate: RestTemplate,

    /** data.go.kr 공공데이터 인증키 (application-local.yml의 public-data.api-key) */
    @Value("\${public-data.api-key:}")
    private val apiKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        /** 청년문화패스 가맹점 API 기본 URL (실제 엔드포인트 확인 후 교체 필요) */
        private const val YOUTH_CULTURE_BASE_URL =
            "https://api.data.go.kr/openapi/tn_pubr_public_art_pfsn_info01"

        private const val DEFAULT_PAGE_SIZE = 100
    }

    /**
     * 청년문화패스 가맹 장소 수집
     *
     * 1. API 키가 없으면 빈 목록 반환 (개발 환경 안전 처리)
     * 2. 공공데이터 API 호출 → 응답 파싱
     * 3. 각 장소를 [PublicBenefitPlace] DTO로 변환하여 반환
     *
     * @param pageNo 조회 페이지 (1-based)
     * @return 청년문화패스 혜택 장소 목록
     */
    fun fetchYouthCulturePassPlaces(pageNo: Int = 1): List<PublicBenefitPlace> {
        if (apiKey.isBlank()) {
            log.warn("[PublicDataClient] public-data.api-key 미설정 — 청년문화패스 조회 스킵")
            return emptyList()
        }
        return runCatching {
            // TODO: 실제 data.go.kr 청년문화패스 API 엔드포인트 확인 후 구현
            // val url = "$YOUTH_CULTURE_BASE_URL?serviceKey=$apiKey&pageNo=$pageNo&numOfRows=$DEFAULT_PAGE_SIZE&type=json"
            // val response = restTemplate.getForObject(url, YouthCulturePassResponse::class.java)
            // response?.items?.map { it.toPublicBenefitPlace() } ?: emptyList()
            log.info("[PublicDataClient] 청년문화패스 API 연동 준비 중 (pageNo: {})", pageNo)
            emptyList<PublicBenefitPlace>()
        }.getOrElse { e ->
            log.error("[PublicDataClient] 청년문화패스 API 호출 실패: {}", e.message)
            emptyList()
        }
    }

    /**
     * 지자체 청년 할인 가맹점 수집
     *
     * @param region 지역 코드 (예: "11" = 서울특별시), null이면 전국
     * @return 지자체 청년 할인 가맹점 목록
     */
    fun fetchLocalYouthDiscountPlaces(region: String? = null): List<PublicBenefitPlace> {
        if (apiKey.isBlank()) {
            log.warn("[PublicDataClient] public-data.api-key 미설정 — 지자체 청년 할인 조회 스킵")
            return emptyList()
        }
        return runCatching {
            // TODO: 지자체별 청년 할인 가맹점 API 조사 후 구현
            // 각 지자체마다 별도 API를 제공하거나 통합 API 활용
            log.info("[PublicDataClient] 지자체 청년 할인 API 연동 준비 중 (region: {})", region ?: "전국")
            emptyList<PublicBenefitPlace>()
        }.getOrElse { e ->
            log.error("[PublicDataClient] 지자체 청년 할인 API 호출 실패: {}", e.message)
            emptyList()
        }
    }
}

/**
 * 공공데이터 수집 결과 내부 DTO
 *
 * PublicDataClient 에서 반환하는 표준 형식.
 * PlaceService 에서 이 DTO를 Place 엔티티로 변환하여 저장한다.
 */
data class PublicBenefitPlace(
    /** 장소명 */
    val name: String,
    /** 도로명 주소 */
    val roadAddress: String,
    /** 지번 주소 */
    val address: String,
    /** 카테고리 (공연장, 전시관, 음식점 등) */
    val category: String,
    /** 경도 */
    val x: Double,
    /** 위도 */
    val y: Double,
    /** 전화번호 */
    val phone: String? = null,
    /** 혜택 유형 */
    val benefitType: BenefitType,
    /** 할인·혜택 내용 */
    val discountInfo: String,
    /** 대상 연령 하한 */
    val targetAgeMin: Int? = null,
    /** 대상 연령 상한 */
    val targetAgeMax: Int? = null,
    /** 데이터 출처 */
    val dataSource: PlaceDataSource = PlaceDataSource.PUBLIC_DATA,
    /** 공공데이터 원본 URL */
    val sourceUrl: String? = null
)
