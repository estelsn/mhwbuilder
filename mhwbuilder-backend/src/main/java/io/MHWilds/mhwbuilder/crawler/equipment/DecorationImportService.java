package io.MHWilds.mhwbuilder.crawler.equipment;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Decoration;
import io.MHWilds.mhwbuilder.domain.equipment.repository.DecorationRepository;
import io.MHWilds.mhwbuilder.domain.skill.entity.EquipSkill;
import io.MHWilds.mhwbuilder.domain.skill.entity.Skill;
import io.MHWilds.mhwbuilder.domain.skill.repository.EquipSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SkillRepository;
import io.MHWilds.mhwbuilder.util.entityenums.EquipType;
import io.MHWilds.mhwbuilder.util.entityenums.GearSkillType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DecorationImportService {
    private final DecorationCrawlService decorationCrawlService;
    private final DecorationRepository decorationRepository;
    private final EquipSkillRepository equipSkillRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public int importDecorations() throws IOException{
        int count = 0;
        //{"code":1,"name":"공격주【1】","category":1,"rarity":3,"slotLevel":1,
        // "equipSkillList":[{"code":101,"name":"공격","level":1}]}
        List<RawDecorationDto> rawDecoes = decorationCrawlService.crawlDecorations();

        Set<Integer> existingCodes = decorationRepository.findAll().stream()
                .map(Decoration::getCode)
                .collect(Collectors.toSet());

        Map<Integer, Skill> skillMap = skillRepository.findAll().stream()
                .collect(Collectors.toMap(Skill::getCode, Function.identity()));
        List<EquipSkill> eSkillList = new ArrayList<>();

        for(RawDecorationDto rawDeco : rawDecoes){
            verifyRawDecoData(rawDeco);
            if (existingCodes.contains(rawDeco.getCode())) {
                continue;
            }

            for(RawEquipSkillDto reSkill : rawDeco.getEquipSkillList()){
                verifyRawEquipSkillData(reSkill, skillMap);
            }
            Decoration savedDeco = decorationRepository.save(buildDecoration(rawDeco));
            existingCodes.add(savedDeco.getCode());

            for(RawEquipSkillDto reSkill : rawDeco.getEquipSkillList()){
                eSkillList.add(buildEquipSkill(savedDeco, reSkill, skillMap));
            }
            count++;
        }
        equipSkillRepository.saveAll(eSkillList);

        return count;
    }
    //{"code":1,"name":"공격주【1】","category":1,"rarity":3,"slotLevel":1,
    // "equipSkillList":[{"code":101,"name":"공격","level":1}]}
    private void verifyRawDecoData(RawDecorationDto rawDeco){
        if (rawDeco == null) {throw new IllegalStateException("장식주 데이터가 null입니다");}
        if(rawDeco.getCode() == 0){ throw new IllegalStateException("코드가 존재하지 않습니다");}
        if (rawDeco.getName() == null || rawDeco.getName().isBlank()) {
            throw new IllegalStateException("이름이 존재하지 않습니다");
        }
        int category = rawDeco.getCategory();
        if(category != 1 && category != 2){throw new IllegalStateException("카테고리 값이 잘못되었습니다");}
        int rarity = rawDeco.getRarity();
        if(rarity<2 || rarity >8){throw new IllegalStateException("레어도가 범주를 넘어섰습니다");}
        int slotLv = rawDeco.getSlotLevel();
        if(slotLv !=1 && slotLv !=2 && slotLv !=3){throw new IllegalStateException("슬롯 레벨이 잘못되었습니다");}
        if (rawDeco.getEquipSkillList() == null || rawDeco.getEquipSkillList().isEmpty()) {
            throw new IllegalStateException("장식주 스킬 목록이 비어 있습니다. code=" + rawDeco.getCode());
        }

    }

    private void verifyRawEquipSkillData(RawEquipSkillDto rawSkill, Map<Integer, Skill> skillMap){
        if (rawSkill == null) {throw new IllegalStateException("장비 스킬 데이터가 null입니다");}
        Skill skill = skillMap.get(rawSkill.getCode());

        if(skill == null){
            throw new IllegalStateException("존재하지 않는 스킬 code : " + rawSkill.getCode());
        }
        if (!skill.getName().equals(rawSkill.getName())){
            throw new IllegalStateException(
                    "스킬 이름 불일치. code : "+ rawSkill.getCode()
                            + ", rawName : " + rawSkill.getName()
                            + ", dbName : " + skill.getName()
            );
        }
    }

    private Decoration buildDecoration(RawDecorationDto rawDeco){
        Decoration deco = new Decoration();
        deco.setCode(rawDeco.getCode());
        deco.setName(rawDeco.getName());
        deco.setType(GearSkillType.fromCode(rawDeco.getCategory()));
        deco.setRarity(rawDeco.getRarity());
        deco.setSlotLevel(rawDeco.getSlotLevel());
        deco.setVersion("v1");
        return deco;
    }

    private EquipSkill buildEquipSkill(Decoration savedDeco, RawEquipSkillDto reSkill, Map<Integer, Skill> skillMap){
        EquipSkill eSkill = new EquipSkill();

        eSkill.setEquipId(savedDeco.getId());
        eSkill.setEquipType(EquipType.DECORATION);
        eSkill.setSkill(skillMap.get(reSkill.getCode()));
        eSkill.setSkillLevel(reSkill.getLevel());
        return eSkill;
    }



}
