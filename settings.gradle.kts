rootProject.name = "zoni"

// 공통 모듈
include("module-common")

// 마이크로서비스 6개
include(
    "service-user",
    "service-place",
    "service-feed",
    "service-ai",
    "service-chat",
    "service-notify"
)