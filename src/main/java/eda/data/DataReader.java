package eda.data;

import eda.domain.DataType;

import java.util.List;

public interface DataReader {
    public List<Object> read(String path, String columnName, DataType dataType);
}
