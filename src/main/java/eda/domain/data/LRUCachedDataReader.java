package eda.domain.data;

import eda.domain.data.cache.Cache;
import eda.domain.data.cache.LRUCache;
import eda.domain.DataType;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class LRUCachedDataReader implements DataReader {
    static final int DEFAULT_CACHE_SIZE = (int) 1e8;

    private Cache<CacheKey, List<Object>> cache;

    public LRUCachedDataReader() {
        cache = new LRUCache<>(DEFAULT_CACHE_SIZE);
    }

    public LRUCachedDataReader(int size) {
        cache = new LRUCache<>(size);
    }

    @Override
    public List<Object> read(String path, String columnName, DataType dataType) {
        CacheKey key = new CacheKey(path, columnName, dataType);
        Optional<List<Object>> cached = cache.get(key);
        if (cached.isPresent())
            return cached.get();

        List<String> valuesString;
        try {
            ORCReader orcReader = new ORCReader(path);
            valuesString = orcReader.readColumn(columnName);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        List<Object> values = dataType.convertStringList(valuesString);
        int size = calculateSize(valuesString, dataType);
        if (size < DEFAULT_CACHE_SIZE)
            cache.put(key, values, size);
        return values;
    }

    @Override
    public List<Object> readN(String path, String columnName, DataType dataType, int count) {
        CacheKey key = new CacheKey(path, columnName, dataType);
        Optional<List<Object>> cached = cache.get(key);
        if (cached.isPresent()) {
            List<Object> list = cached.get();
            return list.subList(0, Math.min(count, list.size()));
        }

        List<String> valuesString;
        try {
            ORCReader orcReader = new ORCReader(path);
            valuesString = orcReader.readColumnN(columnName, count);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return dataType.convertStringList(valuesString);
    }

    public void clearCache() {
        cache.clear();
    }

    private int calculateSize(List<String> values, DataType dataType) {
        final int DATE_BYTESIZE = 32;
        final int OBJECT_BYTESIZE = 8;

        return switch (dataType) {
            case INT, FLOAT, BOOL -> values.size() * OBJECT_BYTESIZE;
            case STRING -> values.stream().mapToInt(String::length).sum();
            case DATE -> values.size() * DATE_BYTESIZE;
        };
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class CacheKey {
        private final String path;
        private final String columnName;
        private final DataType dataType;
    }
}
