implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
db 연결, orm 처리
엔티티로 테이블 자동생성
repository로 crud 자동 처리

implementation 'org.springframework.boot:spring-boot-starter-web'
rest api 서버
컨트롤러-리액트 통신용
tomcat 내장 서버

implementation 'com.mysql:mysql-connector-j:8.2.0'
mysql 연결 드라이버

compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
getter, setter 자동생성
@Builder, @NoArgsConstructor 등 코드 간소화 도구


testImplementation 'org.springframework.boot:spring-boot-starter-test'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
테스트용

implementation 'com.github.f4b6a3:ksuid-creator:4.1.0'
ksuid 설정용

implementation 'org.jsoup:jsoup:1.17.2'
크롤링 파싱용

implementation 'org.apache.commons:commons-text:1.12.0'
문자열 처리 유틸