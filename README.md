# MOYEO

가톨릭대학교 구성원을 위한 팀 매칭 및 모집 플랫폼입니다.

MOYEO는 교내 학생들이 프로젝트, 스터디, 공모전 팀원을 쉽게 찾고, 모집글 작성, 팀원 매칭, 리뷰, AI 기능을 통해 더 편리하게 팀 활동을 진행할 수 있도록 만든 웹 서비스입니다.

---

## 프로젝트 소개

MOYEO는 교내 프로젝트, 스터디, 공모전 모집 정보를 한 곳에서 확인할 수 있는 플랫폼입니다.

기존에는 에브리타임, 카카오톡 오픈채팅, 학과 단톡방 등 여러 채널에 흩어져 있던 모집 정보를 하나의 서비스에서 관리할 수 있도록 했습니다.

사용자는 자신의 프로필, 기술 스택, 활동 분야를 등록할 수 있고, 조건에 맞는 팀원을 탐색하거나 모집글에 지원할 수 있습니다.

또한 활동 이후 리뷰를 남길 수 있으며, AI 기능을 통해 팀원 탐색과 매칭을 보조할 수 있도록 구현했습니다.



## 기술 스택

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- OAuth2
- Swagger

### Infra

- AWS EC2
- Docker
  

### Collaboration

- GitHub
- Notion
  

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
