package com.zoni.common

class ZoniException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message
) : RuntimeException(message)