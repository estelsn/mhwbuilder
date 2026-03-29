package io.MHWilds.mhwbuilder.domain.buildset.repository;

import io.MHWilds.mhwbuilder.domain.buildset.entity.BuildSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildSetRepository extends JpaRepository<BuildSet, String> {
    List<BuildSet> findByMhUser_Id(String mhUserId);
}