dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j:8.3.0")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation(project(":module-common"))
}