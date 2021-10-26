package eda.data.cache;

import java.util.Optional;

public interface Cache <K, V> {
    public Optional<V> get(K key);
    public void put(K key, V value, int size);
    public int maxSize();
    public int size();
    public void clear();
}
