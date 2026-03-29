

# Recommendations API (세팅 추천)

---
## 1. 추천 조합 목록 조회

- **Endpoint**: `/recommendations`
- **Method**: POST
- **Description**: 사용자가 선택한 스킬이나 조건에 맞는 추천 빌드 목록을 조회
  각 빌드의 조합 가능 여부(`available`)도 포함되며, 불가능한 조합이면 클라이언트에서 알림을 표시

### Notes
- 추천 점수(`matchScore`)는 선택한 스킬과 빌드 적합도를 기반으로 계산
- 페이징(`page`, `limit`)과 정렬(`sortBy`) 옵션을 지원.
- 클라이언트는 추천 목록을 출력 후, 상세보기와 저장 기능을 제공.

---

## 2. 추천 조합 상세보기

- **Endpoint**: `/recommendations?select={recommendationId}`
- **Method**: GET
- **Description**: 특정 추천 빌드의 상세 정보를 조회

### Notes
- 상세보기에서는 추천 빌드의 스킬, 장비 구성을 확인
- 저장 요청 전 빌드 확인용
- 로그인 상태에 따라 일부 기능 제한
- 클라이언트는 상세 정보 확인 후, 저장 버튼을 통해 빌드를 저장

---

## 3. 추천 조합 저장 (클라이언트 임시 저장 + 로그인 페이지 이동)

- **Endpoint**: `/recommendations/save`
- **Method**: POST
- **Description**: 로그인한 사용자는 추천 결과를 자신의 새 빌드로 저장합니다.  
  로그인하지 않은 사용자는 **클라이언트(localStorage 등)에 임시 저장**한 뒤, 로그인 페이지로 이동하도록 안내합니다.

### Notes
- 추천 ID(`recommendationId`)를 기반으로 기존 추천 결과를 참조합니다.
- 로그인 상태 확인:
  - 로그인되어 있으면 **정식 빌드로 저장**.
  - 로그인되어 있지 않으면:
    1. 세팅을 **클라이언트 임시 저장** (localStorage 등)  
    2. 클라이언트에서 **로그인 페이지로 이동**  
    3. 사용자가 로그인 완료 후, 임시 저장 데이터를 정식 빌드로 서버 전송 가능
- **저장 후 바로 내 세팅 상세보기로 이동 가능**  
  - 내 세팅 상세보기 엔드포인트: `/users/{userId}/builds/{buildId}`  
  - `buildId`는 `/recommendations/save` 저장 응답에서 발급된 ID 사용.
- 잘못된 요청(400)이나 서버 오류(500)도 처리 필요.
- 임시 저장 방식이므로 서버에서는 별도 임시 테이블이 필요하지 않습니다.
- 클라이언트는 저장 버튼 클릭 시, 로그인 상태를 먼저 확인 후 처리 로직을 구현해야 합니다.


# Builds API (세팅 시뮬레이터)

---

## 1. 시뮬레이터 장비/장식주 목록 조회

- **Endpoint**: `/simulators`
- **Method**: GET
- **Description**: 시뮬레이터에서 선택 가능한 장비와 장식주 전체 목록을 조회합니다.

### Notes
- 서버에서 최신 장비/장식 데이터를 제공
- 클라이언트에서 한 번 로드 후 **로컬 상태에 저장**하여 실시간 선택/변경 가능
- 페이징, 필터, 정렬 옵션 필요 시 추가 가능
- 각 항목에는 장비/장식 고유 ID, 슬롯, 스킬, 스탯 정보 포함

---

## 2. 시뮬레이터 장비/장식 상세 조회 (선택)

- **Endpoint**: `/simulators/items/{itemId}`
- **Method**: GET
- **Description**: 특정 장비 또는 장식주의 상세 정보를 조회합니다.

### Notes
- 장비 툴팁, 스킬 설명, 슬롯 상세 정보 제공
- 클라이언트에서 상세 정보 필요 시 호출
- 서버 요청 최소화 위해 필요 없는 경우 생략 가능

---

## 3. 세팅 저장

- **Endpoint**: `/simulators/save`
- **Method**: POST
- **Description**: 사용자가 시뮬레이터에서 만든 세팅을 저장합니다.

### Notes
- 로그인 상태 확인:
  - 로그인 O → 서버에 저장 → 내 세팅 상세보기
  - 로그인 X → **클라이언트 임시 저장 후 로그인 페이지 이동**  
    - 로그인 완료 시, 임시 저장 데이터를 서버로 전송 가능
- 회원가입이 안 되어 있는 경우:
  - 로그인 페이지에서 회원가입으로 넘어가는 순간 **임시 저장 데이터 삭제**
  - 이유: 회원 정보 없이는 서버 저장 불가
- 저장 성공 시, 응답으로 `buildId` 반환 → 내 세팅 상세보기(`/users/{userId}/builds/{buildId}`)로 바로 이동
- 잘못된 요청(400)이나 서버 오류(500) 처리 필요
- 클라이언트에서 저장 버튼 클릭 시, 로그인/회원가입 상태 확인 후 처리


# Notices API (공지사항)

---

## 1. 공지사항 목록 조회

- **Endpoint**: `/notices`
- **Method**: GET
- **Description**: 전체 공지사항 목록을 조회합니다.

### Notes
- 페이징(`page`, `limit`) 및 정렬(`sortBy`) 옵션 지원 가능
- 클라이언트에서 목록 클릭 시 상세보기(`/notices/{noticeId}`)로 이동
- 잘못된 요청 시 400 반환, 서버 오류 시 500 반환

---

## 2. 공지사항 상세보기

- **Endpoint**: `/notices/{noticeId}`
- **Method**: GET
- **Description**: 특정 공지사항의 상세 내용을 조회합니다.

### Notes
- 일반 사용자용 조회 API로, **수정/삭제/등록 기능 없음**
- 잘못된 요청(잘못된 ID 등) 시 404 반환
- 서버 오류 시 500 반환


# Admin API (관리자 페이지)

---

## 1. 공지사항 관리

### 1-1. 공지사항 등록

- **Endpoint**: `/admin/notices`
- **Method**: POST
- **Description**: 새로운 공지사항 등록

### Notes
- 관리자 권한 필요
- 제목, 내용 필수
- 잘못된 요청 시 400, 서버 오류 시 500 반환

### 1-2. 공지사항 상세보기

- **Endpoint**: `/admin/notices/{noticeId}`
- **Method**: PATCH
- **Description**: 기존 공지사항 일부 필드 수정

### Notes
- 관리자 권한 필요
- 잘못된 요청(없는 ID 등) 시 404 반환

### 1-3. 공지사항 삭제

- **Endpoint**: `/admin/notices/{noticeId}`
- **Method**: DELETE
- **Description**: 공지사항 삭제

### Notes
- 관리자 권한 필요
- 잘못된 요청(없는 ID 등) 시 404 반환

---

## 2. 장비/장식 데이터 관리

### 2-1. 장비/장식 등록

- **Endpoint**: `/admin/items`
- **Method**: POST
- **Description**: 신규 장비 또는 장식 등록

### Notes
- 관리자 권한 필요

### 2-2. 장비/장식 수정

- **Endpoint**: `/admin/items/{itemId}`
- **Method**: PATCH
- **Description**: 기존 장비/장식 일부 필드 수정

### Notes
- 관리자 권한 필요

### 2-3. 장비/장식 삭제

- **Endpoint**: `/admin/items/{itemId}`
- **Method**: DELETE
- **Description**: 장비/장식 삭제

### Notes
- 관리자 권한 필요

---

## 3. TaskLog 관리

### 3-1. TaskLog 조회

- **Endpoint**: `/admin/tasklogs`
- **Method**: GET
- **Description**: 작업 로그 조회

### Notes
- 관리자 권한 필요
- 페이징, 검색, 필터링 가능
- 등록/수정/삭제 없음

---

## 4. 회원 관리

### 4-1. 회원 조회

- **Endpoint**: `/admin/users`
- **Method**: GET
- **Description**: 회원 목록 조회

### Notes
- 관리자 권한 필요
- 페이징, 검색, 필터링 가능

### 4-2. 회원 제재/차단/부관리자 임명

- **Endpoint**: `/admin/users/{userId}`
- **Method**: PATCH
- **Description**: 회원 제재, 차단, 부관리자 권한 부여

### Notes
- 관리자 권한 필요
- 로직에서 제재와 연동하여 필요 시 게시물 삭제 처리 가능

### 4-3. 회원 탈퇴/삭제

- **Endpoint**: `/admin/users/{userId}`
- **Method**: DELETE
- **Description**: 회원 탈퇴 처리 또는 삭제

### Notes
- 관리자 권한 필요

---

## 5. 게시물 관리

### 5-1. 게시물 조회

- **Endpoint**: `/admin/posts`
- **Method**: GET
- **Description**: 이상 게시물 조회

### Notes
- 관리자 권한 필요
- 페이징, 검색, 필터링 가능

### 5-2. 게시물 삭제

- **Endpoint**: `/admin/posts/{postId}`
- **Method**: DELETE
- **Description**: 게시물 삭제 (제재와 연동 가능)

### Notes
- 관리자 권한 필요
- 삭제 시 제재 기록과 함께 이유 입력 가능 → 로직에서 처리

---

## 6. 건의사항 관리

### 6-1. 건의사항 조회

- **Endpoint**: `/admin/suggestions`
- **Method**: GET
- **Description**: 사용자 건의사항 조회

### Notes
- 관리자 권한 필요
- 페이징, 검색, 필터링 가능

### 6-2. 건의사항 처리 상태 변경

- **Endpoint**: `/admin/suggestions/{suggestionId}`
- **Method**: PATCH
- **Description**: 건의사항 처리 상태 업데이트

### Notes
- 관리자 권한 필요


