package io.MHWilds.mhwbuilder.domain.buildset.repository;

import io.MHWilds.mhwbuilder.domain.buildset.entity.BuildEquip;
import io.MHWilds.mhwbuilder.domain.buildset.entity.BuildEquipDecoration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuildEquipDecorationRepository extends JpaRepository<BuildEquipDecoration, String> {
    List<BuildEquipDecoration> findByBuildEquip_Id(String buildEquipId);

    Optional<BuildEquipDecoration> findByBuildEquip_IdAndDecoration_Id(String buildEquipId, String decorationId);
}
