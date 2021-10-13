package eda.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class GetStatisticsRequestDto {
    List<String> features;
    List<StatisticRequestDto> statistics;
}
