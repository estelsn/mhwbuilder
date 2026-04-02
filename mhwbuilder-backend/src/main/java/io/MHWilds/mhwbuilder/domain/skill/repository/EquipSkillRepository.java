package io.MHWilds.mhwbuilder.domain.skill.repository;

import io.MHWilds.mhwbuilder.domain.skill.entity.EquipSkill;
import io.MHWilds.mhwbuilder.util.entityenums.EquipType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipSkillRepository extends JpaRepository<EquipSkill, String> {

    List<EquipSkill> findByEquipId(String equipId);

    Optional<EquipSkill> findByEquipIdAndSkill_Id(String equipId, String skillId);

    List<EquipSkill> findByEquipType(EquipType equipType);

    boolean existsByEquipIdAndSkill_Id(String equipId, String skillId);
    List<EquipSkill> findByEquipTypeAndEquipId(EquipType equipType, String equipId);
}
