package io.MHWilds.mhwbuilder.domain.skill.repository;

import io.MHWilds.mhwbuilder.domain.skill.entity.SeriesSkill;
import io.MHWilds.mhwbuilder.domain.skill.entity.SeriesSkillEffect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesSkillEffectRepository extends JpaRepository<SeriesSkillEffect, String> {
    Optional<SeriesSkillEffect> findBySeriesSkill_IdAndRequiredCount(String seriesSkillId, int requiredCount);
    boolean existsBySeriesSkill_IdAndRequiredCount(String seriesSkillId, int requiredCount);
    List<SeriesSkillEffect> findAllBySeriesSkill_Id(String seriesSkillId);
    boolean existsBySeriesSkillAndRequiredCount(SeriesSkill seriesSkill, int requiredCount);

}
