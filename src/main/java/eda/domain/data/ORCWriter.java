package eda.domain.data;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ORCWriter {
    private String path;

    public ORCWriter(String path) {
        this.path = path;
    }

    public void write(List<String> header, List<List<String>> columns) throws IOException {
        Configuration conf = new Configuration();
        TypeDescription schema = TypeDescription.createStruct();
        for (String columnName : header) {
            schema.addField(columnName, TypeDescription.createString());
        }

        try (Writer writer = OrcFile.createWriter(new Path(path), OrcFile.writerOptions(conf).setSchema(schema))) {
            VectorizedRowBatch batch = schema.createRowBatch();
            for (int r = 0; r < columns.get(0).size(); r++) {
                int row = batch.size++;
                for (int i = 0; i < columns.size(); i++) {
                    BytesColumnVector bcv = (BytesColumnVector) batch.cols[i];
                    String value = columns.get(i).get(r);
                    bcv.vector[row] = value.getBytes(StandardCharsets.UTF_8);
                    bcv.start[row] = 0;
                    bcv.length[row] = value.length();
                }

                if (batch.size == batch.getMaxSize()) {
                    writer.addRowBatch(batch);
                    batch.reset();
                }
            }
            if (batch.size != 0) {
                writer.addRowBatch(batch);
                batch.reset();
            }
        }
    }

    public static void merge(String outputPath, List<String> header, List<String> inputFiles) throws IOException {
        Configuration conf = new Configuration();
        TypeDescription schema = TypeDescription.createStruct();
        for (String columnName : header) {
            schema.addField(columnName, TypeDescription.createString());
        }

        OrcFile.mergeFiles(new Path(outputPath),
                OrcFile.writerOptions(conf).setSchema(schema),
                inputFiles.stream().map(Path::new).toList());
    }
}
