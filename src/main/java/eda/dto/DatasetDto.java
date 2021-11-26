package eda.dto;

import eda.domain.Dataset;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Builder
@Getter
public class DatasetDto {
    private final long id;
    private final String name;
    private final String path;
    private final String source;
    private final List<DatasetColumnDto> columns;

    public static DatasetDto of(Dataset entity) {
        return DatasetDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .path(entity.getPath())
                .source(entity.getSource())
                .columns(entity.getColumns().stream()
                        .map(DatasetColumnDto::of)
                        .toList())
                .build();
    }
}
