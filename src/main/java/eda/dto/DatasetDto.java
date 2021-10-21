package eda.dto;

import eda.domain.Dataset;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Builder
@Getter
public class DatasetDto {
    private final long id;
    private final String name;
    private final String path;
    private final String source;
    private final Set<DatasetColumnDto> columns;

    @RequiredArgsConstructor
    @Builder
    @Getter
    public static class DatasetColumnDto {
        private final String name;
        private final String data_type;

        public static DatasetColumnDto of(Dataset.DatasetColumn entity) {
            return DatasetColumnDto.builder()
                    .name(entity.getName())
                    .data_type(entity.getDataType().name())
                    .build();
        }
    }

    public static DatasetDto of(Dataset entity) {
        return DatasetDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .path(entity.getPath())
                .source(entity.getSource())
                .columns(entity.getColumns().stream()
                        .map(DatasetColumnDto::of)
                        .collect(Collectors.toSet()))
                .build();
    }
}
