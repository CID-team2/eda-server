package eda.dto;

import eda.domain.DatasetColumn;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Getter
public class DatasetColumnDto {
    private final String name;
    private final String data_type;

    public static DatasetColumnDto of(DatasetColumn entity) {
        return DatasetColumnDto.builder()
                .name(entity.getName())
                .data_type(entity.getDataType() != null ? entity.getDataType().name() : "")
                .build();
    }
}