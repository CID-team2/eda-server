package eda.service;

import eda.domain.Feature;
import eda.domain.Statistic;
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
    private final Statistic statistic;

    public Optional<GetStatisticsResponseDto> getStatistics(String featureViewName,
                                                            GetStatisticsRequestDto getStatisticsRequestDto) {
        String featureName = getStatisticsRequestDto.getFeatures().get(0);
        Optional<Feature> featureOptional = featureViewService.getFeature(featureViewName, featureName);
        if (featureOptional.isEmpty())
            return Optional.empty();
        Feature feature = featureOptional.get();

        List<StatisticRequestDto> statisticRequestDtos = getStatisticsRequestDto.getStatistics();

        int nullCount = statistic.getNullCount(feature);
        Map<String, Object> resultStatistics = new HashMap<>(Map.of("basic", statistic.getStatistic(feature)));
        for (StatisticRequestDto statisticRequestDto : statisticRequestDtos) {
            Map<String, Object> result = statistic.getStatistic(feature, statisticRequestDto);
            if (result != null) {
                resultStatistics.put(statisticRequestDto.getName(), result);
            }
        }

        return Optional.of(GetStatisticsResponseDto.builder()
                .null_count(nullCount)
                .statistics(resultStatistics)
                .build());
    }
}
