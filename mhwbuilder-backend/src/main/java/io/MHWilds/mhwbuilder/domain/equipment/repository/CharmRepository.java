package io.MHWilds.mhwbuilder.domain.equipment.repository;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Charm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharmRepository extends JpaRepository<Charm, String> {

    Optional<Charm> findByName(String name);

    boolean existsByName(String name);

    boolean existsByCode(int code);
}
