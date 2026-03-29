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
public class DecorationCrawlService {
    private static final String DECORATION_URL = "https://mhf.inven.co.kr/db/mhwilds/accessory/";
    private final ObjectMapper objectMapper;

    public List<RawDecorationDto> crawlDecorations() throws IOException{
        Document doc = Jsoup.connect(DECORATION_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36") //http 헤더 조작. 브라우저에서 접속한 것처럼 요청하는 설정
                .header("Accept-Language", "ko-KR, ko; q=0.9")
                .timeout(15000)
                .get();

        Element main = doc.selectFirst("main#mhwiDbAccessory");
        if (main == null) {throw new IllegalStateException("main#mhwiDbAccessory 요소를 찾지 못했습니다");}

        String rawData = main.attr("data-base-info");
        if (rawData.isBlank()) {throw new IllegalStateException("data-base-info 값이 비어 있습니다.");}

        String decodedJson = StringEscapeUtils.unescapeHtml4(rawData);

        JsonNode root = objectMapper.readTree(decodedJson);
        JsonNode list = root.path("_data_").path("list");
        if(!list.isArray()){throw new IllegalStateException("장식주 리스트가 배열 형태가 아닙니다");}

        List<RawDecorationDto> result = new ArrayList<>();

        //{"code":1,"name":"공격주【1】","category":1,"rare":3,"slot":1,
                //"skills":[{"code":101,"name":"공격","level":1}]},
        //{"code":2,"name":"공격주Ⅱ【2】","category":1,"rare":4,"slot":2,
                //"skills":[{"code":101,"name":"공격","level":2}]},
        //{"code":42,"name":"화염-속변주【3】","category":1,"rare":6,"slot":3,
                //"skills":[{"code":106,"name":"불속성 공격 강화","level":3},{"code":142,"name":"고속 변형","level":1}]},
        for(JsonNode node : list){
            RawDecorationDto decoDto = new RawDecorationDto();
            decoDto.setCode(node.path("code").asInt());
            decoDto.setName(node.path("name").asText());
            decoDto.setCategory(node.path("category").asInt());
            decoDto.setRarity(node.path("rare").asInt());
            decoDto.setSlotLevel(node.path("slot").asInt());


            JsonNode skills = node.path("skills");
            if (!skills.isArray()) {throw new IllegalStateException("skills가 배열이 아닙니다. code=" + decoDto.getCode());}
            decoDto.setEquipSkillList(crawlEquipSkill(skills));

            result.add(decoDto);
        }

        return result;
    }

    private List<RawEquipSkillDto> crawlEquipSkill(JsonNode skills){
        List<RawEquipSkillDto> equipSkillList = new ArrayList<>();
        for(JsonNode node : skills){
            RawEquipSkillDto eSkill = new RawEquipSkillDto();
            eSkill.setCode(node.path("code").asInt());
            eSkill.setName(node.path("name").asText());
            eSkill.setLevel(node.path("level").asInt());
            equipSkillList.add(eSkill);
        }
        return equipSkillList;
    }

}
