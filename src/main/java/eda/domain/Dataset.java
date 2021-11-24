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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class Dataset extends BaseTimeEntity {
    private static final String DATA_PATH = "data";

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

    @Version
    private Long version;

    public void addColumn(String name, DataType dataType) {
        DatasetColumn datasetColumn = DatasetColumn.builder()
                .name(name)
                .dataType(dataType)
                .dataset(this)
                .build();
        columns.add(datasetColumn);
    }

    /**
     * Create a Dataset from CSV InputStream
     *
     * @param datasetName Name of a dataset
     * @param input InputStream for data
     * @param source Source of the InputStream (optional)
     * @param additionalColumns Names of additional columns to distinguish current data from the data to be added later (optional)
     * @param additionalValues Column values w.r.t. additionalColumns (sizes should be equal). All rows will have the same value (optional)
     * @return created dataset
     */
    public static Dataset createFromCSV(String datasetName, InputStream input, String source,
                                        String[] additionalColumns, String[] additionalValues) throws IOException {
        long time = System.currentTimeMillis();
        String path = "%s/%s_%d.orc".formatted(DATA_PATH, datasetName, time);
        List<ColumnData> columns = writeWithCSV(path, input, additionalColumns, additionalValues);

        Dataset dataset = Dataset.builder()
                .name(datasetName)
                .path(path)
                .source(source)
                .build();
        for (ColumnData column : columns) {
            dataset.addColumn(column.name, column.dataType);
        }
        return dataset;
    }

    /**
     * Add data to this with given CSV InputStream
     *
     * @param input InputStream for data
     * @param source Source of the InputStream (optional)
     * @param additionalColumns Names of additional columns to distinguish current data from the other data (optional)
     * @param additionalValues Column values w.r.t. additionalColumns (sizes should be equal). All rows will have the same value (optional)
     */
    public void updateWithCSV(InputStream input, String source, String[] additionalColumns, String[] additionalValues) throws IOException {
        long time = System.currentTimeMillis();
        String newFilePath = "%s/%s_%d.orc".formatted(DATA_PATH, name, time);
        String tempFilePath = "%s/%s_%d_temp.orc".formatted(DATA_PATH, name, time);
        String oldFilePath = this.path;

        List<ColumnData> columns = writeWithCSV(tempFilePath, input, additionalColumns, additionalValues);
        List<String> header = columns.stream().map(ColumnData::name).toList();
        ORCWriter.merge(newFilePath, header, List.of(oldFilePath, tempFilePath));
        this.path = newFilePath;
        this.source = this.source + ";" + source;

        Files.delete(Path.of(oldFilePath));
        Files.delete(Path.of(tempFilePath));
    }

    private static List<ColumnData> writeWithCSV(String path, InputStream input,
                                                 String[] additionalColumns,
                                                 String[] additionalValues) throws IOException {
        // read csv stream
        ColumnData[] columns;
        try {
            columns = readColumnsFromCSV(input);
        } catch (RuntimeJsonMappingException e) {
            throw new IOException(e.toString());
        }
        List<ColumnData> columnsList = new LinkedList<>(Arrays.asList(columns));

        // append additionalColumns
        final int count = columns[0].data.size();
        if (additionalColumns != null && additionalColumns.length > 0) {
            for (int i = 0; i < additionalColumns.length; i++) {
                String columnName = additionalColumns[i];
                String value = additionalValues[i];
                List<String> data = Collections.nCopies(count, value);
                ColumnData column = new ColumnData(columnName, data, DataType.inferType(List.of(value)));
                columnsList.add(column);
            }
        }

        // write orc file
        ORCWriter writer = new ORCWriter(path);
        List<String> header = columnsList.stream().map(ColumnData::name).toList();
        List<List<String>> columnData = columnsList.stream().map(ColumnData::data).toList();
        writer.write(header, columnData);

        return columnsList;
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
