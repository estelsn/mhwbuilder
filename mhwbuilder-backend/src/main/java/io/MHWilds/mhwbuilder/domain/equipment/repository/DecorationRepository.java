package io.MHWilds.mhwbuilder.domain.equipment.repository;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Decoration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DecorationRepository extends JpaRepository<Decoration, String> {
    Optional<Decoration> findByName(String name);

    boolean existsByName(String name);
}
