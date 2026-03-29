package io.MHWilds.mhwbuilder.domain.equipment.repository;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Armor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArmorRepository extends JpaRepository<Armor, String> {

    Optional<Armor> findByName(String name);

    boolean existsByName(String name);

    boolean existsByCode(int code);
}
