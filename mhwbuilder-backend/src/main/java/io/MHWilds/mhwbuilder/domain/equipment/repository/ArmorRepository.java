package io.MHWilds.mhwbuilder.domain.equipment.repository;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Armor;
import io.MHWilds.mhwbuilder.util.entityenums.EquipCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArmorRepository extends JpaRepository<Armor, String> {

    Optional<Armor> findByName(String name);

    boolean existsByName(String name);

    boolean existsByCode(int code);
    List<Armor> findByCategory(EquipCategory category);

    @Query("""
    select distinct a
    from Armor a
    where exists (
        select 1
        from EquipSeriesSkill ess
        where ess.armor = a
          and ess.seriesSkill.id = :setSkillId
    )
    """)
    List<Armor> findAllBySetSkillId(@Param("setSkillId") String setSkillId);

}
