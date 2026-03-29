package io.MHWilds.mhwbuilder.crawler.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service //@service : 컨트롤러에서 생성자를 통해 이 클래스를 주입받아 사용
public class SkillCrawlPreviewService { //외부 페이지 접속, html 분석, json 추출, dto 리스트 생성

    //Jackon 라이브러리에서 json을 json node로 파싱해 바꿔주는 용도
    private final ObjectMapper objectMapper = new ObjectMapper();
    //url을 받아서 스킬 목록을 찾아 rawSkillDto 리스트로 반환
    public List<RawSkillPreviewDto> previewSkillNames(String url) throws Exception {
        //url 접속, 설정, 다운로드, 파싱
        Document doc = Jsoup.connect(url) //url 접속 준비(준비만)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36") //http 헤더 조작. 브라우저에서 접속한 것처럼 요청한다는 알림.
                .header("accept-Language", "ko-KR, ko; q=0.9") //한국어(대한민국) 우선. 한국어도 괜찮지만 우선순위 밀림 q=1이 가장 선호
                .timeout(15000) //15초 대기
                .get(); //받은 html을 객체 doc에 저장
       //<main id="mhwiDbSkill" data-base-info="..."> 인 html이 있는지 체크
        Element main = doc.selectFirst("main#mhwiDbSkill");
        if (main == null) {
            throw new IllegalStateException("main#mhwiDbSkill 요소를 찾지 못했습니다.");
        }
        //속성 data-base-info 값을 rawdata로 꺼내기
        String rawData = main.attr("data-base-info");
        if (rawData.isBlank()) { //공백만 있으면
            throw new IllegalStateException("data-base-info 값이 비어 있습니다.");
        }
        //json을 escape화(특수문자가 잘 안들어가므로 html에 맞게 변경. "->&quot, '->&#39;, &->&amp;)했을 수 있으므로 재변환해서 문자열에 입력
        String decodedJson = StringEscapeUtils.unescapeHtml4(rawData);
    //{"code":131,"name":"KO술","category":1,"category_text":"공격","slotsize":0,"max_level":3,"description":"몬스터를 기절 상태로 만들 확률이 높아진다.","levels":["기절 위력 1.2배","기절 위력 1.3배","기절 위력 1.4배"],"seriesskill1":null,"seriesskill2":null,"comment_count":1}

        //디버깅.  json이 2000자 이상이면 2000자까지 출력
        System.out.println("===== DECODED JSON START =====");
        System.out.println(decodedJson.substring(880)  );
        System.out.println("===== DECODED JSON END =====");

        //jsonnode: json 데이터를 트리 구조로 표현한 객체
        //readTree(...)는 문자열 JSON을 JsonNode로 바꿔줌
        JsonNode root = objectMapper.readTree(decodedJson);
        JsonNode skillList = root.path("_data_").path("list");//get이 아니라 path를 쓰면 null 대신 빈노드 반환해서 안전한 연결 가능

        //구조 검증
        if (!skillList.isArray()) {
            throw new IllegalStateException("_data_.list가 배열 형태가 아닙니다.");
        }

        //반환 리스트 생성
        List<RawSkillPreviewDto> result = new ArrayList<>();

        for (JsonNode skillNode : skillList) {
            int code = skillNode.path("code").asInt(); //code 필드를 int로 변경해서 출력
            String name = skillNode.path("name").asText("").trim(); //name 필드를 가져옴

            //비었을 경우 무시
            if (name.isEmpty()) {
                continue;
            }
            //출처 출력
            String href = "/db/mhwilds/skill/" + code;
            result.add(new RawSkillPreviewDto(name, code, href));
        }
        //반환 리스트 크기
        System.out.println("skill count = " + result.size());

        for (int i = 0; i < Math.min(result.size(), 5); i++) {
            RawSkillPreviewDto skill = result.get(i);
            System.out.println(skill);
        }

        return result;
    }
}