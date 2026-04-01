package io.MHWilds.mhwbuilder.domain.skill.repository;

import io.MHWilds.mhwbuilder.domain.skill.entity.SeriesSkill;
import io.MHWilds.mhwbuilder.util.entityenums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesSkillRepository extends JpaRepository<SeriesSkill, String> {
    Optional<SeriesSkill> findByName(String name);

    boolean existsByCode(int code);

    List<SeriesSkill> findByCategoryOrderByNameAsc(SkillCategory category);
}
