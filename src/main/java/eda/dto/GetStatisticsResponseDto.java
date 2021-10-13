package eda.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class GetStatisticsResponseDto {
    int null_count;
    Map<String, Object> statistics;
}
