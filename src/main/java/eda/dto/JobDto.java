package eda.dto;

import eda.domain.worker.Job;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
public class JobDto {
    Long id;
    Long dataset_column_id;

    public static JobDto of(Job entity) {
        return JobDto.builder()
                .id(entity.getId())
                .dataset_column_id(entity.getColumn().getId())
                .build();
    }
}
