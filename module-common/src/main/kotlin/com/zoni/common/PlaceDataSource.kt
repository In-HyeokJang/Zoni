package com.zoni.common

/**
 * 장소 데이터 출처
 *
 * 장소 정보가 어떤 경로로 수집되었는지 구분한다.
 * javax.sql.DataSource 와의 이름 충돌을 피하기 위해 PlaceDataSource 로 명명.
 */
enum class PlaceDataSource(val displayName: String) {
    /** 카카오맵 API를 통해 검색·저장된 장소 (기본값) */
    KAKAO_MAP("카카오맵"),

    /** 공공데이터포털(data.go.kr) API 연동으로 자동 수집 */
    PUBLIC_DATA("공공데이터포털"),

    /** 사용자가 직접 입력·등록한 장소 */
    USER_INPUT("사용자 직접 입력")
}
