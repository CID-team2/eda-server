package eda.web;

import eda.dto.FeatureViewDto;
import eda.service.FeatureViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class FeatureViewController {
    private final FeatureViewService featureViewService;

    @GetMapping("/api/v1/feature-views")
    public List<String> getFeatureViewList() {
        return featureViewService.getFeatureViewList().stream()
                .map(FeatureViewDto::getName)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v1/feature-views/{featureViewName}")
    public ResponseEntity<FeatureViewDto> getFeatureView(@PathVariable String featureViewName) {
        return ResponseEntity.of(featureViewService.getFeatureView(featureViewName));
    }


}
