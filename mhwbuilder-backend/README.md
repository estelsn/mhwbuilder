Monster Hunter Wilds Set Builder

## 1. 프로젝트 개요
몬스터헌터 와일즈에서 원하는 스킬 기반으로 장비 세팅을 추천하고,
사용자가 직접 세팅을 구성 및 저장할 수 있는 프로그램입니다.

## 2. 개발 배경
기존 게임 플레이 중 세팅 관리의 불편함을 해결하기 위해 시작했으며,
초기에는 대규모 프로젝트로 설계했으나 MVP 형태로 방향을 재설정했습니다.

## 3. 기술 스택
- Backend: Java, Spring Boot, JPA
- Frontend: React (Vite)
- Database: H2 (MVP), MySQL (확장 고려)
- 기타: Jsoup (크롤링), KSUID (ID 생성)

## 4. 주요 기능
- 스킬 기반 장비 추천
- 세팅 빌더 (수동 구성)
- 세팅 저장 및 관리
- 데이터 크롤링 및 업데이트

## 5. 구조 설계 특징
- 1:N 관계 중심 설계 (정규화 적용)
- KSUID 기반 ID 생성
- DB 교체 가능 구조 (MySQL → H2 → 확장 고려)

## 6. 실행 방법
```bash
./gradlew bootRun