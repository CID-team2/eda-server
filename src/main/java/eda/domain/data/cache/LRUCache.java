package eda.domain.data.cache;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCache <K, V> implements Cache<K, V> {
    private int size;
    private int maxSize;
    private DoublyLinkedList<CacheElement<K, V>> list = new DoublyLinkedList<>();
    private HashMap<K, DoublyLinkedList.Node<CacheElement<K, V>>> nodeHashMap = new HashMap<>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LRUCache(int size) {
        maxSize = size;
    }

    @Override
    public Optional<V> get(K key) {
        lock.readLock().lock();
        try {
            DoublyLinkedList.Node<CacheElement<K, V>> node = nodeHashMap.get(key);
            if (node == null)
                return Optional.empty();

            synchronized (this) {
                list.moveToFront(node);
            }

            return Optional.of(node.value.value);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void put(K key, V value, int size) {
        lock.writeLock().lock();
        try {
            if (size > maxSize)
                throw new IllegalArgumentException("cache entry size is bigger than cache size");

            DoublyLinkedList.Node<CacheElement<K, V>> node = nodeHashMap.get(key);
            if (node != null) {
                final int sizeDelta = size - node.value.size;
                if (this.size + sizeDelta > maxSize)
                    evict(sizeDelta);
                node.value = new CacheElement<>(key, value, size);
                list.moveToFront(node);
                this.size += sizeDelta;
            }
            else {
                if (this.size + size > maxSize)
                    evict(size);
                node = list.pushFront(new CacheElement<>(key, value, size));
                nodeHashMap.put(key, node);
                this.size += size;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void evict(int needSize) {
        lock.writeLock().lock();
        try {
            while (this.size + needSize > maxSize) {
                CacheElement<K, V> e = list.popBack();
                nodeHashMap.remove(e.key);
                this.size -= e.size;
            }
        } finally {
            lock.writeLock().unlock();
        }
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
