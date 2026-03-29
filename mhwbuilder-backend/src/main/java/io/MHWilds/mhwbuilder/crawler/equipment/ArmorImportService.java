package io.MHWilds.mhwbuilder.crawler.equipment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawArmorDto;
import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawElementalDto;
import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawEquipSkillDto;
import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawSetSkillDto;
import io.MHWilds.mhwbuilder.domain.equipment.entity.Armor;
import io.MHWilds.mhwbuilder.domain.equipment.repository.ArmorRepository;
import io.MHWilds.mhwbuilder.domain.skill.entity.EquipSeriesSkill;
import io.MHWilds.mhwbuilder.domain.skill.entity.EquipSkill;
import io.MHWilds.mhwbuilder.domain.skill.entity.SeriesSkill;
import io.MHWilds.mhwbuilder.domain.skill.entity.Skill;
import io.MHWilds.mhwbuilder.domain.skill.repository.EquipSeriesSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.EquipSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SeriesSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SkillRepository;
import io.MHWilds.mhwbuilder.util.entityenums.EquipCategory;
import io.MHWilds.mhwbuilder.util.entityenums.EquipType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArmorImportService {

    private final ArmorCrawlService armorCrawlService;
    private final ArmorRepository armorRepository;
    private final SkillRepository skillRepository;
    private final SeriesSkillRepository seriesSkillRepository;
    private final EquipSkillRepository equipSkillRepository;
    private final EquipSeriesSkillRepository equipSeriesSkillRepository;
    private final ObjectMapper objectMapper;



    @Transactional
    public int importArmor() throws Exception{
        int count = 0;
        List<EquipSkill> eSkillList = new ArrayList<>();
        List<EquipSeriesSkill> esSkillList = new ArrayList<>();

        List<RawArmorDto> rawArmors = armorCrawlService.crawlArmors();

        List<Skill> allSkill = skillRepository.findAll();
        Map<Integer, Skill> skillMap = allSkill.stream()
                .collect(Collectors.toMap(Skill::getCode, Function.identity()));

        List<SeriesSkill> allSeriesSkill = seriesSkillRepository.findAll();
        Map<Integer, SeriesSkill> seriesSkillMap = allSeriesSkill.stream()
                .collect(Collectors.toMap(SeriesSkill::getCode, Function.identity()));

        Set<Integer> existingCodes = armorRepository.findAll().stream()
                .map(Armor::getCode)
                .collect(Collectors.toSet());

        for (RawArmorDto rawArmor : rawArmors){
            if(existingCodes.contains(rawArmor.getCode())){
                continue;
            }
            Armor savedArmor = saveArmor(rawArmor);
            saveEquipSkill(savedArmor, rawArmor, skillMap, eSkillList);
            saveEquipSeriesSkill(savedArmor, rawArmor,seriesSkillMap, esSkillList);
            count++;
        }

        equipSkillRepository.saveAll(eSkillList);
        equipSeriesSkillRepository.saveAll(esSkillList);

        return count;
    }

    private Armor saveArmor(RawArmorDto rawArmor){ //방어구 데이터 저장

        //id, code, name, category, rarity, slot1 2 3, def, elem, ver
        Armor armor = new Armor();
        armor.setCode(rawArmor.getCode());
        armor.setName(rawArmor.getName());
        armor.setCategory(EquipCategory.fromEquipCode(rawArmor.getCategory()));
        armor.setRarity(rawArmor.getRare());

        List<Integer> values = rawArmor.getSlot() != null
                ? rawArmor.getSlot().getValues()
                : Collections.emptyList();

        int slot1 = 0, slot2 = 0, slot3 = 0;
        if (values.size() > 0) slot1 = values.get(0);
        if (values.size() > 1) slot2 = values.get(1);
        if (values.size() > 2) slot3 = values.get(2);


        armor.setSlot1Lv(slot1);
        armor.setSlot2Lv(slot2);
        armor.setSlot3Lv(slot3);

        armor.setDefense(rawArmor.getDefense());


        List<RawElementalDto> elementals =
                rawArmor.getElementals() != null
                        ? rawArmor.getElementals()
                        : Collections.emptyList(); //수정 안되는 빈리스트. 읽기 전용

        armor.setElementals(toJson(elementals));
        armor.setVersion("v1");

        return armorRepository.save(armor);
    }
    private void saveEquipSkill(Armor savedArmor, RawArmorDto rawArmor, Map<Integer, Skill> skillMap,  List<EquipSkill> eSkillList){
        List<RawEquipSkillDto> skills = rawArmor.getSkills() != null //skills nullpointException 검증
                ? rawArmor.getSkills()
                : Collections.emptyList();

        for(RawEquipSkillDto rawSkill : skills){

            Skill skill = skillMap.get(rawSkill.getCode());
            if (skill == null) {
                throw new RuntimeException("Skill이 DB에서 검색되지 않음: " + rawSkill.getCode());
            }
            EquipSkill equipSkill = new EquipSkill();
            equipSkill.setEquipId(savedArmor.getId());
            equipSkill.setEquipType(EquipType.ARMOR);
            equipSkill.setSkill(skill);
            equipSkill.setSkillLevel(rawSkill.getLevel());

            eSkillList.add(equipSkill);
        }
    }

    private void saveEquipSeriesSkill(Armor savedArmor, RawArmorDto rawArmor, Map<Integer, SeriesSkill> seriesSkillMap, List<EquipSeriesSkill> esSkillList){
        List<RawSetSkillDto> setSkills = rawArmor.getSetSkills() != null
                ? rawArmor.getSetSkills()
                : Collections.emptyList();

        for(RawSetSkillDto rawSetSkill : setSkills){

            SeriesSkill sSkill = seriesSkillMap.get(rawSetSkill.getCode());
            if(sSkill == null){
                throw new RuntimeException("SeriesSkill 이 DB에서 검색되지 않음: " + rawSetSkill.getCode());
            }
            EquipSeriesSkill esSkill = new EquipSeriesSkill();
            esSkill.setArmor(savedArmor);
            esSkill.setSeriesSkill(sSkill);

            esSkillList.add(esSkill);
        }
    }

    private String toJson(Object obj) { //배열 json화
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

}


