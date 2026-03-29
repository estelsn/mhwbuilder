package io.MHWilds.mhwbuilder.crawler.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.MHWilds.mhwbuilder.crawler.skill.dto.RawSeriesSkillDto;
import io.MHWilds.mhwbuilder.crawler.skill.dto.RawSkillDto;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class SkillCrawlService {
    private static final String SKILL_URL = "https://mhf.inven.co.kr/db/mhwilds/skill/";
    private final ObjectMapper objectMapper;

    public SkillCrawlService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<RawSkillDto> crawlSkills() throws Exception {
        Document doc = Jsoup.connect(SKILL_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36") //http 헤더 조작. 브라우저에서 접속한 것처럼 요청하는 설정
                .header("accept-Language", "ko-KR, ko; q=0.9")
                .timeout(15000)
                .get();


        System.out.println("문서 title: " + doc.title());


        Element main = doc.selectFirst("main#mhwiDbSkill");
        System.out.println("문서 title: " + doc.title());
        if (main == null) {
            throw new IllegalStateException("main#mhwiDbSkill 요소를 찾지 못했습니다.");
        }

        String rawData = main.attr("data-base-info");

        System.out.println("rawData 길이: " + rawData.length());
        System.out.println("rawData 앞부분: " + rawData.substring(0, Math.min(300, rawData.length())));

        if (!main.hasAttr("data-base-info") || rawData.isBlank()) {
            throw new IllegalStateException("data-base-info 값이 비어 있습니다.");
        }

        String decodedJson = StringEscapeUtils.unescapeHtml4(rawData);
        System.out.println("decodedJson 앞부분: " + decodedJson.substring(0, Math.min(300, decodedJson.length())));

        JsonNode root = objectMapper.readTree(decodedJson);
        System.out.println("root 필드들: " + root.fieldNames().next());

        JsonNode list = root.path("_data_").path("list");

        System.out.println("=== root field names ===");
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            System.out.println(fieldName);
        }
        JsonNode dataNode = root.path("_data_");

        System.out.println("=== _data_ field names ===");
        Iterator<String> dataFields = dataNode.fieldNames();
        while (dataFields.hasNext()) {
            String field = dataFields.next();
            System.out.println(field);
        }


        if (!list.isArray()) {
            throw new IllegalStateException("스킬 리스트가 배열 형태가 아닙니다.");
        }

        List<RawSkillDto> result = new ArrayList<>();

        for (JsonNode node : list) {
            RawSkillDto dto = new RawSkillDto();
            dto.setCode(node.path("code").asInt());
            dto.setName(node.path("name").asText());
            dto.setCategory(node.path("category").asInt());
            dto.setCategoryText(node.path("category_text").asText());
            dto.setSlotSize(node.path("slotsize").asInt());
            dto.setMaxLevel(node.path("max_level").asInt());
            dto.setDescription(node.path("description").asText());

            if (dto.getName() == null || dto.getName().isBlank()) {
                System.out.println("이상 데이터 발견: code=" + dto.getCode());
            }

            List<String> levels = new ArrayList<>();
            JsonNode levelsNode = node.path("levels");
            if (levelsNode.isArray()) {
                for (JsonNode lv : levelsNode) {
                    levels.add(lv.asText());
                }
            }
            dto.setLevels(levels);

            JsonNode ss1 = node.path("seriesskill1");
            if (!ss1.isMissingNode() && !ss1.isNull()) {
                RawSeriesSkillDto series1 = new RawSeriesSkillDto();
                series1.setCount(ss1.path("count").asInt());
                series1.setName(ss1.path("name").asText());
                series1.setDescription(ss1.path("desc").asText());
                dto.setSeriesSkill1(series1);
            }

            // seriesSkill2 처리
            JsonNode ss2 = node.path("seriesskill2");
            if (!ss2.isMissingNode() && !ss2.isNull()) {
                RawSeriesSkillDto series2 = new RawSeriesSkillDto();
                series2.setCount(ss2.path("count").asInt());
                series2.setName(ss2.path("name").asText());
                series2.setDescription(ss2.path("desc").asText());
                dto.setSeriesSkill2(series2);
            }
            result.add(dto);

        }

        return result;
    }
}