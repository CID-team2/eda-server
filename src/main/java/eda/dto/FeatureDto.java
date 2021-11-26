package eda.dto;

import eda.domain.Feature;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Builder
@Getter
public class FeatureDto {
    private final Long id;

    @NotBlank
    private final String name;

    @NotBlank
    private final String dataset_name;

    @NotBlank
    private final String column_name;

    private final String data_type;

    @NotBlank
    private final String feature_type;

    @Valid
    private final List<FeatureDto> children;

    @Valid
    private final List<String> tags;

    private final String description;

    private final LocalDateTime created_at;
    private final LocalDateTime modified_at;

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
                                .toList()
                )
                .tags(entity.getTags())
                .description(entity.getDescription())
                .created_at(entity.getCreatedAt())
                .modified_at(entity.getModifiedAt())
                .build();
    }

    public List<FeatureDto> getChildren() {
        if (children == null)
            return List.of();
        else return children;
    }

    public List<String> getTags() {
        if (tags == null)
            return List.of();
        else return tags;
    }
}
