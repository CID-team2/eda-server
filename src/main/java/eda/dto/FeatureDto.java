package eda.dto;

import eda.domain.Feature;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Set;
import java.util.stream.Collectors;

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

    @NotBlank
    private final String data_type;

    @NotBlank
    private final String feature_type;

    @Valid
    private final Set<FeatureDto> children;

    @Valid
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

    public Set<FeatureDto> getChildren() {
        if (children == null)
            return Set.of();
        else return children;
    }

    public Set<String> getTags() {
        if (tags == null)
            return Set.of();
        else return tags;
    }
}
