package eda.worker;

import eda.domain.*;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "worker", name = "enabled", havingValue = "true")
@EnableScheduling
@Component
public class Worker {
    private final DatasetColumnRepository columnRepository;
    private final StatisticEntityRepository statisticEntityRepository;
    private final Statistic statistic;

    @Scheduled(fixedRateString = "${worker.schedule.interval:86400000}")
    public void precalculateStatistics() {
        List<DatasetColumn> columnList = columnRepository.findAll();
        for (DatasetColumn column : columnList) {
            for (FeatureType featureType : FeatureType.values()) {
                for (Statistic.Kind kind : Statistic.Kind.values()) {
                    if (!kind.getType().contains(Statistic.Kind.Type.SINGLE))
                        continue;
                    if (!kind.supports(column.getDataType(), featureType))
                        continue;

                    Map<String, Object> result = getStatistic(column, featureType, kind);
                    saveStatistic(column, featureType, kind, result);
                }
            }
        }
        log.info("Worker finished");
    }

    private Map<String, Object> getStatistic(DatasetColumn column, FeatureType featureType, Statistic.Kind kind) {
        Feature feature = Feature.builder()
                .name("worker_temp_feature")
                .dataset(column.getDataset())
                .column(column)
                .featureType(featureType)
                .build();
        StatisticRequestDto statisticRequestDto = StatisticRequestDto.builder()
                .name(kind.name())
                .build();
        return statistic.getStatistic(feature, statisticRequestDto);
    }

    private void saveStatistic(DatasetColumn column, FeatureType featureType, Statistic.Kind kind, Map<String, Object> value) {
        Optional<StatisticEntity> statisticEntityOptional = statisticEntityRepository.get(column, featureType, kind);
        StatisticEntity statisticEntity;
        if (statisticEntityOptional.isPresent()) {
            statisticEntity = statisticEntityOptional.get();
            statisticEntity.setValue(value);
        } else {
            statisticEntity = StatisticEntity.builder()
                    .column(column)
                    .featureType(featureType)
                    .kind(kind)
                    .value(value)
                    .build();
        }
        statisticEntityRepository.save(statisticEntity);
    }
}
