package eda.service;

import eda.domain.Feature;
import eda.dto.GetStatisticsRequestDto;
import eda.dto.GetStatisticsResponseDto;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class StatisticsService {
    private final FeatureViewService featureViewService;

    public Optional<GetStatisticsResponseDto> getStatistics(String featureViewName,
                                                            GetStatisticsRequestDto getStatisticsRequestDto) {
        String featureName = getStatisticsRequestDto.getFeatures().get(0);
        Optional<Feature> featureOptional = featureViewService.getFeature(featureViewName, featureName);
        if (featureOptional.isEmpty())
            return Optional.empty();
        Feature feature = featureOptional.get();

        List<StatisticRequestDto> statisticRequestDtos = getStatisticsRequestDto.getStatistics();

        int nullCount = feature.getNullCount();
        Map<String, Object> resultStatistics = new HashMap<>(Map.of("basic", feature.getStatistic()));
        for (StatisticRequestDto statisticRequestDto : statisticRequestDtos) {
            resultStatistics.put(statisticRequestDto.getName(), feature.getStatistic(statisticRequestDto));
        }

        return Optional.of(GetStatisticsResponseDto.builder()
                .null_count(nullCount)
                .statistics(resultStatistics)
                .build());
    }
}
