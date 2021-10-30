package eda.domain;

import eda.domain.data.DataReader;
import eda.domain.data.StatisticCalculator;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
public class Statistic {
    private final DataReader dataReader;
    private final Random random = new Random();

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
                    result.putAll(StatisticCalculator.getNumericStatistics(valuesNumber));
                }
            case ORDINAL:
                List<Comparable<Object>> valuesComparable = values.stream().map(v -> (Comparable<Object>) v).toList();
                result.putAll(StatisticCalculator.getOrdinalStatistics(valuesComparable));
            case CATEGORICAL:
                result.put("mode", StatisticCalculator.getMode(values));
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
            throw new UnsupportedOperationException("'%s' is not supported".formatted(statisticRequestDto.getName()));
        }
        if (!kind.supports(feature.getDataType(), feature.getFeatureType()))
            throw new UnsupportedOperationException(
                    "'%s' is not supported with DataType '%s', FeatureType '%s'".formatted(
                            statisticRequestDto.getName(), feature.getDataType(), feature.getFeatureType()
                    ));

        List<Object> values = dataReader.read(feature.getDataset().getPath(), feature.getColumnName(),
                feature.getDataType()).stream()
                .filter(Objects::nonNull)
                .toList();
        return switch (kind) {
            case BOXPLOT -> StatisticCalculator.getBoxplot(values.stream().map(Number.class::cast).toList());
        };
    }

    public List<String> getExample(Dataset dataset, String columnName, int count, boolean random) {
        List<Object> values = dataReader.read(dataset.getPath(), columnName, DataType.STRING);
        List<String> result = new ArrayList<>();
        final int size = Math.min(values.size(), count);
        if (random) {
            for (int i = 0; i < size; i++) {
                result.add((String) values.get(this.random.nextInt(size)));
            }
        }
        else {
            result = values.stream().limit(size).map(String.class::cast).toList();
        }
        return result;
    }

    public List<String> getExample(Feature feature, int count, boolean random) {
        return getExample(feature.getDataset(), feature.getColumnName(), count, random);
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
