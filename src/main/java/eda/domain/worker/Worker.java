package eda.domain.worker;

import eda.domain.*;
import eda.dto.JobDto;
import eda.dto.StatisticRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "worker", name = "enabled", havingValue = "true")
@Component
public class Worker {
    @Value("${worker.api.hostname:http://localhost:8080}")
    private String host;
    @Value("${worker.api.uri:/api/v1/job}")
    private String uri;

    private final DatasetColumnRepository columnRepository;
    private final StatisticEntityRepository statisticEntityRepository;
    private final Statistic statistic;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedDelayString = "${worker.schedule.interval:60000}")
    public void fetchAndProcessJob() {
        try {
            while (true) {
                Optional<JobDto> fetched = fetchJob();
                if (fetched.isEmpty())
                    return;
                JobDto jobDto = fetched.get();
                Optional<DatasetColumn> columnOptional = columnRepository.findById(jobDto.getDataset_column_id());
                if (columnOptional.isEmpty())
                    continue;

                DatasetColumn column = columnOptional.get();
                processJob(column);

                Boolean success = reportJob(jobDto.getId());
                if (success != null && success)
                    log.info("Job finished: " + jobDto);
                else
                    log.error("Job report failed");
            }
        } catch (ResourceAccessException e) {
            log.error("Cannot access to server: " + e);
        }
    }

    private Optional<JobDto> fetchJob() {
        while (true) {
            // get job list
            ResponseEntity<List<JobDto>> getResponse = restTemplate.exchange(
                    host + uri, HttpMethod.GET,
                    null, new ParameterizedTypeReference<List<JobDto>>() {}
            );
            if (getResponse.getStatusCode() != HttpStatus.OK) {
                log.error("Get request failed: " + getResponse);
                return Optional.empty();
            }
            List<JobDto> jobList = getResponse.getBody();
            if (jobList == null || jobList.isEmpty())
                return Optional.empty();
            JobDto jobDto = jobList.get(0);
            Long jobId = jobDto.getId();

            // claim a job
            ResponseEntity<Boolean> putResponse = restTemplate.exchange(
                    "%s/%d".formatted(host + uri, jobId), HttpMethod.PUT,
                    null, Boolean.class
            );
            if (putResponse.getStatusCode() != HttpStatus.OK) {
                log.error("Put request failed: " + putResponse);
                return Optional.empty();
            }
            Boolean success = putResponse.getBody();
            if (success != null && success)
                return Optional.of(jobDto);
        }
    }

    private Boolean reportJob(long jobId) {
        // claim a job
        ResponseEntity<Boolean> putResponse = restTemplate.exchange(
                "%s/%d".formatted(host + uri, jobId), HttpMethod.DELETE,
                null, Boolean.class
        );
        if (putResponse.getStatusCode() != HttpStatus.OK) {
            log.error("Delete request failed: " + putResponse);
            return false;
        }
        return putResponse.getBody();
    }

    private void processJob(DatasetColumn column) {
        for (FeatureType featureType : FeatureType.values()) {
            for (Statistic.Kind kind : Statistic.Kind.values()) {
                boolean isForSingleFeature = kind.getType().contains(Statistic.Kind.Type.SINGLE);
                boolean isSupported = kind.supports(column.getDataType(), featureType);
                if (!(isForSingleFeature && isSupported))
                    continue;

                Map<String, Object> result = getStatistic(column, featureType, kind);
                saveStatistic(column, featureType, kind, result);
            }
        }
    }

    private Map<String, Object> getStatistic(DatasetColumn column, FeatureType featureType, Statistic.Kind kind) {
        Feature feature = Feature.builder()
                .name("worker_temp_feature")
                .dataset(column.getDataset())
                .column(column)
                .featureType(featureType)
                .build();
        StatisticRequestDto statisticRequestDto = StatisticRequestDto.builder()
                .name(kind.name())
                .build();
        return statistic.getStatistic(feature, statisticRequestDto);
    }

    private void saveStatistic(DatasetColumn column, FeatureType featureType, Statistic.Kind kind, Map<String, Object> value) {
        Optional<StatisticEntity> statisticEntityOptional = statisticEntityRepository.get(column, featureType, kind);
        StatisticEntity statisticEntity;
        if (statisticEntityOptional.isPresent()) {
            statisticEntity = statisticEntityOptional.get();
            statisticEntity.setValue(value);
        } else {
            statisticEntity = StatisticEntity.builder()
                    .column(column)
                    .featureType(featureType)
                    .kind(kind)
                    .value(value)
                    .build();
        }
        statisticEntityRepository.save(statisticEntity);
    }
}
