package eda.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
@Getter
public class StatisticRequestDto {
    String name;
    Map<String, Object> params;
}