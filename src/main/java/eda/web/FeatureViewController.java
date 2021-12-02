package eda.web;

import eda.dto.FeatureViewDto;
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
    public FeatureViewDto createFeatureView(@RequestBody @Valid FeatureViewDto featureViewDto) {
        return featureViewService.createFeatureView(featureViewDto);
    }

    @GetMapping("/{featureViewName}")
    public ResponseEntity<FeatureViewDto> getFeatureView(@PathVariable String featureViewName) {
        return ResponseEntity.of(featureViewService.getFeatureView(featureViewName));
    }

    @PutMapping("/{featureViewName}")
    public FeatureViewDto updateFeatureView(@PathVariable String featureViewName,
                                            @RequestBody @Valid FeatureViewDto featureViewDto) {
        return featureViewService.updateFeatureView(featureViewName, featureViewDto);
    }

    @DeleteMapping("/{featureViewName}")
    public ResponseEntity<Void> deleteFeatureView(@PathVariable String featureViewName) {
        boolean result = featureViewService.deleteFeatureView(featureViewName);
        if (result)
            return ResponseEntity.noContent().build();
        else
            return ResponseEntity.notFound().build();
    }

    @GetMapping("/{featureViewName}/example")
    public ResponseEntity<Map<String, List<String>>> getFeatureViewExample(@PathVariable String featureViewName,
                                                                           @RequestParam(required = false) String[] feature,
                                                                           @RequestParam(required = false) Integer count,
                                                                           @RequestParam(required = false) Boolean random) {
        return ResponseEntity.of(featureViewService.getFeatureViewExample(featureViewName,
                feature,
                count != null ? count : 10,
                random != null ? random : false
                ));
    }

    @GetMapping("/{featureViewName}/statistic")
    public ResponseEntity<Map<String, Object>> getStatistic(@PathVariable String featureViewName,
                                                            @RequestParam List<String> feature,
                                                            @RequestParam(required = false) String statistic,
                                                            HttpServletRequest req) {
        Map<String, String[]> params = req.getParameterMap();
        params = new HashMap<>(params);     // make params modifiable
        params.remove("feature");
        params.remove("statistic");

        // build getStatisticsRequestDto
        StatisticRequestDto statisticRequestDto;
        if (statistic == null) {
            statisticRequestDto = null;
        } else {
            Map<String, Object> convertedParams = new HashMap<>();
            params.forEach((k, v) -> convertedParams.put(k, v[0]));
            statisticRequestDto = StatisticRequestDto.builder()
                    .name(statistic)
                    .params(convertedParams)
                    .build();
        }

        return ResponseEntity.of(statisticsService.getStatistic(featureViewName, feature, statisticRequestDto));
    }

    @GetMapping("/{featureViewName}/statistic/check")
    public ResponseEntity<Boolean> checkStatistic(@PathVariable String featureViewName,
                                                  @RequestParam List<String> feature,
                                                  @RequestParam String statistic) {
        return ResponseEntity.of(statisticsService.checkStatistic(featureViewName, feature, statistic));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class, UnsupportedOperationException.class})
    public Map<String, String> badRequest(Exception e) {
        Map<String, String> errorAttributes = new HashMap<>();
        errorAttributes.put("message", e.getMessage());
        return errorAttributes;
    }
}
