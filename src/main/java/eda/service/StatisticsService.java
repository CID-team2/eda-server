package eda.service;

import eda.domain.Feature;
import eda.domain.FeatureView;
import eda.domain.FeatureViewRepository;
import eda.domain.Statistic;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class StatisticsService {
    private final FeatureViewService featureViewService;
    private final FeatureViewRepository featureViewRepository;
    private final Statistic statistic;

    public Optional<Map<String, Object>> getStatistic(String featureViewName, List<String> featureNames,
                                                       StatisticRequestDto statisticRequestDto) {
        List<Feature> features;
        if (featureNames == null || featureNames.isEmpty()) {
            Optional<FeatureView> featureViewOptional = featureViewRepository.findByName(featureViewName);
            if (featureViewOptional.isEmpty())
                return Optional.empty();
            features = featureViewOptional.get().getFeatures();
        } else {
            features = new LinkedList<>();
            for (String featureName : featureNames) {
                Optional<Feature> featureOptional = featureViewService.getFeature(featureViewName, featureName);
                if (featureOptional.isEmpty())
                    return Optional.empty();
                features.add(featureOptional.get());
            }
        }

        if (statisticRequestDto == null) {
            statisticRequestDto = StatisticRequestDto.builder()
                    .name("basic")
                    .build();
        }

        try {
            return Optional.of(statistic.getStatisticFromEntity(features, statisticRequestDto));
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public Optional<Boolean> checkStatistic(String featureViewName, List<String> featureNames, String statisticName) {
        List<Feature> features = new ArrayList<>();
        for (String featureName : featureNames) {
            Optional<Feature> featureOptional = featureViewService.getFeature(featureViewName, featureName);
            if (featureOptional.isEmpty())
                return Optional.empty();
            features.add(featureOptional.get());
        }

        try {
            statistic.checkValidRequest(features, statisticName);
        } catch (UnsupportedOperationException e) {
            return Optional.of(false);
        }
        return Optional.of(true);
    }
}
