package eda.web;

import eda.dto.DatasetDto;
import eda.service.BadRequestException;
import eda.service.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/datasets")
@RestController
public class DatasetController {
    private final DatasetService datasetService;
    @GetMapping
    public List<String> getDatasetList() {
        return datasetService.getDatasetList().stream()
                .map(DatasetDto::getName)
                .toList();
    }

    @GetMapping("/{datasetName}")
    public ResponseEntity<DatasetDto> getDataset(@PathVariable String datasetName) {
        return ResponseEntity.of(datasetService.getDataset(datasetName));
    }

    @GetMapping("/{datasetName}/example")
    public ResponseEntity<Map<String, List<String>>> getDatasetExample(@PathVariable String datasetName,
                                                                       @RequestParam(required = false) Integer count,
                                                                       @RequestParam(required = false) Boolean random) {
        return ResponseEntity.of(datasetService.getDatasetExample(datasetName,
                count != null ? count : 10,
                random != null ? random : false
                ));
    }

    @PostMapping
    public void createDatasetFromRemoteCSV(@RequestParam String name, @RequestParam String url) {
        datasetService.createDatasetFromRemoteCSV(name, url);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class})
    public Map<String, String> badRequest(Exception e) {
        Map<String, String> errorAttributes = new HashMap<>();
        errorAttributes.put("message", e.getMessage());
        return errorAttributes;
    }
}
