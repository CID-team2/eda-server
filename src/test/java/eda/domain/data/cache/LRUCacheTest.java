package eda.domain.data.cache;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {
    @Test
    void get_and_evict() {
        // given
        final LRUCache<String, Integer> cache = new LRUCache<>(3);

        // when
        cache.put("a", 1, 1);
        cache.put("b", 2, 1);
        cache.put("c", 3, 1);
        cache.put("d", 4, 1);
        Optional<Integer> valueA = cache.get("a");
        Optional<Integer> valueB = cache.get("b");
        Optional<Integer> valueC = cache.get("c");
        Optional<Integer> valueD = cache.get("d");

        // then
        assertTrue(valueA.isEmpty());
        assertTrue(valueB.isPresent());
        assertTrue(valueC.isPresent());
        assertTrue(valueD.isPresent());
        assertEquals(2, valueB.get());
        assertEquals(3, valueC.get());
        assertEquals(4, valueD.get());
    }

    @Test
    void evict_with_size() {
        // given
        final LRUCache<String, Integer> cache = new LRUCache<>(3);

        // when
        cache.put("a", 1, 1);
        cache.put("b", 2, 2);
        cache.put("c", 3, 3);
        Optional<Integer> valueA = cache.get("a");
        Optional<Integer> valueB = cache.get("b");
        Optional<Integer> valueC = cache.get("c");

        // then
        assertTrue(valueA.isEmpty());
        assertTrue(valueB.isEmpty());
        assertTrue(valueC.isPresent());
        assertEquals(3, valueC.get());
    }

    @Test
    void concurrent_no_data_loss() {
        // given
        final int size = 1000;
        final int cacheSize = 100;
        final LRUCache<String, Integer> cache = new LRUCache<>(cacheSize);

        // when
        List<Thread> threads = IntStream.range(0, size)
                .mapToObj(i -> new Thread(() -> cache.put(Integer.toString(i), i, 1)))
                .toList();
        for (Thread thread : threads)
            thread.start();
        try {
            for (Thread thread : threads)
                thread.join();
        } catch (InterruptedException e) {
            fail();
        }

        // then
        for (int i = 0; i < size - cacheSize; i++) {
            assertTrue(cache.get(Integer.toString(i)).isEmpty());
        }
        for (int i = size - cacheSize; i < size; i++) {
            assertTrue(cache.get(Integer.toString(i)).isPresent());
            assertEquals(i, cache.get(Integer.toString(i)).get());
        }
    }
}