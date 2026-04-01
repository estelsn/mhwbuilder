package io.MHWilds.mhwbuilder.domain.skill.service;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Decoration;
import io.MHWilds.mhwbuilder.domain.equipment.repository.DecorationRepository;
import io.MHWilds.mhwbuilder.domain.skill.dto.response.NormalSkillOptionResponse;
import io.MHWilds.mhwbuilder.domain.skill.dto.response.SetSkillEffectOptionResponse;
import io.MHWilds.mhwbuilder.domain.skill.dto.response.SetSkillOptionResponse;
import io.MHWilds.mhwbuilder.domain.skill.dto.response.SkillSelectPageResponse;
import io.MHWilds.mhwbuilder.domain.skill.entity.EquipSkill;
import io.MHWilds.mhwbuilder.domain.skill.repository.EquipSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SeriesSkillEffectRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SeriesSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SkillRepository;
import io.MHWilds.mhwbuilder.util.entityenums.EquipType;
import io.MHWilds.mhwbuilder.util.entityenums.SkillCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillQueryServiceImpl implements SkillQueryService {

    private final SkillRepository skillRepository;
    private final SeriesSkillRepository seriesSkillRepository;
    private final SeriesSkillEffectRepository seriesSkillEffectRepository;
    private final EquipSkillRepository equipSkillRepository;
    private final DecorationRepository decorationRepository;

    @Override
    public SkillSelectPageResponse getSkillSelectPage() {
        List<NormalSkillOptionResponse> normalSkills = getNormalSkillOptions();
        List<SetSkillOptionResponse> seriesSkills = getSetSkillOptions(SkillCategory.SERIES);
        List<SetSkillOptionResponse> groupSkills = getSetSkillOptions(SkillCategory.GROUP);

        return SkillSelectPageResponse.builder()
                .normalSkills(normalSkills)
                .seriesSkills(seriesSkills)
                .groupSkills(groupSkills)
                .build();
    }

    private List<NormalSkillOptionResponse> getNormalSkillOptions() {
        Map<String, Integer> decorationSlotLevelMap = getDecorationSlotLevelMap();

        List<NormalSkillOptionResponse> result = skillRepository.findAllByOrderByNameAsc().stream()
                .map(skill -> {
                    Integer slotLevel = decorationSlotLevelMap.get(skill.getId());

                    return NormalSkillOptionResponse.builder()
                            .skillId(skill.getId())
                            .skillName(skill.getName())
                            .maxLevel(skill.getMaxLevel())
                            .decorationSlotLevel(slotLevel)
                            .build();
                })
                .toList();

        return result;
    }

    private List<SetSkillOptionResponse> getSetSkillOptions(SkillCategory category) {
        return seriesSkillRepository.findByCategoryOrderByNameAsc(category).stream()
                .map(seriesSkill -> {
                    List<SetSkillEffectOptionResponse> effects = seriesSkillEffectRepository
                            .findBySeriesSkill_IdOrderByRequiredCountAsc(seriesSkill.getId())
                            .stream()
                            .map(effect -> SetSkillEffectOptionResponse.builder()
                                    .requiredCount(effect.getRequiredCount())
                                    .effectName(effect.getEffectTitle())
                                    .effectDescription(effect.getDescription())
                                    .build())
                            .toList();

                    return SetSkillOptionResponse.builder()
                            .setSkillId(seriesSkill.getId())
                            .setSkillName(seriesSkill.getName())
                            .effects(effects)
                            .build();
                })
                .toList();
    }

    private Map<String, Integer> getDecorationSlotLevelMap() {
        List<EquipSkill> decorationSkills = equipSkillRepository.findByEquipType(EquipType.DECORATION);
        System.out.println("decorationSkills size = " + decorationSkills.size());


        Set<String> decorationIds = decorationSkills.stream()
                .map(EquipSkill::getEquipId)
                .collect(Collectors.toSet());
        System.out.println("decorationIds size = " + decorationIds.size());
        System.out.println("decorationIds sample = " + decorationIds.stream().limit(10).toList());

        Map<String, Decoration> decorationMap = decorationRepository.findAllById(decorationIds).stream()
                .collect(Collectors.toMap(Decoration::getId, decoration -> decoration));
        System.out.println("decorationMap size = " + decorationMap.size());
        System.out.println("decorationMap keys sample = " + decorationMap.keySet().stream().limit(10).toList());

        Map<String, Integer> skillMinSlotMap = new HashMap<>();

        for (EquipSkill equipSkill : decorationSkills) {
            String skillId = equipSkill.getSkill().getId();
            String equipId = equipSkill.getEquipId();
            Decoration decoration = decorationMap.get(equipId);

            if (decoration == null) {
                System.out.println("decoration not found for equipId = " + equipId);
                continue;
            }

            int slotLevel = decoration.getSlotLevel();

            skillMinSlotMap.merge(skillId, slotLevel, Math::min);
        }

        return skillMinSlotMap;
    }
}