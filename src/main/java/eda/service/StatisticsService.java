package eda.service;

import eda.data.ORCReader;
import eda.data.StatisticsCalculator;
import eda.domain.Feature;
import eda.domain.FeatureViewRepository;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
//        if (statisticRequestDto == null) {
//            switch(feature.getFeatureType()) {
//                case QUANTITATIVE:
//                    break;
//                case ORDINAL:
//                    break;
//                case CATEGORICAL:
//                    break;
//                case CUSTOM:
//                    break;
//            }
//        }
        List<String> values;
        try {
            values = readValues(feature);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(statisticsCalculator.getBasicStatistics(values.stream()
                .filter(s -> {
                    try {
                        Integer.valueOf(s);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    return true;
                })
                .map(Integer::valueOf)
                .collect(Collectors.toList()))
        );
    }

    private List<String> readValues(Feature feature) throws IOException {
        ORCReader orcReader = new ORCReader(feature.getDataset().getPath());
        return orcReader.readColumn(feature.getColumnName());
    }
}
