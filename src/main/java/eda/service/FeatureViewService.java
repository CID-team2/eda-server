package eda.service;

import eda.domain.Feature;
import eda.domain.FeatureViewRepository;
import eda.dto.FeatureViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FeatureViewService {
    private final FeatureViewRepository featureViewRepository;

    public List<FeatureViewDto> getFeatureViewList() {
        return featureViewRepository.findAll().stream()
                .map(FeatureViewDto::of)
                .toList();
    }

    public Optional<FeatureViewDto> getFeatureView(String featureViewName) {
        return featureViewRepository.findByName(featureViewName)
                .map(FeatureViewDto::of);
    }

    public Optional<Feature> getFeature(String featureViewName, String featureName) {
        return featureViewRepository.findByName(featureViewName).flatMap(featureView ->
                featureView.getFeatures().stream()
                        .filter(feature1 -> feature1.getName().equals(featureName))
                        .findFirst()
        );
    }
}
