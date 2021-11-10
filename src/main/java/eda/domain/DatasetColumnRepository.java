package eda.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DatasetColumnRepository extends JpaRepository<DatasetColumn, Long> {
    @Query(value = "SELECT c " +
            "FROM DatasetColumn c INNER JOIN Dataset d ON c.dataset = d " +
            "WHERE d.name = :datasetName AND c.name = :columnName")
    Optional<DatasetColumn> findByDatasetAndColumnName(String datasetName, String columnName);
}
