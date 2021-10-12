package eda.domain;

import eda.data.ORCReader;
import eda.data.StatisticsCalculator;
import eda.dto.StatisticRequestDto;
import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;

@Getter
@Entity
public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_id")
    private Set<Feature> children;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    private String columnName;

    private DataType dataType;

    private FeatureType featureType;

    @ManyToOne
    @JoinColumn(name = "feature_view_id")
    private FeatureView featureView;

    @ElementCollection
    @CollectionTable(name = "feature_tag", joinColumns = @JoinColumn(name = "feature_id"))
    private Set<String> tags;

    public List<Object> readValues() {
        List<String> valuesString;
        try {
            ORCReader orcReader = new ORCReader(dataset.getPath());
            valuesString = orcReader.readColumn(columnName);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        return dataType.convertStringList(valuesString);
    }

    public int getNullCount() {
        List<Object> valuesWithNull = readValues();
        List<Object> values = valuesWithNull.stream().filter(Objects::nonNull).toList();
        return valuesWithNull.size() - values.size();
    }

    public Map<String, Object> getStatistic() {
        List<Object> valuesWithNull = readValues();
        List<Object> values = valuesWithNull.stream().filter(Objects::nonNull).toList();

        Map<String, Object> result = new HashMap<>();
        switch (getFeatureType()) {
            case QUANTITATIVE:
                if (dataType == DataType.INT || dataType == DataType.FLOAT) {
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

    public Map<String, Object> getStatistic(StatisticRequestDto statisticRequestDto) {
        throw new NotImplementedException();
    }
}
