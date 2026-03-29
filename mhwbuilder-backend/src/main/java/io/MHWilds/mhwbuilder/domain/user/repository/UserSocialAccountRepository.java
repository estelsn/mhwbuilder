package io.MHWilds.mhwbuilder.domain.user.repository;


import io.MHWilds.mhwbuilder.domain.user.entity.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, String> {

    Optional<UserSocialAccount> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByProviderAndProviderId(String provider, String providerId);
}