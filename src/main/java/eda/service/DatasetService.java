package eda.service;

import eda.domain.*;
import eda.dto.DatasetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@RequiredArgsConstructor
@Service
public class DatasetService {
    private final DatasetRepository datasetRepository;
    private final Statistic statistic;

    public List<DatasetDto> getDatasetList() {
        return datasetRepository.findAll().stream()
                .map(DatasetDto::of)
                .toList();
    }

    public Optional<DatasetDto> getDataset(String datasetName) {
        return datasetRepository.findByName(datasetName)
                .map(DatasetDto::of);
    }

    public void createDatasetFromRemoteCSV(String datasetName, String url) {
        if (datasetRepository.findByName(datasetName).isPresent())
            throw new BadRequestException("Dataset '%s' already exists".formatted(datasetName));
        try {
            Dataset dataset = Dataset.createFromCSV(datasetName, url, new URL(url).openStream());
            datasetRepository.save(dataset);
        } catch (IOException e) {
            throw new BadRequestException("Cannot create dataset - " + e.toString());
        }
    }

    public Optional<Map<String, List<String>>> getDatasetExample(String datasetName, int count, boolean random) {
        Optional<Dataset> datasetOptional = datasetRepository.findByName(datasetName);
        if (datasetOptional.isEmpty()) return Optional.empty();
        Dataset dataset = datasetOptional.get();

        Map<String, List<String>> result = new HashMap<>();
        Integer randomSeed = random ? new Random().nextInt() : null;
        for (DatasetColumn column : dataset.getColumns()) {
            result.put(column.getName(), statistic.getExample(dataset, column.getName(), count, randomSeed));
        }
        return Optional.of(result);
    }
}
