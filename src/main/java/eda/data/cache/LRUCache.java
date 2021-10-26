package eda.data.cache;

import java.util.HashMap;
import java.util.Optional;

public class LRUCache <K, V> implements Cache<K, V> {
    private int size;
    private int maxSize;
    private DoublyLinkedList<CacheElement<K, V>> list = new DoublyLinkedList<>();
    private HashMap<K, DoublyLinkedList.Node<CacheElement<K, V>>> nodeHashMap = new HashMap<>();

    public LRUCache(int size) {
        maxSize = size;
    }

    @Override
    public Optional<V> get(K key) {
        DoublyLinkedList.Node<CacheElement<K, V>> node = nodeHashMap.get(key);
        if (node == null)
            return Optional.empty();

        list.moveToFront(node);
        return Optional.of(node.value.value);
    }

    @Override
    public void put(K key, V value, int size) {
        if (size > maxSize)
            throw new IllegalArgumentException("cache entry size is bigger than cache size");

        while (this.size + size > maxSize) {
            CacheElement<K, V> e = list.popBack();
            nodeHashMap.remove(e.key);
            this.size -= e.size;
        }

        DoublyLinkedList.Node<CacheElement<K, V>> node = list.pushFront(new CacheElement<>(key, value, size));
        nodeHashMap.put(key, node);
        this.size += size;
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        size = 0;
        list.clear();
        nodeHashMap.clear();
    }

    static class CacheElement <K, V> {
        K key;
        V value;
        int size;

        CacheElement(K key, V value, int size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }
    }
}
