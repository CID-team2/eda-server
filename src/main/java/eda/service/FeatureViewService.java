package eda.service;

import eda.domain.*;
import eda.dto.FeatureDto;
import eda.dto.FeatureViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
public class FeatureViewService {
    private final FeatureViewRepository featureViewRepository;
    private final DatasetRepository datasetRepository;
    private final DatasetColumnRepository datasetColumnRepository;
    private final Statistic statistic;

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

    public FeatureViewDto createFeatureView(FeatureViewDto featureViewDto) {
        if (featureViewRepository.findByName(featureViewDto.getName()).isPresent())
            throw new BadRequestException("Feature view '%s' is already exist".formatted(featureViewDto.getName()));

        FeatureView featureView = convertFeatureViewDto(featureViewDto);
        if (!featureView.isValid())
            throw new BadRequestException("FeatureViewDTO is not valid");
        featureView = featureViewRepository.save(featureView);
        return FeatureViewDto.of(featureView);
    }

    @Transactional
    public FeatureViewDto updateFeatureView(String featureViewName, FeatureViewDto featureViewDto) {
        Optional<FeatureView> featureViewOptional = featureViewRepository.findByName(featureViewName);
        if (featureViewOptional.isEmpty()) {
            if (!featureViewName.equals(featureViewDto.getName()))
                throw new BadRequestException("Names in path(%s) and DTO(%s) should be equal"
                        .formatted(featureViewName, featureViewDto.getName()));
            return createFeatureView(featureViewDto);
        } else {
            FeatureView featureView = featureViewOptional.get();
            if (!featureView.isValid())
                throw new BadRequestException("FeatureViewDTO is not valid");
            featureView.update(convertFeatureViewDto(featureViewDto));
            featureView = featureViewRepository.save(featureView);
            return FeatureViewDto.of(featureView);
        }
    }

    @Transactional
    public boolean deleteFeatureView(String featureViewName) {
        Optional<FeatureView> featureViewOptional = featureViewRepository.findByName(featureViewName);
        if (featureViewOptional.isEmpty())
            return false;
        FeatureView featureView = featureViewOptional.get();
        featureViewRepository.delete(featureView);
        return true;
    }

    public Optional<Map<String, List<String>>> getFeatureViewExample(String featureViewName, String[] featureNames,
                                                                     int count, boolean random) {
        Optional<FeatureView> featureViewOptional = featureViewRepository.findByName(featureViewName);
        if (featureViewOptional.isEmpty())
            return Optional.empty();
        FeatureView featureView = featureViewOptional.get();

        List<Feature> targetFeatures;
        if (featureNames != null) {
            targetFeatures = new LinkedList<>();
            for (String featureName : featureNames) {
                Optional<Feature> featureOptional = getFeature(featureViewName, featureName);
                if (featureOptional.isEmpty())
                    return Optional.empty();
                targetFeatures.add(featureOptional.get());
            }
        } else {
            targetFeatures = featureView.getFeatures();
        }

        Map<String, List<String>> result = new HashMap<>();
        Integer randomSeed = random ? new Random().nextInt() : null;
        for (Feature feature : targetFeatures) {
            result.put(feature.getColumnName(), statistic.getExample(feature, count, randomSeed));
        }
        return Optional.of(result);
    }

    private FeatureView convertFeatureViewDto(FeatureViewDto featureViewDto) {
        return FeatureView.builder()
                .name(featureViewDto.getName())
                .features(featureViewDto.getFeatures().stream().map(this::convertFeatureDto)
                        .toList())
                .build();
    }

    private Feature convertFeatureDto(FeatureDto featureDto) {
        final String datasetName = featureDto.getDataset_name();
        final String columnName = featureDto.getColumn_name();
        final Dataset dataset = datasetRepository.findByName(datasetName)
                .orElseThrow(() -> new BadRequestException(
                        "Dataset '%s' does not exist".formatted(datasetName)));
        final DatasetColumn datasetColumn = datasetColumnRepository.findByDatasetAndColumnName(datasetName, columnName)
                .orElseThrow(() -> new BadRequestException(
                        "DatasetColumn '%s' in Dataset '%s' does not exist".formatted(datasetName, columnName)));

        return Feature.builder()
                .children(featureDto.getChildren().stream().map(this::convertFeatureDto)
                        .toList())
                .name(featureDto.getName())
                .dataset(dataset)
                .column(datasetColumn)
                .featureType(FeatureType.valueOf(featureDto.getFeature_type()))
                .tags(featureDto.getTags())
                .description(featureDto.getDescription())
                .build();
    }
}
