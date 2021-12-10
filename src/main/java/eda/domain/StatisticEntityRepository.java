package eda.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StatisticEntityRepository extends JpaRepository<StatisticEntity, Long> {
    @Query(value = "SELECT se " +
            "FROM StatisticEntity se " +
            "WHERE column = :column AND featureType = :featureType AND kind = :kind")
    Optional<StatisticEntity> get(DatasetColumn column, FeatureType featureType, Statistic.Kind kind);

    @Query(value = "SELECT se " +
            "FROM StatisticEntity se " +
            "Where column.id = :columnId")
    List<StatisticEntity> findAllByColumnId(Long columnId);
}
