package eda.dto;

import eda.domain.FeatureView;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
public class FeatureViewDto {
    private Long id;
    private String name;
    private Set<FeatureDto> features;

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
