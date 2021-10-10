package eda.dto;

import eda.domain.Feature;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
public class FeatureDto {
    private Long id;
    private String name;
    private String dataset_name;
    private String column_name;
    private String data_type;
    private String feature_type;
    private Set<FeatureDto> children;
    private Set<String> tags;

    public static FeatureDto of(Feature entity) {
        return FeatureDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .dataset_name(entity.getDataset().getName())
                .column_name(entity.getColumnName())
                .data_type(entity.getDataType().name())
                .feature_type(entity.getFeatureType().name())
                .children(
                        entity.getChildren().stream()
                                .map(FeatureDto::of)
                                .collect(Collectors.toSet())
                )
                .tags(entity.getTags())
                .build();
    }
}
