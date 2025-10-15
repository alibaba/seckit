package com.alibaba.seckit.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author renyi.cry
 * @date 2020/6/10 5:35 下午
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private final Lock lock = new ReentrantLock();

    private final int maxCapacity;

    public LRUCache(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public V get(Object key) {
        lock.lock();
        try {
            return super.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        lock.lock();
        try {
            return super.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        lock.lock();
        try {
            return super.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }

}


