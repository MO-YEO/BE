# MOYEO

가톨릭대학교 구성원을 위한 팀 매칭 및 모집 플랫폼입니다.

MOYEO는 교내 학생들이 프로젝트, 스터디, 공모전 팀원을 쉽게 찾고, 모집글 작성, 팀원 탐색, 자유게시판 기능을 통해 더 편리하게 팀 활동을 진행할 수 있도록 만든 웹 서비스입니다.

---

## 프로젝트 소개

MOYEO는 교내 프로젝트, 스터디, 공모전 모집 정보를 한 곳에서 확인할 수 있는 플랫폼입니다.

기존에는 에브리타임, 카카오톡 오픈채팅, 학과 단톡방 등 여러 채널에 흩어져 있던 모집 정보를 하나의 서비스에서 관리할 수 있도록 했습니다.

사용자는 자신의 프로필, 기술 스택, 활동 분야를 등록할 수 있고, 조건에 맞는 팀원을 탐색하거나 모집글에 지원할 수 있습니다.

---

## 기술 스택

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL
* JWT
* OAuth2
* Swagger

### Infra

* AWS EC2
* Docker

### Collaboration

* GitHub
* Notion

---

## 시스템 구조

```text
Client
  |
  | HTTP Request
  v
Spring Boot Server
  |
  | JPA
  v
PostgreSQL
```

---

## 주요 기능

### 인증 / 회원

* Google OAuth 기반 로그인
* JWT 기반 인증 처리
* 가톨릭대학교 이메일 도메인 검증
* 내 프로필 조회 및 수정
* 기술 스택 및 활동 분야 등록 / 수정

### 팀원 찾기

* 팀원 목록 조회
* 기술 스택 기반 팀원 필터링
* 활동 분야 기반 팀원 필터링
* 팀원 상세 프로필 조회

### 모집글

* 프로젝트 / 스터디 / 공모전 모집글 작성
* 모집글 목록 조회
* 모집글 상세 조회
* 모집글 수정 및 삭제
* 모집글 상태 관리

### 자유게시판

* 게시글 작성
* 게시글 목록 조회
* 게시글 상세 조회
* 게시글 수정 및 삭제
* 댓글 작성 및 삭제
* 게시글 좋아요 기능

---

## ERD

프로젝트의 주요 도메인은 회원, 기술 스택, 활동 분야, 모집글, 지원, 자유게시판, 댓글, 좋아요를 중심으로 구성했습니다.

> ERD 이미지 추가 예정

---

## API 문서

Swagger를 통해 API 명세를 확인할 수 있습니다.

```text
http://3.37.55.120.nip.io:8080/swagger-ui/index.html
```

---

## 백엔드 담당 역할

본 프로젝트에서 백엔드 개발을 담당했습니다.

주요 담당 범위는 다음과 같습니다.

* Spring Boot 기반 백엔드 서버 구현
* Google OAuth 로그인 및 JWT 인증 처리
* 학교 이메일 도메인 검증 로직 구현
* 회원 프로필 조회 / 수정 API 구현
* 기술 스택 및 활동 분야 관리 기능 구현
* 팀원 목록 조회 및 필터링 API 구현
* 모집글 관련 API 구현
* 자유게시판 게시글 / 댓글 / 좋아요 API 구현
* Swagger 기반 API 문서화
* PostgreSQL 연동 및 JPA 기반 데이터 처리

---

## 실행 방법

### 1. 프로젝트 클론

```bash
git clone https://github.com/MO-YEO/BE.git
cd BE
```

### 2. 환경 변수 설정

프로젝트 실행을 위해 DB, JWT, OAuth 관련 환경 변수가 필요합니다.

```text
DB_URL=jdbc:postgresql://localhost:5433/moyeo
DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your_jwt_secret

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### 3. Docker로 PostgreSQL 실행

```bash
docker compose up -d
```

### 4. Spring Boot 실행

```bash
./gradlew bootRun
```

또는 IntelliJ에서 `MoyeoApplication`을 실행합니다.

---

## 프로젝트를 진행하며 배운 점

이번 프로젝트를 통해 단순히 API를 만드는 것뿐만 아니라, 인증 흐름, 도메인 설계, 프론트엔드와의 연동 방식까지 함께 고려하는 경험을 할 수 있었습니다.

특히 OAuth 로그인 이후 JWT를 발급하고, 인증된 사용자 정보를 기반으로 프로필, 게시글, 댓글, 좋아요 기능을 처리하는 흐름을 구현하면서 백엔드에서 인증과 권한 처리가 얼마나 중요한지 배웠습니다.

또한 팀 프로젝트를 진행하면서 API 명세를 명확하게 공유하는 것과 프론트엔드에서 필요한 응답 형태를 맞추는 과정이 중요하다는 점을 느꼈습니다.
