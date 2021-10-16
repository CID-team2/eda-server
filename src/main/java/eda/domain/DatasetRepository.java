package eda.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatasetRepository extends JpaRepository<Dataset, Integer> {
    Optional<Dataset> findByName(String name);
}
