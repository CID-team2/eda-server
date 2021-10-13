package eda.dto;

import eda.domain.DataType;
import eda.domain.Dataset;
import eda.domain.Feature;
import eda.domain.FeatureType;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Builder
@Getter
public class FeatureDto {
    private final Long id;
    private final String name;
    private final String dataset_name;
    private final String column_name;
    private final String data_type;
    private final String feature_type;
    private final Set<FeatureDto> children;
    private final Set<String> tags;

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
