package eda.domain;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import eda.domain.data.ORCWriter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class Dataset {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    private String source;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "dataset_id")
    private Set<DatasetColumn> columns = new HashSet<>();

    public void addColumn(String name, DataType dataType) {
        DatasetColumn datasetColumn = DatasetColumn.builder()
                .name(name)
                .dataType(dataType)
                .dataset(this)
                .build();
        columns.add(datasetColumn);
    }

    public static Dataset createFromCSV(String datasetName, String source, InputStream input) throws IOException {
        String newPath = "data/%s.orc".formatted(datasetName);
        ColumnData[] columns;
        try {
            columns = readColumnsFromCSV(input);
        } catch (RuntimeJsonMappingException e) {
            throw new IOException(e.toString());
        }

        ORCWriter writer = new ORCWriter(newPath);
        List<String> header = Arrays.stream(columns).map(ColumnData::name).toList();
        List<List<String>> columnData = Arrays.stream(columns).map(ColumnData::data).toList();
        writer.write(header, columnData);

        Dataset dataset = Dataset.builder()
                .name(datasetName)
                .path(newPath)
                .source(source)
                .build();
        for (ColumnData column : columns) {
            dataset.addColumn(column.name, column.dataType);
        }
        return dataset;
    }

    private static ColumnData[] readColumnsFromCSV(InputStream stream) throws IOException {
        CsvMapper mapper = new CsvMapper();
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(stream);

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

        ColumnData[] result = new ColumnData[columnCount];
        for (int i = 0; i < columnCount; i++) {
            result[i] = new ColumnData(header[i], data.get(i), DataType.inferType(data.get(i)));
        }
        return result;
    }

    private static record ColumnData(String name, List<String> data, DataType dataType) {}
}
