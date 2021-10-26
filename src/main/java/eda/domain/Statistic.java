package eda.domain;

import eda.data.DataReader;
import eda.data.StatisticsCalculator;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
public class Statistic {
    private final DataReader dataReader;

    public int getNullCount(Feature feature) {
        List<Object> valuesWithNull = dataReader.read(feature.getDataset().getPath(), feature.getColumnName(),
                feature.getDataType());
        List<Object> values = valuesWithNull.stream().filter(Objects::nonNull).toList();
        return valuesWithNull.size() - values.size();
    }

    public Map<String, Object> getStatistic(Feature feature) {
        List<Object> values = dataReader.read(feature.getDataset().getPath(), feature.getColumnName(),
                feature.getDataType()).stream()
                .filter(Objects::nonNull)
                .toList();

        Map<String, Object> result = new HashMap<>();
        switch (feature.getFeatureType()) {
            case QUANTITATIVE:
                if (feature.getDataType() == DataType.INT || feature.getDataType() == DataType.FLOAT) {
                    List<Number> valuesNumber = values.stream().map(Number.class::cast).toList();
                    result.putAll(StatisticsCalculator.getNumericStatistics(valuesNumber));
                }
            case ORDINAL:
                List<Comparable<Object>> valuesComparable = values.stream().map(v -> (Comparable<Object>) v).toList();
                result.putAll(StatisticsCalculator.getOrdinalStatistics(valuesComparable));
            case CATEGORICAL:
                result.put("mode", StatisticsCalculator.getMode(values));
                break;
            case CUSTOM:
                break;
        }
        return result;
    }

    public Map<String, Object> getStatistic(Feature feature, StatisticRequestDto statisticRequestDto) {
        Kind kind;
        try {
            kind = Kind.valueOf(statisticRequestDto.getName().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (!kind.supports(feature.getDataType(), feature.getFeatureType()))
            return null;

        List<Object> values = dataReader.read(feature.getDataset().getPath(), feature.getColumnName(),
                feature.getDataType()).stream()
                .filter(Objects::nonNull)
                .toList();
        return switch (kind) {
            case BOXPLOT -> StatisticsCalculator.getBoxplot(values.stream().map(Number.class::cast).toList());
        };
    }

    @RequiredArgsConstructor
    enum Kind {
        BOXPLOT(Set.of(DataType.INT, DataType.FLOAT),
                Set.of(FeatureType.QUANTITATIVE));

        private final Set<DataType> supportedDataTypes;
        private final Set<FeatureType> supportedFeatureTypes;


        public boolean supports(DataType dataType, FeatureType featureType) {
            return supportDataType(dataType) && supportFeatureType(featureType);
        }

        public boolean supportDataType(DataType dataType) {
            return supportedDataTypes.contains(dataType);
        }

        public boolean supportFeatureType(FeatureType featureType) {
            return supportedFeatureTypes.contains(featureType);
        }
    }
}
