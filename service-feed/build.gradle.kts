plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    // Web (Spring MVC) - service-user와 동일한 스택
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Validation (@Valid, @NotBlank 등)
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JPA + MySQL
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j:8.3.0")

    // Security (JWT 인증)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Redis (access token 블랙리스트 확인)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // 공통 모듈 (ApiResponse, ErrorCode, ZoniException, JwtPrincipal)
    implementation(project(":module-common"))
}