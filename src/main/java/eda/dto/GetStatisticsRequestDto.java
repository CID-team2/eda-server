package eda.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@ToString
@Builder
@Getter
public class GetStatisticsRequestDto {
    @NotNull
    @Size(min = 1, max = 1)
    List<@NotEmpty String> features;

    @Valid
    @Size(max = 0)
    List<StatisticRequestDto> statistics;
}
