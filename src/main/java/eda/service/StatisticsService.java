package eda.service;

import eda.data.StatisticsCalculator;
import eda.domain.DataType;
import eda.domain.Feature;
import eda.domain.FeatureViewRepository;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class StatisticsService {
    private final FeatureViewRepository featureViewRepository;
    private final StatisticsCalculator statisticsCalculator;
    private final FeatureViewService featureViewService;

    public Optional<Map<String, Object>> getStatistic(String featureViewName, String featureName,
                                                      StatisticRequestDto statisticRequestDto) {
        Optional<Feature> featureOptional = featureViewService.getFeature(featureViewName, featureName);
        if (featureOptional.isEmpty())
            return Optional.empty();
        Feature feature = featureOptional.get();

        List<Object> valuesWithNull = feature.readValues();
        List<Object> values = valuesWithNull.stream().filter(Objects::nonNull).toList();
        long nullCount = valuesWithNull.size() - values.size();

        Map<String, Object> result = new HashMap<>();
        if (statisticRequestDto == null) {
            switch (feature.getFeatureType()) {
                case QUANTITATIVE:
                    if (feature.getDataType() == DataType.INT || feature.getDataType() == DataType.FLOAT) {
                        List<Number> valuesNumber = values.stream().map(Number.class::cast).toList();
                        result.putAll(statisticsCalculator.getNumericStatistics(valuesNumber));
                    }
                case ORDINAL:
                    List<Comparable<Object>> valuesComparable = values.stream().map(v -> (Comparable<Object>) v).toList();
                    result.putAll(statisticsCalculator.getOrdinalStatistics(valuesComparable));
                case CATEGORICAL:
                    result.put("mode", statisticsCalculator.getMode(values));
                    break;
                case CUSTOM:
                    break;
            }
        }

        return Optional.of(result);
    }
}
