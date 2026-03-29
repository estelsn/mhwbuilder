package io.MHWilds.mhwbuilder.crawler.equipment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CharmCrawlService {
    private static final String CHARM_URL = "https://mhf.inven.co.kr/db/mhwilds/charms/";
    private final ObjectMapper objectMapper;

    public List<RawCharmDto> crawlCharms() throws IOException {
        Document doc = Jsoup.connect(CHARM_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36") //http 헤더 조작. 브라우저에서 접속한 것처럼 요청하는 설정
                .header("Accept-Language", "ko-KR, ko; q=0.9")
                .timeout(15000)
                .get();

        Element main = doc.selectFirst("main#mhwiDbCharms");
        if (main == null){throw new IllegalStateException("main#mhwiDbCharms 요소를 찾지 못했습니다" );}

        String rawData = main.attr("data-base-info");
        if(rawData.isBlank()){throw new IllegalStateException("data-base-info 값이 비어있습니다");}

        String decodedJson = StringEscapeUtils.unescapeHtml4(rawData);

        JsonNode root = objectMapper.readTree(decodedJson);
        JsonNode list = root.path("_data_").path("list");

        if(!list.isArray()){
            throw new IllegalStateException("Charm 리스트가 배열 형태가 아닙니다");
        }

        List<RawCharmDto> result = new ArrayList<>();
        //데이터 입력
        //{"code":4,"name":"체술의 호석Ⅰ","max_level":1,
        // "skills":[{"code":124,"name":"체술","level":1}],
        for(JsonNode node : list){
            RawCharmDto rawCharm = new RawCharmDto();
            //단순 데이터
            rawCharm.setCode(node.path("code").asInt());
            rawCharm.setName(node.path("name").asText());
            rawCharm.setMaxLevel(node.path("max_level").asInt());
            //스킬 연동
            JsonNode rawSkills = node.path("skills");
            if(!rawSkills.isArray()){
                throw new IllegalStateException("호석 스킬리스트가 배열 형태가 아닙니다");
            }
            for(JsonNode rawSkill : rawSkills){
                RawEquipSkillDto reSkill = new RawEquipSkillDto();
                reSkill.setCode(rawSkill.path("code").asInt());
                reSkill.setName(rawSkill.path("name").asText());
                reSkill.setLevel(rawSkill.path("level").asInt());
                rawCharm.setEquipSkill(reSkill);
            }
            result.add(rawCharm);
        }

        return result;
    }
}
