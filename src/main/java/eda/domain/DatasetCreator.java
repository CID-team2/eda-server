package eda.domain;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import eda.domain.data.ORCWriter;
import lombok.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Component
public class DatasetCreator {
    private final DatasetRepository datasetRepository;

    public void createDatasetFromCSV(String datasetName, String path) throws IOException {
        String newPath = "data/%s.orc".formatted(datasetName);
        Column[] columns = readColumnsFromCSV(path);

        ORCWriter writer = new ORCWriter(newPath);
        List<String> header = Arrays.stream(columns).map(Column::getName).toList();
        List<List<String>> columnData = Arrays.stream(columns).map(Column::getData).toList();
        writer.write(header, columnData);

        Dataset dataset = Dataset.builder()
                .name(datasetName)
                .path(newPath)
                .source(path)
                .build();
        for (Column column : columns) {
            dataset.addColumn(column.name, column.dataType);
        }
        datasetRepository.save(dataset);
    }

    private Column[] readColumnsFromCSV(String path) throws IOException {
        File csvFile = new File(path);
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(csvFile);

        // read header and set data list
        String[] header = it.next();
        int columnCount = header.length;
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            data.add(new LinkedList<>());
        }

        // read data
        String[] line;
        while (it.hasNext()) {
            line = it.next();
            assert (line.length == columnCount);

            for (int i = 0; i < line.length; i++) {
                data.get(i).add(line[i]);
            }
        }

        Column[] result = new Column[columnCount];
        for (int i = 0; i < columnCount; i++) {
            result[i] = Column.builder()
                    .name(header[i])
                    .data(data.get(i))
                    .dataType(inferType(data.get(i)))
                    .build();
        }
        return result;
    }

    private DataType inferType(List<String> data) {
        long count = data.size();
        long nonnullCount = DataType.STRING.convertStringList(data).size();
        if (nonnullCount == 0)
            return DataType.STRING;

        for (DataType dataType : List.of(DataType.BOOL, DataType.DATE, DataType.INT, DataType.FLOAT)) {
            List<Object> dataConverted = dataType.convertStringList(data);
            long nullCount = dataConverted.stream().filter(Objects::isNull).count();
            if ((double) (count - nullCount) / nonnullCount > 0.8)
                return dataType;
        }
        return DataType.STRING;
    }

    @Builder
    @Data
    private static class Column {
        String name;
        List<String> data;
        DataType dataType;
    }
}
