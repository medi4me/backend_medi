# mediforme - Backend

🥕 의약품 정보 챗봇·검색 서비스의 Spring Boot 서버입니다.

사용자가 약을 검색·촬영해 정보를 확인하고, 챗봇으로 복약 관련 질의응답, 복약 맞춤 관리 서비스 메디포미의 백엔드입니다.
식약처·openFDA 등 공공 라벨 데이터를 기반으로 하며, 챗봇은 OpenAI 위에 RAG를 얹어 환자 안전 관점의 답변 품질을 높였습니다.

## 주요 기능

- **사용자 인증** — JWT 발급·재발급(Redis 토큰 저장), SMS 본인 인증(CoolSMS)
- **의약품 검색** — 식약처(e약은요)·openFDA·RxNorm 다중 소스 통합 검색
- **의약품 카메라 인식** — Google Cloud Vision OCR 로 약 이미지에서 정보 추출
- **약물 상호작용 안내** — 함께 복용 시 주의 정보 제공
- **챗봇 Q&A** — OpenAI 기반 질의응답에 RAG 라벨 컨텍스트를 주입(검색 실패 시 약 메타데이터 앵커링으로 폴백)
- **운영 관측** — Actuator + Micrometer(Prometheus) 헬스·메트릭

## 기술 스택

- **언어·프레임워크** — Java 17, Spring Boot 3.3.2 (Web, Security, Data JPA, Validation)
- **데이터** — MySQL 8 (JPA/Hibernate), Redis
- **빌드** — Gradle 멀티모듈
- **문서** — OpenAPI(Swagger) 어노테이션 기반 API 문서
- **외부 연동** — OpenAI, Google Cloud Vision, 식약처 e약은요, openFDA, RxNorm, CoolSMS, RAG 검색 서비스([mediforme-chatbot-rag](https://github.com/medi4me/mediforme-chatbot-rag))

## 모듈 구조

| 모듈 | 설명 |
|---|---|
| `app` | 부트 엔트리(`MediformeApplication`), 설정·프로파일 병합 |
| `api` | 웹 컨트롤러 + 도메인 로직 |
| `common` | 공통 응답·예외 등 |
| `lib:redis` | Redis 설정 모듈 |

`api` 도메인 패키지: `user`(인증) · `medicine`(검색·OCR·상호작용) · `search` · `chatbot`(RAG) · `admin` · `status` · `check` · `global`(보안·공통 설정)

## 디렉터리

| 경로 | 설명 |
|---|---|
| `database/` | 스키마·시드 SQL |
| `http/` | IntelliJ HTTP Client 요청 파일(수동 API 테스트) |
| `eval/chatbot-rag/` | 챗봇 RAG 평가 하네스(Python) — Version A/B/C 비교 측정 |

## 실행

사전 준비: MySQL 8, Redis, 아래 환경변수

```bash
./gradlew :app:bootRun
```

기본 활성 프로파일은 `local, redis` 입니다(`local` / `dev` / `prod` 중 선택). 테스트는 `./gradlew test`.

### 환경변수

| 변수 | 용도 |
|---|---|
| `DB_LOCAL_URL` / `DB_LOCAL_USERNAME` / `DB_LOCAL_PASSWORD` | MySQL 접속 (dev·prod 는 `DB_DEV_*` / `DB_PROD_*`) |
| `REDIS_HOST` / `REDIS_PORT` | Redis 접속 |
| `JWT_SECRET_KEY` / `JWT_ACCESS_EXPIRATION` / `JWT_REFRESH_EXPIRATION` | JWT 서명·만료 |
| `MFDS_API_KEY` | 식약처 e약은요 API 키 |
| `GOOGLE_APPLICATION_CREDENTIALS` | Google Vision 서비스 계정 키 경로 |
| `COOLSMS_API_KEY` / `COOLSMS_API_SECRET` / `COOLSMS_API_SENDER` | SMS 인증 |
| `RAG_SERVICE_URL` | RAG 검색 서비스 주소 (기본 `http://localhost:8000`) |

## 챗봇 RAG

챗봇은 사용자 질문과 약 식별자를 RAG 검색 서비스([mediforme-chatbot-rag](https://github.com/medi4me/mediforme-chatbot-rag), FastAPI + BGE-M3 + FAISS)의 `/retrieve` 로 보내, 실제 의약품 라벨 청크를 컨텍스트로 받아 답변을 생성합니다. 검색이 비거나 서비스 장애 시에는 약 메타데이터 앵커링으로 폴백하므로 기존 동작 이하로 떨어지지 않습니다.

도입 근거는 동일 골든셋·judge 로 측정한 A/B/C 비교에 있습니다 — hallucination 65.9%(A 베이스라인) → 59.8%(B 앵커링) → 52.4%(C RAG), 특히 어려운 질문에서 정확도가 크게 개선됩니다. 자세한 내용은 [`eval/chatbot-rag/reports/2026-05-21-A-vs-B-vs-C.md`](eval/chatbot-rag/reports/2026-05-21-A-vs-B-vs-C.md) 를 참고하세요.
