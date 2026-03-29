package io.MHWilds.mhwbuilder.crawler.equipment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.MHWilds.mhwbuilder.domain.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArmorCrawlService {
    private static final String ARMOR_URL = "https://mhf.inven.co.kr/db/mhwilds/armor/";
    private final ObjectMapper objectMapper;
    private final SkillRepository skillRepository;

    public List<RawArmorDto> crawlArmors() throws Exception {
        Document doc = Jsoup.connect(ARMOR_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36") //http 헤더 조작. 브라우저에서 접속한 것처럼 요청하는 설정.
                .header("Accept-Language", "ko-KR, ko; q=0.9")
                .timeout(15000)
                .get();

        Set<Integer> skillCodeSet = new HashSet<>(skillRepository.findAllCodes());
        Element main = doc.selectFirst("main#mhwiDbArmor");

        if (main == null) {
            throw new IllegalStateException("main#mhwiDbArmor 요소를 찾지 못했습니다");
        }

        String rawData = main.attr("data-base-info");
        if (rawData.isBlank()) {
            throw new IllegalStateException("data-base-info 값이 비어 있습니다.");
        }

        String decodedJson = StringEscapeUtils.unescapeHtml4(rawData);

        JsonNode root = objectMapper.readTree(decodedJson);
        JsonNode list = root.path("_data_").path("list");

        if (!list.isArray()) {
            throw new IllegalStateException("아머 리스트가 배열 형태가 아닙니다.");
        }

        List<RawArmorDto> result = new ArrayList<>();

        for(JsonNode node : list){
            RawArmorDto armorDto = new RawArmorDto();

            //기본 데이터 입력
            armorDto.setCode(node.path("code").asInt());
            armorDto.setName(node.path("name").asText());
            armorDto.setCategory(node.path("category").asInt());
            armorDto.setCategoryText(node.path("category_text").asText());
            armorDto.setRare(node.path("rare").asInt());
            armorDto.setDefense(node.path("defense").asInt());
            //slot 처리
            JsonNode slot = node.path("slots");
            if(slot.has("value")){

                RawSlotDto slotDto = new RawSlotDto();
                slotDto.setCount(slot.path("count").asInt());

                JsonNode value = slot.path("value");

                List <Integer> valueList = new ArrayList<>();
                for(JsonNode singleValue : value){
                    if(singleValue.isInt()) {
                        valueList.add(singleValue.asInt());
                    }
                }
                slotDto.setValues(valueList);
                armorDto.setSlot(slotDto);
            }
            //elementals 처리
            //jsonnode에 받아서 하나씩 분해하고 각 값을 입력한 뒤 다시 armorDto에 저장
            //"elementals":[{"element_type":"화","value":-1,"negative":true},{"element_type":"수","value":4,"negative":false},{"element_type":"뇌","value":-3,"negative":true},{"element_type":"빙","value":0,"negative":false},{"element_type":"용","value":0,"negative":false}]
            JsonNode elementals = node.path("elementals");
            List<RawElementalDto> rawElemList = new ArrayList<>();
            for(JsonNode elem : elementals){
                RawElementalDto elemDto = new RawElementalDto();
                elemDto.setElementType(elem.path("element_type").asText());
                elemDto.setValue(elem.path("value").asInt());
                rawElemList.add(elemDto);
            }
            armorDto.setElementals(rawElemList);



            //스킬 처리
            //"skill":[{"code":154,"name":"완전 충전","level":2},{"code":165,"name":"귀마개","level":1}]
            JsonNode rawSkills = node.path("skill");
            List<RawEquipSkillDto> rawSkillList = new ArrayList<>();
            List<RawSetSkillDto> setSkillList = new ArrayList<>();

            for (JsonNode rawSkill : rawSkills) {

                int code = rawSkill.path("code").asInt();
                String name = rawSkill.path("name").asText();
                int level = rawSkill.path("level").asInt();

                if (skillCodeSet.contains(code)) {
                    // 일반 스킬
                    RawEquipSkillDto skillDto = new RawEquipSkillDto();
                    skillDto.setCode(code);
                    skillDto.setName(name);
                    skillDto.setLevel(level);
                    rawSkillList.add(skillDto);

                } else {
                    // 시리즈/그룹 스킬
                    RawSetSkillDto setSkill = new RawSetSkillDto();
                    setSkill.setCode(code);
                    setSkill.setName(name);
                    setSkillList.add(setSkill);
                }
            }

            armorDto.setSkills(rawSkillList);
            //시리즈, 그룹스킬 입력
            //"series_skill":[{"code":217,"name":"파의룡의 수호"}],"series_skill2":[],"group_skill":[{"code":253,"name":"주인의 혼"}],
            JsonNode ss1 = node.path("series_skill");
            JsonNode ss2 = node.path("series_skill2");
            JsonNode gs = node.path("group_skill");


            RawSetSkillDto s1 = addSkills(ss1);
            RawSetSkillDto s2 = addSkills(ss2);
            RawSetSkillDto g1 = addSkills(gs);

            if (s1 != null) setSkillList.add(s1);
            if (s2 != null) setSkillList.add(s2);
            if (g1 != null) setSkillList.add(g1);

            armorDto.setSetSkills(setSkillList);

            result.add(armorDto);
        }

        return result;
    }


    private RawSetSkillDto addSkills(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray() || arrayNode.isEmpty()) {
            return null;
        }
        JsonNode node = arrayNode.get(0);

        RawSetSkillDto skillDto = new RawSetSkillDto();
        skillDto.setCode(node.path("code").asInt());
        skillDto.setName(node.path("name").asText());

        return skillDto;
    }




}
