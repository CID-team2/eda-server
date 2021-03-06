package eda.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeatureViewRepository extends JpaRepository<FeatureView, Long> {
    Optional<FeatureView> findByName(String name);
}
