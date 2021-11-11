package eda.service;

import eda.domain.Feature;
import eda.domain.Statistic;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class StatisticsService {
    private final FeatureViewService featureViewService;
    private final Statistic statistic;

    public Optional<Map<String, Object>> getStatistic(String featureViewName, List<String> featureNames,
                                                       StatisticRequestDto statisticRequestDto) {
        List<Feature> features = new ArrayList<>();
        for (String featureName : featureNames) {
            Optional<Feature> featureOptional = featureViewService.getFeature(featureViewName, featureName);
            if (featureOptional.isEmpty())
                return Optional.empty();
            features.add(featureOptional.get());
        }

        if (statisticRequestDto == null) {
            statisticRequestDto = StatisticRequestDto.builder()
                    .name("basic")
                    .build();
        }

        try {
            if (features.size() == 1)
                return Optional.of(statistic.getStatisticFromEntity(features.get(0), statisticRequestDto));
            else
                return Optional.of(statistic.getStatistic(features, statisticRequestDto));
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
