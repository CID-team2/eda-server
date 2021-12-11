package eda.domain.data;

import eda.domain.DataType;

import java.util.List;

public interface DataReader {
    List<Object> read(String path, String columnName, DataType dataType);
    List<Object> readN(String path, String columnName, DataType dataType, int count);
}
