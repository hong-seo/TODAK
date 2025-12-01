# TODAK Backend 🩺

병원 진료 예약 및 비대면 진료 서비스 **TODAK** 의 백엔드 서버입니다.  
병원 / 의사 정보 조회, 진료 예약, 녹음 업로드, AI 음성 인식(STT)·요약 등의 기능을 제공합니다.

---

## 🏗 Tech Stack

- Language: **Java17** 
- Framework: **Spring Boot**
- Build: **Gradle** – `build.gradle.kts`
- DB: (예시) **PostgreSQL (Supabase)** 
- Storage: **AWS S3** (녹음 파일 업로드)
- Infra:
  - 내부 **AI 서버** (Whisper STT + 요약 모델)
  - Docker / Dockerfile 로 컨테이너 빌드



---

## 🔌 주요 모듈 구조
src/main/java/com.todak.api
 ├─ appointment      # 진료 날짜·시간 선택 및 예약 후보 조회
 ├─ auth             # 로그인/인증 (추후 확장)
 ├─ common           # 공통 응답, 예외 처리, 유틸
 ├─ config           # Spring Security / CORS / ObjectMapper 등 설정
 ├─ consultation     # 진료(상담) 생성/조회 도메인
 ├─ health           # 서버 헬스 체크, 모니터링 API
 ├─ hospital         # 병원/의사/진료과 조회 도메인
 ├─ infra            # 외부 시스템 연동 (S3, AI 서버 등)
 ├─ recording        # 녹음 업로드 → STT → 요약 관리
 ├─ summary          # STT 결과 요약 
 └─ user             # 사용자 


## 📁 Package Structure

TODAK 백엔드 서버는 도메인 기반으로 패키지를 구성하여,
**병원/진료/예약/녹음(STT)/요약(AI)** 기능을 명확히 분리한 구조로 설계되어 있습니다.

bash
코드 복사
src/main/java/com.todak.api
 ├─ appointment      # 진료 날짜·시간 선택 및 예약 후보 조회
 ├─ auth             # 로그인/인증 (추후 확장)
 ├─ common           # 공통 응답, 예외 처리, 유틸
 ├─ config           # Spring Security / CORS / ObjectMapper 등 설정
 ├─ consultation     # 진료(상담) 생성/조회 도메인
 ├─ health           # 서버 헬스 체크, 모니터링 API
 ├─ hospital         # 병원/의사/진료과 조회 도메인
 ├─ infra            # 외부 시스템 연동 (S3, AI 서버 등)
 ├─ recording        # 녹음 업로드 → STT → 요약 관리
 ├─ summary          # STT 결과 요약 도메인
 └─ user.entity      # 사용자 엔티티
🔍 패키지 상세 설명
### 📂 appointment
진료 가능한 날짜, 시간 슬롯을 조회하는 기능을 담당합니다.

예약 가능한 Slot 조회

특정 병원의 예약 가능한 시간 계산

📂 auth
인증/인가 관련 패키지로,
JWT 인증 또는 소셜 로그인 등 추후 기능 확장을 위해 분리된 구조입니다.

📂 common
프로젝트 전역에서 사용하는 공통 요소들을 모아둔 패키지입니다.

공통 응답 포맷 (ApiResponse<T>)

전역 예외 처리 (GlobalExceptionHandler)

Custom Exception

페이지네이션·유틸 등

📂 config
스프링 애플리케이션 설정을 모아둔 패키지입니다.

CORS 설정

Spring Security 설정 (있다면)

Jackson 설정 (ObjectMapper 커스터마이징)

Swagger / OpenAPI 설정 (있다면)

📂 consultation
사용자의 진료 예약(상담) 을 관리하는 핵심 도메인입니다.

주요 기능

진료 생성 (Consultation 생성 API)

특정 사용자/병원의 예약 조회

Recording 및 Summary 와 연동하여,
“이 진료에 대한 녹음/요약 결과”를 연결

📂 health
애플리케이션 상태 점검 API

/health or /api/health

서버 alive 체크

📂 hospital
병원 관련 전체 기능을 담당하는 도메인입니다.

HospitalController

병원 목록 조회

병원 상세 조회

병원 검색 (이름/진료과 기준)

HospitalService

병원/의사/진료과 조회

DTO 변환 및 가공

사용 엔티티/DTO

Hospital

Doctor

Department / Category

AvailableHours 등

📂 infra
외부 시스템과의 연동을 담당하는 하위 패키지입니다.

infra.s3
S3 파일 업로드/다운로드 처리

S3UploaderService

upload(MultipartFile file): String
→ 파일 업로드 후 S3 key 반환

downloadAsMultipartFileByKey(String key): MultipartFile
→ 기존 파일을 읽어서 AI 서버에 전달 가능한 MultipartFile로 변환
→ STT 호출 시 그대로 AiClient로 넘김

infra.ai
Whisper STT / 요약 모델 서버와 통신하는 클라이언트

AiClient

requestStt(recordingId, consultationId, file)
→ 음성 파일을 AI 서버로 전송해 STT 수행

requestSummary(recordingId, transcript)
→ STT 결과 텍스트를 요약 모델로 전달

사용 프로퍼티

ai.server.base-url

ai.server.internal-key

AI 서버와는 RestTemplate 또는 WebClient로 HTTP 통신 수행.

📂 recording
녹음 파일과 STT 처리 흐름을 담당하는 도메인입니다.

Recording 엔티티

id

consultationId

s3Key

transcript

status (RECORDED, TRANSCRIBED, …)

RecordingController

녹음 시작/종료 API

S3 업로드 처리

STT 요청 API

요약 요청 API

RecordingService

파일 S3 업로드

S3 → MultipartFile 변환

AiClient 호출(STT/요약)

Recording 상태 업데이트

📂 summary
음성 인식(STT) 텍스트를 AI 모델로 요약하는 기능을 담당.

SummaryController / SummaryService

텍스트 요약 요청

요약 결과 DB 저장

진료(Consultation)와 연결

📂 user.entity
사용자 관련 엔티티를 정의하는 패키지입니다.

User

Role

사용자 관련 공통 필드 등

