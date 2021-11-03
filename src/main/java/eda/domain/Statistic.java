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

    public int getNullCount(List<Feature> features) {
        List<List<Object>> valuesWithNull = readFeatures(features);
        List<List<Object>> values = getNonnullRows(valuesWithNull);
        return valuesWithNull.get(0).size() - values.get(0).size();
    }

    public Map<String, Object> getStatistic(Feature feature) {
        List<Object> values = dataReader.read(feature.getDataset().getPath(), feature.getColumnName(),
                feature.getDataType()).stream()
                .filter(Objects::nonNull)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("null_count", getNullCount(feature));
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
        checkValidRequest(List.of(feature), statisticRequestDto);
        Kind kind = Kind.valueOf(statisticRequestDto.getName().toUpperCase());

        List<Object> values = dataReader.read(feature.getDataset().getPath(), feature.getColumnName(),
                feature.getDataType()).stream()
                .filter(Objects::nonNull)
                .toList();
        return switch (kind) {
            case BOXPLOT -> StatisticCalculator.getBoxplot(values.stream().map(Number.class::cast).toList());
            case CORR_MATRIX -> throw new UnsupportedOperationException("Correlation matrix is for multiple features");
        };
    }

    public Map<String, Object> getStatistic(List<Feature> features) {
        if (features.size() == 1)
            return getStatistic(features.get(0));

        return Map.of("null_count", getNullCount(features));
    }

    public Map<String, Object> getStatistic(List<Feature> features, StatisticRequestDto statisticRequestDto) {
        if (features.size() == 1)
            return getStatistic(features.get(0), statisticRequestDto);

        checkValidRequest(features, statisticRequestDto);
        Kind kind = Kind.valueOf(statisticRequestDto.getName().toUpperCase());

        List<List<Object>> values = getNonnullRows(readFeatures(features));

        return switch (kind) {
            case BOXPLOT -> throw new UnsupportedOperationException("Boxplot is for single feature");
            case CORR_MATRIX -> Map.of("matrix",
                    StatisticCalculator.getCorrMatrix(values.stream()
                            .map(l -> l.stream().map(Number.class::cast).toList())
                            .toList()));
        };
    }

    public List<String> getExample(Dataset dataset, String columnName, int count, Integer randomSeed) {
        List<Object> values = dataReader.read(dataset.getPath(), columnName, DataType.STRING);
        List<String> result = new ArrayList<>();
        final int size = Math.min(values.size(), count);
        if (randomSeed != null) {
            this.random.setSeed(randomSeed);
            for (int i = 0; i < size; i++) {
                result.add((String) values.get(this.random.nextInt(size)));
            }
        }
        else {
            result = values.stream().limit(size).map(String.class::cast).toList();
        }
        return result;
    }

    public List<String> getExample(Feature feature, int count, Integer randomSeed) {
        return getExample(feature.getDataset(), feature.getColumnName(), count, randomSeed);
    }

    private void checkValidRequest(List<Feature> features, StatisticRequestDto statisticRequestDto)
            throws UnsupportedOperationException {
        Kind kind;

        // check statistic exists
        try {
            kind = Kind.valueOf(statisticRequestDto.getName().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("'%s' is not supported".formatted(statisticRequestDto.getName()));
        }

        // check statistic is valid for all featureType and dataType
        for (Feature feature : features) {
            if (!kind.supports(feature.getDataType(), feature.getFeatureType()))
                throw new UnsupportedOperationException(
                        "'%s' is not supported with DataType '%s', FeatureType '%s'".formatted(
                                statisticRequestDto.getName(), feature.getDataType(), feature.getFeatureType()
                        ));
        }
    }

    private List<List<Object>> readFeatures(List<Feature> features) {
        List<List<Object>> result = new ArrayList<>();
        for (Feature feature : features) {
            List<Object> list = dataReader.read(feature.getDataset().getPath(), feature.getColumnName(),
                    feature.getDataType());
            result.add(list);
        }
        return result;
    }

    private List<List<Object>> getNonnullRows(List<List<Object>> values) {
        List<List<Object>> result = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            result.add(new ArrayList<>());
        }

        for (int i = 0; i < values.get(0).size(); i++) {
            boolean nonnull = true;
            for (int j = 0; j < values.size(); j++) {
                if (values.get(j).get(i) == null) {
                    nonnull = false;
                    break;
                }
            }
            if (nonnull) {
                for (int j = 0; j < values.size(); j++) {
                    result.get(j).add(values.get(j).get(i));
                }
            }
        }
        return result;
    }

    @RequiredArgsConstructor
    enum Kind {
        BOXPLOT(Set.of(DataType.INT, DataType.FLOAT),
                Set.of(FeatureType.QUANTITATIVE)),
        CORR_MATRIX(Set.of(DataType.INT, DataType.FLOAT),
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
