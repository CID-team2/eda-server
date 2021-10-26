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

@Primary
@Component
public class LRUCachedDataReader implements DataReader {
    static final int DEFAULT_CACHE_SIZE = (int) 1e8;

    Cache<CacheKey, List<Object>> cache;

    LRUCachedDataReader() {
        cache = new LRUCache<>(DEFAULT_CACHE_SIZE);
    }

    LRUCachedDataReader(int size) {
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
        cache.put(key, values, calculateSize(valuesString, dataType));
        return values;
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
