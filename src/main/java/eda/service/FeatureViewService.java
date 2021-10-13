package eda.service;

import eda.domain.*;
import eda.dto.FeatureDto;
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
    private final DatasetRepository datasetRepository;

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

    public void createFeatureView(FeatureViewDto featureViewDto) {
        if (featureViewRepository.findByName(featureViewDto.getName()).isPresent())
            throw new CannotCreateFeatureViewException("Feature view '%s' is already exist".formatted(featureViewDto.getName()));

        FeatureView featureView = convertFeatureViewDto(featureViewDto);
        featureViewRepository.save(featureView);
    }

    private FeatureView convertFeatureViewDto(FeatureViewDto featureViewDto) {
        return FeatureView.builder()
                .name(featureViewDto.getName())
                .features(featureViewDto.getFeatures().stream().map(this::convertFeatureDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    private Feature convertFeatureDto(FeatureDto featureDto) {
        return Feature.builder()
                .children(featureDto.getChildren().stream().map(this::convertFeatureDto)
                        .collect(Collectors.toSet()))
                .name(featureDto.getName())
                .dataset(datasetRepository.findByName(featureDto.getDataset_name())
                        .orElseThrow(() -> new CannotCreateFeatureViewException(
                                "Dataset '%s' is not exist".formatted(featureDto.getDataset_name()))))
                .columnName(featureDto.getColumn_name())
                .dataType(DataType.valueOf(featureDto.getData_type()))
                .featureType(FeatureType.valueOf(featureDto.getFeature_type()))
                .tags(featureDto.getTags())
                .build();
    }
}
