package eda.dto;

import eda.domain.Dataset;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
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
    private final long num_records;
    private final LocalDateTime created_at;
    private final LocalDateTime modified_at;

    public static DatasetDto of(Dataset entity) {
        return DatasetDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .path(entity.getPath())
                .source(entity.getSource())
                .columns(entity.getColumns().stream()
                        .map(DatasetColumnDto::of)
                        .toList())
                .num_records(entity.getNumRecords())
                .created_at(entity.getCreatedAt())
                .modified_at(entity.getModifiedAt())
                .build();
    }
}
