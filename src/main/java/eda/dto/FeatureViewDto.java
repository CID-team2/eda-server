package eda.dto;

import eda.domain.FeatureView;
import lombok.Builder;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
public class FeatureViewDto {
    private final Long id;

    @NotBlank
    private final String name;

    @Valid
    @NotNull
    @Size(min = 1)
    private final Set<FeatureDto> features;

    public static FeatureViewDto of(FeatureView entity) {
        return FeatureViewDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .features(entity.getFeatures().stream()
                        .map(FeatureDto::of)
                        .collect(Collectors.toSet())
                )
                .build();
    }
}
