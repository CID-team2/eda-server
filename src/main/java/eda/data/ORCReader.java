package eda.data;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.apache.hadoop.hive.ql.io.orc.Reader;
import org.apache.hadoop.hive.ql.io.orc.RecordReader;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.io.BytesWritable;
import org.apache.orc.OrcProto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

// this reader does NOT work for nested types
public class ORCReader {
    private Reader reader;

    // resource_path: relative path to resources/ directory
    public ORCReader(String resourcePath) throws IOException {
        Configuration conf = new Configuration();

        ClassPathResource resource = new ClassPathResource(resourcePath);
        reader = OrcFile.createReader(new Path(resource.getFile().getPath()), OrcFile.readerOptions(conf));
    }

    // returns List of Pair<FieldName, TypeName>
    public List<Pair<String, String>> readSchema() {
        List<String> fieldNames = reader.getFileTail().getFooter().getTypesList().get(0).getFieldNamesList();
        List<String> typeNames = reader.getFileTail().getFooter().getTypesList().stream()
                .skip(1)
                .map(OrcProto.Type::getKind)
                .map(OrcProto.Type.Kind::toString)
                .toList();

        return IntStream.range(0, fieldNames.size())
                .mapToObj(i -> Pair.of(fieldNames.get(i), typeNames.get(i)))
                .toList();
    }

    public List<String> readColumn(String columnName) throws IOException {
        List<String> result = new ArrayList<>();
        RecordReader records = reader.rows();
        StructObjectInspector inspector = (StructObjectInspector)reader.getObjectInspector();
        StructField structField = inspector.getStructFieldRef(columnName);
        Object row = null;
        while(records.hasNext())
        {
            row = records.next(row);
            Object o = inspector.getStructFieldData(row, structField);
            String value = o instanceof BytesWritable bytesWritable ? new String(bytesWritable.copyBytes()) : o.toString();
            result.add(value);
        }

        return result;
    }
}
