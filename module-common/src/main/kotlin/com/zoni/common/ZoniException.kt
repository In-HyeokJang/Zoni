package com.zoni.common

/**
 * ZONI 프로젝트 공통 비즈니스 예외
 * 사용 예: throw ZoniException(ErrorCode.DUPLICATE_EMAIL)
 */
class ZoniException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
