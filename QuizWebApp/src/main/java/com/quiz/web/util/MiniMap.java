package com.quiz.web.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The MiniMap uses the next linked node to store Key-Value objects.
 * The MimiMap is slow, so only for the MiniSize (size < 1000) data. 
 */
public class MiniMap<K, V> implements Map<K, V>{

    private int mSize = 0;
    private MiniNode<K, V> mBeginNode = null;
    private MiniNode<K, V> mEndNode = null;
    private MiniSet<Map.Entry<K, V>> mEntrySet = null;
    private MiniSet<K> mKeySet = null;
    private MiniSet<V> mValues = null;

    @Override
    public void clear() {
        this.mBeginNode = null;
        this.mEndNode = null;
        this.mSize = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.getNodeByKey(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return this.getNodeByValue(value) != null;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (mEntrySet == null) {
            mEntrySet = new MiniSet<Map.Entry<K, V>>(){
                @Override
                public Map.Entry<K, V> getValue(MiniNode<K, V> node) {
                    return node;
                }
            };
        }
        return mEntrySet;
    }

    @Override
    public V get(Object key) {
        V value = null;
        MiniNode<K, V> node = this.getNodeByKey(key);
        if (node != null) {
            value = node.mValue;
        }
        return value;
    }

    @Override
    public V getOrDefault(Object key, V def) {
        V value = this.get(key);
        if (value == null) value = def;
        return value;
    }

    @Override
    public boolean isEmpty() {
        return mSize <= 0;
    }

    @Override
    public Set<K> keySet() {
        if (mKeySet == null) {
            mKeySet = new MiniSet<K>(){
                @Override
                public K getValue(MiniNode<K, V> node) {
                    return node.mKey;
                }
            };
        }
        return mKeySet;
    }

    @Override
    public V put(K key, V value) {
        return this.addOrUpdate(key, value, true);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            K key = e.getKey();
            V value = e.getValue();
            this.addOrUpdate(key, value, true);
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return this.addOrUpdate(key, value, false);
    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            throw new RuntimeException("Key is null");
        }
        return this.removeNode(key, null);
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (key == null || value == null) {
            throw new RuntimeException("Key or value is null");
        }
        return this.removeNode(key, value) != null;
    }

    @Override
    public V replace(K key, V newValue) {
        V oldValue = null;
        MiniNode<K, V> node = this.getNodeByKey(key);
        if (node != null) {
            oldValue = node.mValue;
            node.mValue = newValue;
        }
        return oldValue;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        boolean result = false;
        MiniNode<K, V> node = this.getNodeByKey(key);
        if (node != null && node.mValue.equals(oldValue)) {
            node.mValue = newValue;
            result = true;
        }
        return result;
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public Collection<V> values() {
        if (mValues == null) {
            mValues = new MiniSet<V>(){
                @Override
                public V getValue(MiniNode<K, V> node) {
                    return node.mValue;
                }
            };
        }
        return mValues;
    }

    // Get the previous node by key
    private MiniNode<K, V> getPreviousNode(Object key) {
        MiniNode<K, V> node = mBeginNode;
        MiniNode<K, V> previous = null;
        boolean found = false;
        while (node != null) {
            if (node.mKey.equals(key)) {
                found = true;
                break;
            }
            previous = node;
            node = node.mNext;
        }
        return found ? previous : null;
    }

    // Get node by key
    private MiniNode<K, V> getNodeByKey(Object key) {
        if (key == null) {
            throw new RuntimeException("Key is null");
        }
        MiniNode<K, V> result = null;
        MiniNode<K, V> node = mBeginNode;
        while (node != null) {
            if (node.mKey.equals(key)) {
                result = node;
                break;
            }
            node = node.mNext;
        }
        return result;
    }

    // Get node by value
    private MiniNode<K, V> getNodeByValue(Object value) {
        MiniNode<K, V> result = null;
        MiniNode<K, V> node = mBeginNode;
        while (node != null) {
            if (node.mValue.equals(value)) {
                result = node;
                break;
            }
            node = node.mNext;
        }
        return result;
    }

    // Add key value or update existing value
    private V addOrUpdate(K key, V value, boolean update) {
        V result = value;
        if (key == null || value == null) {
            throw new RuntimeException("Key or value is null");
        } if (this.mBeginNode == null) {
            mBeginNode = new MiniNode<K, V>(key, value);
            mEndNode = mBeginNode;
            mSize = 1;
        } else {
            MiniNode<K, V> node = this.getNodeByKey(key);
            if (node == null) { // Add new node
                MiniNode<K, V> newNode = new MiniNode<K, V>(key, value);
                mEndNode.mNext = newNode;
                mEndNode = newNode;
                mSize++;
            } else if (update) { // Update old value
                node.setValue(value);
            } else { // Do not update old value
                result = null;
            }
        }
        return result;
    }

    // Remove node method
    private V removeNode(Object key, Object value) {
        V result = null;
        if (mBeginNode != null) {
            if (mBeginNode.mKey.equals(key)) { // Find the 1st node
                if (value == null || value.equals(mBeginNode.mValue)) {
                    result = mBeginNode.mValue;
                    if (mEndNode == mBeginNode) {
                        mEndNode = null;
                    }
                    mBeginNode = mBeginNode.mNext;
                    mSize--;
                }
            } else {
                MiniNode<K, V> previous = getPreviousNode(key);
                if (previous != null) { // Remove current node
                    MiniNode<K, V> current = previous.mNext;
                    if (value == null || value.equals(current.mValue)) {
                        result = current.mValue;
                        previous.mNext = current.mNext;
                        if (mEndNode == current) {
                            mEndNode = previous;
                        }
                        mSize--;
                    }
                }
            }
        }
        return result;
    }

    // MiniNode class
    private static class MiniNode<K, V> implements Map.Entry<K, V> {
        private K mKey = null;
        private V mValue = null;
        private MiniNode<K, V> mNext = null;
        // Constructor
        private MiniNode(K k, V v) {
            mKey = k;
            mValue = v;
        }
        @Override
        public K getKey() {
            return mKey;
        }
        @Override
        public V getValue() {
            return mValue;
        }
        @Override
        public V setValue(V value) {
            V old = mValue;
            mValue = value;
            return old;
        }
    }

    // MiniSet
    private abstract class MiniSet<E> extends AbstractSet<E> {
        @Override
        public Spliterator<E> spliterator() {
            return null;
        }
        @Override
        public Stream<E> parallelStream() {
            return null;
        }
        @Override
        public boolean removeIf(Predicate<? super E> p) {
            return false;
        }
        @Override
        public Stream<E> stream() {
            return null;
        }
        @Override
        public void forEach(Consumer<? super E> c) {
        }
        @Override
        public Iterator<E> iterator() {
            return new MiniIterator<E>(this);
        }
        @Override
        public int size() {
            return mSize;
        }
        // New abstract method
        public abstract E getValue(MiniNode<K, V> node);
    }

    // MiniIterator
    private final class MiniIterator<E> implements Iterator<E> {
        protected MiniNode<K, V> mEntry = null;
        private int mIndex = 0;
        private MiniSet<E> mSet = null;
        // Constructor
        public MiniIterator(MiniSet<E> set) {
            mSet = set;
        }
        @Override
        public boolean hasNext() {
            return mSize > 0 && mIndex < mSize;
        }
        @Override
        public E next() {
            E value = null;
            if (hasNext()) {
                if (mIndex == 0) {
                    mEntry = mBeginNode;
                } else {
                    mEntry = mEntry.mNext;
                }
                value = mSet.getValue(mEntry);
                mIndex++;
            }
            return value;
        }
    }
}
