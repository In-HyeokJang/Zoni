dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation(project(":module-common"))
}