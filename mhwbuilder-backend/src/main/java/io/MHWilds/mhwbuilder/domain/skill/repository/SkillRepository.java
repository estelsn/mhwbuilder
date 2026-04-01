package io.MHWilds.mhwbuilder.domain.skill.repository;

import io.MHWilds.mhwbuilder.domain.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, String> {

    Optional<Skill> findByCode(int code);

    @Query("select s.code from Skill s")
    List<Integer> findAllCodes();

    boolean existsByCode(int code);

    List<Skill> findAllByOrderByNameAsc();
}
