package eda.dto;

import eda.domain.FeatureView;
import lombok.Builder;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class FeatureViewDto {
    private final Long id;

    @NotBlank
    private final String name;

    @Valid
    @NotNull
    @Size(min = 1)
    private final List<FeatureDto> features;

    private final LocalDateTime created_at;
    private final LocalDateTime modified_at;

    public static FeatureViewDto of(FeatureView entity) {
        return FeatureViewDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .features(entity.getFeatures().stream()
                        .map(FeatureDto::of)
                        .toList()
                )
                .created_at(entity.getCreatedAt())
                .modified_at(entity.getModifiedAt())
                .build();
    }
}
