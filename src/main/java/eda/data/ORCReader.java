package eda.data;

import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.apache.hadoop.hive.ql.io.orc.OrcStruct;
import org.apache.hadoop.hive.ql.io.orc.Reader;
import org.apache.hadoop.hive.ql.io.orc.RecordReader;
import org.apache.hadoop.hive.serde2.objectinspector.StandardStructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.orc.OrcProto;
import org.apache.orc.Reader.Options;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// this reader does NOT work for nested types
public class ORCReader {
    private Reader reader;

    // resource_path: relative path to resources/ directory
    public ORCReader(String resource_path) throws IOException {
        Configuration conf = new Configuration();

        ClassPathResource resource = new ClassPathResource(resource_path);
        reader = OrcFile.createReader(new Path(resource.getFile().getPath()), OrcFile.readerOptions(conf));
    }

    // returns List of Pair<FieldName, TypeName>
    public List<Pair<String, String>> readSchema() throws IOException {
        List<String> fieldNames = reader.getFileTail().getFooter().getTypesList().get(0).getFieldNamesList();
        List<String> typeNames = reader.getFileTail().getFooter().getTypesList().stream()
                .skip(1)
                .map(OrcProto.Type::getKind)
                .map(OrcProto.Type.Kind::toString)
                .collect(Collectors.toList());

        return IntStream.range(0, fieldNames.size())
                .mapToObj(i -> Pair.of(fieldNames.get(i), typeNames.get(i)))
                .collect(Collectors.toList());
    }

    public List<Object> readColumn(int columnIndex) throws IOException {
        int numFields = readSchema().size();
        boolean[] includes = new boolean[numFields + 1];
        includes[columnIndex + 1] = true;
        Options options = new Options().include(includes);
        RecordReader records = reader.rowsOptions(options);

        List<Object> result = new ArrayList<>();
        StructObjectInspector inspector = (StructObjectInspector)reader.getObjectInspector();
        StructField structField = inspector.getAllStructFieldRefs().get(columnIndex);
        Object row = null;
        while(records.hasNext())
        {
            row = records.next(row);
            Object o = inspector.getStructFieldData(row, structField);
            String value = o instanceof BytesWritable ? new String(((BytesWritable) o).copyBytes()) : o.toString();
            result.add(value);
        }

        return result;
    }
}
