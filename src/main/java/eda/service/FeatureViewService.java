package eda.service;

import eda.domain.FeatureViewRepository;
import eda.dto.FeatureViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FeatureViewService {
    private final FeatureViewRepository featureViewRepository;

    public List<FeatureViewDto> getFeatureViewList() {
        return featureViewRepository.findAll().stream()
                .map(FeatureViewDto::of)
                .collect(Collectors.toList());
    }

    public Optional<FeatureViewDto> getFeatureView(String featureViewName) {
        return featureViewRepository.findByName(featureViewName)
                .map(FeatureViewDto::of);
    }
}
