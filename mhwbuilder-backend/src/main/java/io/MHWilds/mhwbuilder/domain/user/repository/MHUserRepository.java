package io.MHWilds.mhwbuilder.domain.user.repository;

import io.MHWilds.mhwbuilder.domain.user.entity.MHUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MHUserRepository extends JpaRepository<MHUser, String> {
    Optional<MHUser> findByName(String name);

    Optional<MHUser> findByEmail(String email);

    boolean existsByName(String name);

    boolean existsByEmail(String email);
}
