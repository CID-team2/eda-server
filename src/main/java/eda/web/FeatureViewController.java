package eda.web;

import eda.dto.FeatureViewDto;
import eda.dto.GetStatisticsRequestDto;
import eda.dto.GetStatisticsResponseDto;
import eda.service.FeatureViewService;
import eda.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class FeatureViewController {
    private final FeatureViewService featureViewService;
    private final StatisticsService statisticsService;

    @GetMapping("/api/v1/feature-views")
    public List<String> getFeatureViewList() {
        return featureViewService.getFeatureViewList().stream()
                .map(FeatureViewDto::getName)
                .toList();
    }

    @GetMapping("/api/v1/feature-views/{featureViewName}")
    public ResponseEntity<FeatureViewDto> getFeatureView(@PathVariable String featureViewName) {
        return ResponseEntity.of(featureViewService.getFeatureView(featureViewName));
    }

    @GetMapping("/api/v1/feature-views/{featureViewName}/statistics")
    public ResponseEntity<GetStatisticsResponseDto> getStatistics(@PathVariable String featureViewName,
                                                                  @RequestBody GetStatisticsRequestDto getStatisticsRequestDto) {
        Optional<Map<String, Object>> basicStatistic = statisticsService.getStatistic(featureViewName,
                getStatisticsRequestDto.getFeatures().get(0),
                null);
        return ResponseEntity.of(basicStatistic.map(stringObjectMap ->
                GetStatisticsResponseDto.builder()
                        .null_count(0)
                        .statistics(Map.of("basic", stringObjectMap))
                        .build()));
    }


}
