package io.MHWilds.mhwbuilder.domain.skill.service;

import io.MHWilds.mhwbuilder.domain.skill.dto.response.NormalSkillOptionResponse;
import io.MHWilds.mhwbuilder.domain.skill.dto.response.SetSkillEffectOptionResponse;
import io.MHWilds.mhwbuilder.domain.skill.dto.response.SetSkillOptionResponse;
import io.MHWilds.mhwbuilder.domain.skill.dto.response.SkillSelectPageResponse;
import io.MHWilds.mhwbuilder.domain.skill.repository.SeriesSkillEffectRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SeriesSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SkillRepository;
import io.MHWilds.mhwbuilder.util.entityenums.SkillCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillQueryServiceImpl implements SkillQueryService {

    private final SkillRepository skillRepository;
    private final SeriesSkillRepository seriesSkillRepository;
    private final SeriesSkillEffectRepository seriesSkillEffectRepository;

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
        return skillRepository.findAllByOrderByNameAsc().stream()
                .map(skill -> NormalSkillOptionResponse.builder()
                        .skillId(skill.getId())
                        .skillName(skill.getName())
                        .maxLevel(skill.getMaxLevel())
                        .build())
                .toList();
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
}