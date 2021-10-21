package eda.service;

import eda.domain.DatasetRepository;
import eda.dto.DatasetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DatasetService {
    private final DatasetRepository datasetRepository;

    public List<DatasetDto> getDatasetList() {
        return datasetRepository.findAll().stream()
                .map(DatasetDto::of)
                .toList();
    }

    public Optional<DatasetDto> getDataset(String datasetName) {
        return datasetRepository.findByName(datasetName)
                .map(DatasetDto::of);
    }
}
