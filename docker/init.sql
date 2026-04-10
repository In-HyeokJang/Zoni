-- ZONI 청년 라이프 통합 플랫폼 — DB 초기화 스크립트
-- docker-compose 최초 실행 시 자동으로 실행됨

CREATE DATABASE IF NOT EXISTS zoni_user   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS zoni_feed   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS zoni_place  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS zoni_chat   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS zoni_notify CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS zoni_ai     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
