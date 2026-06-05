package com.zoni.common

/**
 * 청년 혜택 유형
 *
 * 장소에 적용 가능한 혜택의 출처 및 종류를 구분한다.
 * Phase 3-B 피보팅에서 신설 — 혜택 레이어의 핵심 분류체계.
 */
enum class BenefitType(val displayName: String) {
    /** 문화체육관광부 청년문화패스 가맹 공연/전시 시설 */
    YOUTH_CULTURE_PASS("청년문화패스"),

    /** 지자체 청년 할인 가맹점 (지역별 음식점·카페·쇼핑 등) */
    LOCAL_YOUTH_DISCOUNT("지자체 청년 할인"),

    /** 사용자가 직접 인증·등록한 혜택 장소 */
    USER_VERIFIED("사용자 인증 혜택"),

    /** 공공데이터포털 API 연동으로 자동 수집된 혜택 장소 */
    PUBLIC_API("공공데이터 연동")
}
