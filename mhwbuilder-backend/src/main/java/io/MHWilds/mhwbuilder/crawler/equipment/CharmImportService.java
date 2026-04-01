package io.MHWilds.mhwbuilder.crawler.equipment;

import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawCharmDto;
import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawEquipSkillDto;
import io.MHWilds.mhwbuilder.domain.equipment.entity.Charm;
import io.MHWilds.mhwbuilder.domain.equipment.repository.CharmRepository;
import io.MHWilds.mhwbuilder.domain.skill.entity.EquipSkill;
import io.MHWilds.mhwbuilder.domain.skill.entity.Skill;
import io.MHWilds.mhwbuilder.domain.skill.repository.EquipSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SkillRepository;
import io.MHWilds.mhwbuilder.util.entityenums.EquipType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharmImportService {

    private final CharmCrawlService charmCrawlService;
    private final SkillRepository skillRepository;
    private final CharmRepository charmRepository;
    private final EquipSkillRepository equipSkillRepository;

    @Transactional
    public int importCharms() throws Exception{
        int count = 0;

        List<RawCharmDto> tempRawCharms = charmCrawlService.crawlCharms();
        List<RawCharmDto> rawCharms = keepHighestLevelBySkill(tempRawCharms);

        Map<Integer, Skill> skillMap = skillRepository.findAll().stream()
                .collect(Collectors.toMap(Skill::getCode, Function.identity()));

        Set<Integer> existingCodes = charmRepository.findAll().stream()
                .map(Charm::getCode)
                .collect(Collectors.toSet());

        List<EquipSkill> eSkillList = new ArrayList<>();

        for(RawCharmDto rawCharm : rawCharms){
            if(existingCodes.contains(rawCharm.getCode())){
                continue;
            }
            Charm savedCharm = saveCharm(rawCharm);
            saveEquipSkill(savedCharm, rawCharm, skillMap, eSkillList);
            equipSkillRepository.saveAll(eSkillList);
            count++;
        }

        equipSkillRepository.saveAll(eSkillList);

        return count;
    }

    private Charm saveCharm(RawCharmDto rawCharm){
        //{"code":1,"name":"강주의 호석Ⅰ","maxLevel":1,"equipSkill":{"code":123,"name":"런너","level":1}}
        Charm charm = new Charm();
        charm.setCode(rawCharm.getCode());
        charm.setName(rawCharm.getName());
        charm.setVersion("v1");

        return charmRepository.save(charm);
    }

    private void saveEquipSkill(Charm savedCharm, RawCharmDto rawCharm, Map<Integer, Skill> skillMap, List<EquipSkill> eSkillList){

        Skill skill = skillMap.get(rawCharm.getEquipSkill().getCode());
        if (skill == null) {
            throw new IllegalStateException("존재하지 않는 스킬 코드입니다. code=" + rawCharm.getEquipSkill().getCode());
        }

        EquipSkill eSkill = new EquipSkill();
        eSkill.setEquipId(savedCharm.getId());
        eSkill.setEquipType(EquipType.CHARM);
        eSkill.setSkill(skill);
        eSkill.setSkillLevel(rawCharm.getEquipSkill().getLevel());
        System.out.println(skill.getName());
        eSkillList.add(eSkill);
    }


    private List<RawCharmDto> keepHighestLevelBySkill(List<RawCharmDto> rawCharms) {

        Map<Integer, RawCharmDto> bestMap = new HashMap<>();

        for (RawCharmDto rawCharm : rawCharms) {

            RawEquipSkillDto skill = rawCharm.getEquipSkill();
            if (skill == null) {throw new IllegalStateException("호석에 스킬이 없습니다. code=" + rawCharm.getCode());}

            int skillCode = skill.getCode();

            RawCharmDto existingCharm = bestMap.get(skillCode);

            if (existingCharm == null) {
                bestMap.put(skillCode, rawCharm);
                continue;
            }

            int existingLevel = existingCharm.getEquipSkill().getLevel();
            int currentLevel = skill.getLevel();

            if (currentLevel > existingLevel) {
                bestMap.put(skillCode, rawCharm);
            }
        }

        return new ArrayList<>(bestMap.values());
    }
}
