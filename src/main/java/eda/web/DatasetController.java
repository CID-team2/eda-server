package eda.web;

import eda.dto.DatasetDto;
import eda.service.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class DatasetController {
    private final DatasetService datasetService;
    @GetMapping("/api/v1/datasets")
    public List<String> getDatasetList() {
        return datasetService.getDatasetList().stream()
                .map(DatasetDto::getName)
                .toList();
    }

    @GetMapping("/api/v1/datasets/{datasetName}")
    public ResponseEntity<DatasetDto> getDataset(@PathVariable String datasetName) {
        return ResponseEntity.of(datasetService.getDataset(datasetName));
    }
}
