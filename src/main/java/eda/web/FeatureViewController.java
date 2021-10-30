package eda.web;

import eda.dto.FeatureViewDto;
import eda.dto.GetStatisticsRequestDto;
import eda.dto.GetStatisticsResponseDto;
import eda.dto.StatisticRequestDto;
import eda.service.BadRequestException;
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

    @PostMapping
    public void createFeatureView(@RequestBody @Valid FeatureViewDto featureViewDto) {
        featureViewService.createFeatureView(featureViewDto);
    }

    @GetMapping("/{featureViewName}")
    public ResponseEntity<FeatureViewDto> getFeatureView(@PathVariable String featureViewName) {
        return ResponseEntity.of(featureViewService.getFeatureView(featureViewName));
    }

    @DeleteMapping("/{featureViewName}")
    public ResponseEntity<Void> deleteFeatureView(@PathVariable String featureViewName) {
        boolean result = featureViewService.deleteFeatureView(featureViewName);
        if (result)
            return ResponseEntity.ok(null);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{featureViewName}/example")
    public ResponseEntity<Map<String, List<String>>> getFeatureViewExample(@PathVariable String featureViewName,
                                                                           @RequestParam(required = false) Integer count,
                                                                           @RequestParam(required = false) Boolean random) {
        return ResponseEntity.of(featureViewService.getFeatureViewExample(featureViewName,
                count != null ? count : 10,
                random != null ? random : false
                ));
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class, UnsupportedOperationException.class})
    public Map<String, String> badRequest(Exception e) {
        Map<String, String> errorAttributes = new HashMap<>();
        errorAttributes.put("message", e.getMessage());
        return errorAttributes;
    }

}
