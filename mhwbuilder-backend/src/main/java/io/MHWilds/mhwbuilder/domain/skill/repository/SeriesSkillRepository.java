package io.MHWilds.mhwbuilder.domain.skill.repository;

import io.MHWilds.mhwbuilder.domain.skill.entity.SeriesSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeriesSkillRepository extends JpaRepository<SeriesSkill, String> {
    Optional<SeriesSkill> findByName(String name);

    boolean existsByName(String name);

    boolean existsByCode(int code);
}
