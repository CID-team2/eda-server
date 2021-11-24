package eda.service;

import eda.domain.*;
import eda.dto.DatasetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

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

    public DatasetDto createDatasetFromRemoteCSV(String datasetName, String url, String fileFormat,
                                                 String[] additionalColumns, String[] additionalValues) {
        if (datasetRepository.findByName(datasetName).isPresent())
            throw new BadRequestException("Dataset '%s' already exists".formatted(datasetName));
        try {
            InputStream stream = streamFileFormat(new URL(url).openStream(), fileFormat);
            Dataset dataset = Dataset.createFromCSV(datasetName, stream, url, additionalColumns, additionalValues);
            datasetRepository.save(dataset);
            return DatasetDto.of(dataset);
        } catch (IOException e) {
            throw new BadRequestException("Cannot create dataset - " + e.toString());
        }
    }

    public Optional<DatasetDto> updateDatasetFromRemoteCSV(String datasetName, String url, String fileFormat,
                                                           String[] additionalColumns, String[] additionalValues) {
        Optional<Dataset> datasetOptional = datasetRepository.findByName(datasetName);
        if (datasetOptional.isEmpty())
            return Optional.empty();
        Dataset dataset = datasetOptional.get();
        try {
            InputStream stream = streamFileFormat(new URL(url).openStream(), fileFormat);
            dataset.updateWithCSV(stream, url, additionalColumns, additionalValues);
            datasetRepository.save(dataset);
        } catch (IOException e) {
            throw new BadRequestException("Cannot update dataset - " + e.toString());
        }
        return Optional.of(DatasetDto.of(dataset));
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

    private InputStream streamFileFormat(InputStream stream, String fileFormat) throws IOException {
        if (fileFormat == null)
            return stream;
        else if (fileFormat.equalsIgnoreCase("gz"))
            return new GZIPInputStream(stream);
        else
            throw new BadRequestException("Unsupported file format: %s".formatted(fileFormat));
    }
}
