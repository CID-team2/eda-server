package eda.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@ToString
@Builder
@Getter
public class StatisticRequestDto {
    @NotBlank
    String name;

    Map<String, Object> params;
}