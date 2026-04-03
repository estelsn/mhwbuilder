# Backend

몬스터 헌터 와일즈 세팅 추천 시스템의 백엔드입니다.

루트 README에서 설명된 추천 엔진 로직을 실제로 수행하며,  
스킬 데이터 제공 및 세팅 추천 API를 담당합니다.

---

## 역할

- 스킬 / 장비 데이터 조회 API 제공
- 추천 요청 처리 (세트 스킬 + 장식주 검증)
- 세팅 결과 계산 및 반환

---

## 기술 스택

- Java 21
- Spring Boot
- Spring Data JPA
- H2 Database
- Jsoup
- Jackson

---

## 실행 방법

```bash
./gradlew bootRun