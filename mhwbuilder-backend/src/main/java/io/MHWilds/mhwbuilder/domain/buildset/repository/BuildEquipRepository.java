package io.MHWilds.mhwbuilder.domain.buildset.repository;


import io.MHWilds.mhwbuilder.domain.buildset.entity.BuildEquip;
import io.MHWilds.mhwbuilder.util.entityenums.EquipCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildEquipRepository extends JpaRepository<BuildEquip, String> {

    List<BuildEquip> findByBuildSet_Id(String buildSetId);


}