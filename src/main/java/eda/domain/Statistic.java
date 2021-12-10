package eda.domain;

import eda.domain.data.DataReader;
import eda.domain.data.StatisticCalculator;
import eda.dto.StatisticRequestDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Null;
import java.util.*;

@RequiredArgsConstructor
@Component
public class Statistic {
    private final DataReader dataReader;
    private final StatisticEntityRepository statisticEntityRepository;
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

    public Map<String, Object> getBasicStatistic(Feature feature) {
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

    public Map<String, Object> getBasicStatistic(List<Feature> features) {
        if (features.size() == 1)
            return getBasicStatistic(features.get(0));

        return Map.of("null_count", getNullCount(features));
    }

    public Map<String, Object> getStatistic(Feature feature, StatisticRequestDto statisticRequestDto) {
        checkValidRequest(List.of(feature), statisticRequestDto.getName());
        Kind kind = Kind.valueOf(statisticRequestDto.getName().toUpperCase());

        List<Object> values = dataReader.read(feature.getDataset().getPath(), feature.getColumnName(),
                feature.getDataType()).stream()
                .filter(Objects::nonNull)
                .toList();
        final Map<String, Object> params = Optional.ofNullable(statisticRequestDto.getParams()).orElse(Map.of());

        if (values.isEmpty())
            return Map.of("error", "all values are null");

        return switch (kind) {
            case BASIC -> getBasicStatistic(feature);
            case BOXPLOT -> StatisticCalculator.getBoxplot(values.stream().map(Number.class::cast).toList());
            case HISTOGRAM -> {
                Map<String, Object> boxplot =
                        getStatisticFromEntity(List.of(feature), StatisticRequestDto.builder().name("boxplot").build());
                double start;
                double end;
                int breaks;
                try {
                    start = Double.parseDouble((String) params.getOrDefault("start", boxplot.get("lowerWhisker").toString()));
                    end = Double.parseDouble((String) params.getOrDefault("end", boxplot.get("upperWhisker").toString()));
                    breaks = Integer.parseInt((String) params.getOrDefault("breaks", "10"));
                } catch (NullPointerException | NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse request param: " + e.getMessage());
                }
                yield StatisticCalculator.getHistogram(
                    values.stream().map(Number.class::cast).toList(), start, end, breaks);
            }
            case BARPLOT -> {
                double threshold;
                try {
                    threshold = Double.parseDouble((String) params.getOrDefault("threshold", "0.05"));
                } catch (NullPointerException | NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse request param: " + e.getMessage());
                }
                yield StatisticCalculator.getBarPlot(values, threshold);
            }
            case CORR_MATRIX -> throw new UnsupportedOperationException("Correlation matrix is for multiple features");
        };
    }

    public Map<String, Object> getStatistic(List<Feature> features, StatisticRequestDto statisticRequestDto) {
        if (features.size() == 1)
            return getStatistic(features.get(0), statisticRequestDto);

        features = checkValidRequest(features, statisticRequestDto.getName());
        Kind kind = Kind.valueOf(statisticRequestDto.getName().toUpperCase());

        List<List<Object>> values = getNonnullRows(readFeatures(features));

        return switch (kind) {
            case BASIC -> getBasicStatistic(features);
            case BOXPLOT, HISTOGRAM, BARPLOT -> throw new UnsupportedOperationException(kind.name() + " is for single feature");
            case CORR_MATRIX -> {
                double[][] matrix = StatisticCalculator.getCorrMatrix(values.stream()
                        .map(l -> l.stream().map(Number.class::cast).toList())
                        .toList());
                List<String> featureNames = features.stream().map(Feature::getName).toList();
                yield Map.of("matrix", matrix,
                        "features", featureNames);
            }
        };
    }

    public Map<String, Object> getStatisticFromEntity(List<Feature> features, StatisticRequestDto statisticRequestDto) {
        if (features.size() != 1)
            return getStatistic(features, statisticRequestDto);

        Feature feature = features.get(0);
        Map<String, Object> params = statisticRequestDto.getParams();
        if (params != null && !params.isEmpty())
            return getStatistic(feature, statisticRequestDto);

        Kind kind = Kind.valueOf(statisticRequestDto.getName().toUpperCase());
        Optional<StatisticEntity> statisticEntityOptional =
                statisticEntityRepository.get(feature.getColumn(), feature.getFeatureType(), kind);
        if (statisticEntityOptional.isEmpty())
            return getStatistic(feature, statisticRequestDto);

        StatisticEntity statisticEntity = statisticEntityOptional.get();
        if (statisticEntity.getModifiedAt().isBefore(feature.getDataset().getModifiedAt()))
            return getStatistic(feature, statisticRequestDto);
        Map<String, Object> result = new HashMap<>(statisticEntity.getValue());
        result.put("calculated_at", statisticEntity.getModifiedAt());
        return result;
    }

    public List<String> getExample(Dataset dataset, String columnName, int count, Integer randomSeed) {
        List<Object> values = dataReader.read(dataset.getPath(), columnName, DataType.STRING);
        List<String> result = new ArrayList<>();
        final int size = Math.min(values.size(), count);
        if (randomSeed != null) {
            this.random.setSeed(randomSeed);
            for (int i = 0; i < size; i++) {
                result.add((String) values.get(this.random.nextInt(values.size())));
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

    public List<Feature> checkValidRequest(List<Feature> features, String statistic)
            throws UnsupportedOperationException {
        List<Feature> result = new LinkedList<>(features);
        Kind kind;

        // check statistic exists
        try {
            kind = Kind.valueOf(statistic.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("'%s' is not supported".formatted(statistic));
        }

        // check statistic is valid for all featureType and dataType
        result.removeIf(feature -> !kind.supports(feature.getDataType(), feature.getFeatureType()));
        if (result.isEmpty())
            throw new UnsupportedOperationException("The type(s) of feature(s) given do not match the statistic");
        return result;
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
            for (List<Object> value : values) {
                if (value.get(i) == null) {
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

    @Getter
    @RequiredArgsConstructor
    public enum Kind {
        BASIC(Set.of(DataType.values()),
                Set.of(FeatureType.values()),
                Set.of(Type.values())),
        BOXPLOT(Set.of(DataType.INT, DataType.FLOAT),
                Set.of(FeatureType.QUANTITATIVE, FeatureType.ORDINAL),
                Set.of(Type.SINGLE)),
        HISTOGRAM(Set.of(DataType.INT, DataType.FLOAT),
                Set.of(FeatureType.QUANTITATIVE),
                Set.of(Type.SINGLE)),
        BARPLOT(Set.of(DataType.values()),
                Set.of(FeatureType.ORDINAL, FeatureType.CATEGORICAL),
                Set.of(Type.SINGLE)),
        CORR_MATRIX(Set.of(DataType.INT, DataType.FLOAT),
                Set.of(FeatureType.QUANTITATIVE),
                Set.of(Type.MULTIPLE));

        private final Set<DataType> supportedDataTypes;
        private final Set<FeatureType> supportedFeatureTypes;
        private final Set<Type> type;

        public boolean supports(DataType dataType, FeatureType featureType) {
            return supportDataType(dataType) && supportFeatureType(featureType);
        }

        public boolean supportDataType(DataType dataType) {
            return supportedDataTypes.contains(dataType);
        }

        public boolean supportFeatureType(FeatureType featureType) {
            return supportedFeatureTypes.contains(featureType);
        }

        public enum Type {
            SINGLE, MULTIPLE
        }
    }
}
