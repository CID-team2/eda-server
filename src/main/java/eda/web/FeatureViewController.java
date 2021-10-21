package eda.web;

import eda.dto.FeatureViewDto;
import eda.dto.GetStatisticsRequestDto;
import eda.dto.GetStatisticsResponseDto;
import eda.dto.StatisticRequestDto;
import eda.service.CannotCreateFeatureViewException;
import eda.service.FeatureViewService;
import eda.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/feature-views")
@RestController
public class FeatureViewController {
    private final FeatureViewService featureViewService;
    private final StatisticsService statisticsService;

    @GetMapping
    public List<String> getFeatureViewList() {
        return featureViewService.getFeatureViewList().stream()
                .map(FeatureViewDto::getName)
                .toList();
    }

    @GetMapping("/{featureViewName}")
    public ResponseEntity<FeatureViewDto> getFeatureView(@PathVariable String featureViewName) {
        return ResponseEntity.of(featureViewService.getFeatureView(featureViewName));
    }

    @GetMapping("/{featureViewName}/statistics")
    public ResponseEntity<GetStatisticsResponseDto> getStatistics(@PathVariable String featureViewName,
                                                                  @RequestBody @Valid GetStatisticsRequestDto getStatisticsRequestDto) {
        return ResponseEntity.of(statisticsService.getStatistics(featureViewName, getStatisticsRequestDto));
    }

    @GetMapping("/{featureViewName}/statistic")
    public ResponseEntity<GetStatisticsResponseDto> getStatistic(@PathVariable String featureViewName,
                                                                 @RequestParam String feature,
                                                                 @RequestParam(required = false) String statistic,
                                                                 HttpServletRequest req) {
        Map<String, String[]> params = req.getParameterMap();
        params = new HashMap<>(params);     // make params modifiable
        params.remove("feature");
        params.remove("statistic");

        // build getStatisticsRequestDto
        List<StatisticRequestDto> statistics;
        if (statistic == null) {
            statistics = List.of();
        } else {
            Map<String, Object> convertedParams = new HashMap<>();
            params.forEach((k, v) -> convertedParams.put(k, v[0]));
            statistics = List.of(StatisticRequestDto.builder()
                    .name(statistic)
                    .params(convertedParams)
                    .build());
        }
        GetStatisticsRequestDto getStatisticsRequestDto = GetStatisticsRequestDto.builder()
                .features(List.of(feature))
                .statistics(statistics)
                .build();

        return ResponseEntity.of(statisticsService.getStatistics(featureViewName, getStatisticsRequestDto));
    }

    @PostMapping
    public void createFeatureView(@RequestBody @Valid FeatureViewDto featureViewDto) {
        featureViewService.createFeatureView(featureViewDto);
    }

    @DeleteMapping("/{featureViewName}")
    public ResponseEntity<Void> deleteFeatureView(@PathVariable String featureViewName) {
        boolean result = featureViewService.deleteFeatureView(featureViewName);
        if (result)
            return ResponseEntity.ok(null);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CannotCreateFeatureViewException.class)
    public Map<String, String> cannotCreateFeatureView(CannotCreateFeatureViewException e) {
        Map<String, String> errorAttributes = new HashMap<>();
        errorAttributes.put("message", e.getMessage());
        return errorAttributes;
    }
}
