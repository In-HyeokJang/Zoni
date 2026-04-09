-- ZONI 프로젝트 초기 DB 생성 스크립트
-- docker-compose 최초 실행 시 자동으로 실행됨

CREATE DATABASE IF NOT EXISTS zoni_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS zoni_feed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 추후 서비스 추가 시 여기에 DB 추가
-- CREATE DATABASE IF NOT EXISTS zoni_place CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- CREATE DATABASE IF NOT EXISTS zoni_chat  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

