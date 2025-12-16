---
description: Spring Boot Initial Setup Workflow
---

# Spring Boot 백엔드 프로젝트 워크플로우 (Windsurf IDE)

## 1. 기술 스택 (Tech Stack)

- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.x (최신 안정 버전)
- **Build Tool**: Gradle - Kotlin
- **Packaging**: Jar
- **IDE**: Windsurf
- **Database**: H2 (개발/테스트), PostgreSQL (운영)
- **Cache**: Redis (운영)

## 2. 필수 의존성 (Dependencies)

### Core Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // Development Tools
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
```

## 3. 프로젝트 구조 (Project Structure)

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/backend/
│   │   │   ├── BackendApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── home/
│   │   │   │   │   └── controller/
│   │   │   │   │       └── HomeController.java
│   │   │   │   └── post/
│   │   │   │       ├── controller/
│   │   │   │       │   └── PostController.java
│   │   │   │       ├── service/
│   │   │   │       │   └── PostService.java
│   │   │   │       ├── repository/
│   │   │   │       │   └── PostRepository.java
│   │   │   │       ├── dto/
│   │   │   │       │   ├── PostCreateRequest.java
│   │   │   │       │   ├── PostUpdateRequest.java
│   │   │   │       │   └── PostResponse.java
│   │   │   │       └── entity/
│   │   │   │           └── Post.java
│   │   │   └── global/
│   │   │       ├── exception/
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── response/
│   │   │       │   └── ApiResponse.java
│   │   │       └── initData/
│   │   │           └── BaseInitData.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── application-secret.yml.default
│   └── test/
│       └── java/com/backend/
│           ├── domain/post/
│           │   ├── controller/
│           │   │   └── PostControllerTest.java
│           │   ├── service/
│           │   │   └── PostServiceTest.java
│           │   └── repository/
│           │       └── PostRepositoryTest.java
│           └── BackendApplicationTests.java
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
└── README.md
```

## 4. 핵심 엔티티 및 초기 데이터

### Post 엔티티

```java
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 초기 데이터 설정

```java
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final PostService postService;

    @Autowired
    @Lazy
    private BaseInitData self;

    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();  // 프록시를 통해 호출
        };
    }

    @Transactional
    public void work1() {
        if (postService.count() > 0) return;

        postService.write("제목 1", "내용 1");
        postService.write("제목 2", "내용 2");
        postService.write("제목 3", "내용 3");
    }
}
```

## 5. 환경별 설정 (Configuration)

### 기본 설정 (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: backend
  profiles:
    active: dev
    include: secret
  web:
    resources:
      add-mappings: false
  output:
    ansi:
      enabled: always
  jackson:
    serialization:
      fail-on-empty-beans: false

custom:
  dev:
    cookieDomain: localhost
    frontUrl: "http://${custom.dev.cookieDomain}:3000"
    backUrl: "http://${custom.dev.cookieDomain}:${server.port}"
  prod:
    cookieDomain: app1.blap.kr
    frontUrl: "https://${custom.prod.cookieDomain}"
    backUrl: "https://api.${custom.prod.cookieDomain}"
  site:
    cookieDomain: "${custom.dev.cookieDomain}"
    frontUrl: "${custom.dev.frontUrl}"
    backUrl: "${custom.dev.backUrl}"
    name: APP1
  genFile:
    dirPath: c:/temp/app1_dev
```

### 개발 환경 (application-dev.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:./db_dev;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver
    hikari:
      auto-commit: false
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_batch_fetch_size: 100
        format_sql: true
        highlight_sql: true
        use_sql_comments: true

logging:
  level:
    com.backend: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
    org.hibernate.orm.jdbc.extract: TRACE
    org.springframework.transaction.interceptor: TRACE
```

### 테스트 환경 (application-test.yml)

```yaml
spring:
  data:
    redis:
      port: 26380
  datasource:
    url: jdbc:h2:mem:db_test;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver
    hikari:
      auto-commit: false
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: false
        show_sql: false
  sql:
    init:
      mode: never

logging:
  level:
    com.backend: INFO
    org.springframework.web: DEBUG
    org.hibernate.SQL: WARN
    org.springframework.transaction.interceptor: WARN

custom:
  site:
    cookieDomain: "localhost"
    frontUrl: "http://localhost:3000"
    backUrl: "http://localhost:8080"
    name: APP1_TEST
  genFile:
    dirPath: /tmp/app1_test
```

### 운영 환경 (application-prod.yml)

```yaml
spring:
  data:
    redis:
      host: ${custom.prod.redis.host}
      port: ${custom.prod.redis.port}
      username: ${custom.prod.redis.username}
      password: ${custom.prod.redis.password}
  datasource:
    url: jdbc:postgresql://sangwon7242-db-1.internal:5432/app_prod
    username: postgres
    password: ${custom.prod.db.password}
    driver-class-name: org.postgresql.Driver
    hikari:
      auto-commit: false
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        highlight_sql: false
        use_sql_comments: false

logging:
  level:
    com.backend: INFO
    org.hibernate.SQL: INFO
    org.hibernate.orm.jdbc.bind: INFO
    org.hibernate.orm.jdbc.extract: INFO
    org.springframework.transaction.interceptor: INFO

custom:
  site:
    cookieDomain: "${custom.prod.cookieDomain}"
    frontUrl: "${custom.prod.frontUrl}"
    backUrl: "${custom.prod.backUrl}"
    name: APP1
  genFile:
    dirPath: /gen
```

### 시크릿 설정 (application-secret.yml.default)

```yaml
custom:
  prod:
    redis:
      host: NEED_TO_INPUT
      port: NEED_TO_INPUT
      username: NEED_TO_INPUT
      password: NEED_TO_INPUT
    db:
      password: NEED_TO_INPUT
```

## 6. 코딩 컨벤션 (Coding Standards)

### 의존성 주입

- **생성자 주입** 사용 (`@RequiredArgsConstructor` 활용)
- `@Autowired` 지양

### API 설계 원칙

- **RESTful API**: URL은 명사형, 행위는 HTTP Method로 표현
- **Entity 보호**: Controller에서 Entity 직접 반환 금지, 반드시 DTO 사용
- **공통 응답 포맷**: `ApiResponse<T>` 클래스 활용

### 공통 응답 포맷 예시

```java
@Getter
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
```

## 7. 테스트 전략 (Testing Strategy)

### 테스트 계층별 전략

#### 1. Repository 테스트 (`@DataJpaTest`)

```java
@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("제목으로 게시글 검색")
    void findByTitleContaining() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();
        entityManager.persistAndFlush(post);

        // when
        List<Post> posts = postRepository.findByTitleContaining("테스트");

        // then
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).contains("테스트");
    }
}
```

#### 2. Service 테스트 (`@SpringBootTest`)

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 테스트")
    void createPost() {
        // given
        String title = "테스트 제목";
        String content = "테스트 내용";

        // when
        PostResponse response = postService.write(title, content);

        // then
        assertThat(response.getTitle()).isEqualTo(title);
        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getId()).isNotNull();
    }

    @Test
    @DisplayName("게시글 목록 조회 테스트")
    void getPosts() {
        // given
        postService.write("제목1", "내용1");
        postService.write("제목2", "내용2");

        // when
        List<PostResponse> posts = postService.findAll();

        // then
        assertThat(posts).hasSize(2);
    }
}
```

#### 3. Controller 통합 테스트 (`@WebMvcTest`)

```java
@WebMvcTest(PostController.class)
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("게시글 목록 조회 API 테스트")
    void getPosts() throws Exception {
        // given
        List<PostResponse> mockPosts = Arrays.asList(
                PostResponse.builder().id(1L).title("제목1").content("내용1").build(),
                PostResponse.builder().id(2L).title("제목2").content("내용2").build()
        );
        given(postService.findAll()).willReturn(mockPosts);

        // when & then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title").value("제목1"));
    }
}
```

### 테스트 환경 특징

- **Profile**: `test` 프로파일 자동 활성화
- **Database**: H2 인메모리 데이터베이스 사용
- **격리**: `@Transactional`로 각 테스트 메서드마다 롤백
- **성능**: 빠른 테스트 실행을 위한 최적화된 설정

## 8. 배포 설정 (Deployment)

### Dockerfile (Multi-stage Build)

```dockerfile
# 첫 번째 스테이지: 빌드 스테이지
FROM gradle:jdk-21-and-23-graal-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 설정 파일 복사 및 의존성 다운로드
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN gradle build --no-daemon -x test

# 두 번째 스테이지: 실행 스테이지
FROM ghcr.io/graalvm/jdk-community:23

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```
