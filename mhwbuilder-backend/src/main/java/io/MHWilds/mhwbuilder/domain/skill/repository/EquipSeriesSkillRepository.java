package io.MHWilds.mhwbuilder.domain.skill.repository;

import io.MHWilds.mhwbuilder.domain.skill.entity.EquipSeriesSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipSeriesSkillRepository extends JpaRepository<EquipSeriesSkill, String> {
    // 특정 방어구에 붙은 시리즈 스킬 조회
    List<EquipSeriesSkill> findByArmor_Id(String armorId);

    // 특정 시리즈 스킬을 가진 방어구들
    List<EquipSeriesSkill> findBySeriesSkill_Id(String seriesSkillId);

    // 중복 체크용 (중요)
    boolean existsByArmor_IdAndSeriesSkill_Id(String armorId, String seriesSkillId);
}

