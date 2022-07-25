package com.quiz.web.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class MiniList<E> implements List<E>{

    private static final int REMOVE_BY_INDEX = 0;
    private static final int REMOVE_BY_VALUE = 1;
    private static final int REMOVE_COLLECTION = 2;
    private static final int RETAIN_COLLECTION = 3;

    private int mSize = 0;
    private MiniItem<E> mBeginItem = null;
    private MiniItem<E> mEndItem = null;

    // MiniItem class
    private static class MiniItem<E> {
        private E mValue = null;
        private MiniItem<E> mNext = null;
    }

    @Override
    public Stream<E> parallelStream() {
        return null;
    }

    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        return false;
    }

    @Override
    public Stream<E> stream() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
    }

    @Override
    public void sort(Comparator<? super E> comparator) {
        throw new RuntimeException("Unsupported sort method");
    }

    @Override
    public Spliterator<E> spliterator() {
        return null;
    }

    @Override
    public E get(int index) {
        MiniItem<E> item = this.getItem(index);
        E result = null;
        if (item != null) {
            result = item.mValue;
        }
        return result;
    }

    // private get item
    private MiniItem<E> getItem(int index) {
        if (index < -1 || index >= mSize) {
            throw new RuntimeException("Invalid index : " + index);
        } else if (index == -1) {
            return null; // before the 1st item
        }
        MiniItem<E> result = null;
        MiniItem<E> item = mBeginItem;
        for (int i = 0; i < mSize; i++) {
            if (i == index) {
                result = item;
                break;
            }
            item = item.mNext;
        }
        return result;
    }

    // private get index by value
    private int getIndex(Object value, boolean getFirst) {
        if (value == null) {
            throw new RuntimeException("getIndex : value is null");
        }
        int index = -1;
        MiniItem<E> item = mBeginItem;
        for (int i = 0; i < mSize; i++) {
            if (item.mValue.equals(value)) {
                index = i;
                if (getFirst) break;
            }
            item = item.mNext;
        }
        return index;
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public boolean add(E value) {
        this.addItem(mEndItem, value);
        return true;
    }

    @Override
    public void add(int index, E value) {
        MiniItem<E> previous = this.getItem(index - 1);
        this.addItem(previous, value);
    }

    // private add item
    private MiniItem<E> addItem(MiniItem<E> previous, E value) {
        if (value == null) {
            throw new RuntimeException("addItem : value is null");
        }
        MiniItem<E> newItem = new MiniItem<E>();
        if (mEndItem == null) { // Empty list
            mBeginItem = newItem;
            mEndItem = newItem;
        } else if (previous == null) { // Add on top
            newItem.mNext = mBeginItem;
            mBeginItem = newItem;
        } else {
            newItem.mNext = previous.mNext;
            previous.mNext = newItem;
            if (mEndItem == previous) {
                mEndItem = newItem;
            }
        }
        newItem.mValue = value;
        mSize++;
        return newItem;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        if (collection == this) {
            throw new RuntimeException("Can't add this list self");
        }
        for (E value : collection) {
            this.addItem(mEndItem, value);
        }
        return true;
    }

    @Override
    public boolean addAll(final int index, Collection<? extends E> collection) {
        if (collection == this) {
            throw new RuntimeException("Can't add this list self");
        }
        MiniItem<E> previous = this.getItem(index - 1);
        for (E value : collection) {
            previous = this.addItem(previous, value);
        }
        return true;
    }

    @Override
    public void clear() {
        this.mBeginItem = null;
        this.mEndItem = null;
        this.mSize = 0;
    }

    @Override
    public boolean contains(Object object) {
        return this.indexOf(object) >= 0;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        boolean result = false;
        if (mSize > 0 && collection.size() > 0) {
            result = true;
            for (Object obj : collection) {
                if (this.indexOf(obj) < 0) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public int indexOf(Object value) {
        return this.getIndex(value, true);
    }

    @Override
    public boolean isEmpty() {
        return mSize <= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorE();
    }

    // Iterator E
    private final class IteratorE implements Iterator<E> {
        private MiniItem<E> mEntry = null;
        private int mIndex = 0;
        @Override
        public boolean hasNext() {
            return mSize > 0 && mIndex < mSize;
        }
        @Override
        public E next() {
            E value = null;
            if (hasNext()) {
                if (mIndex == 0) {
                    mEntry = mBeginItem;
                } else {
                    mEntry = mEntry.mNext;
                }
                value = mEntry.mValue;
                mIndex++;
            }
            return value;
        }
    }

    @Override
    public int lastIndexOf(Object value) {
        return this.getIndex(value, false);
    }

    @Override
    public ListIterator<E> listIterator() {
        return null;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object object) {
        return this.remove(REMOVE_BY_VALUE, 0, (E)object, null) != null;
    }

    @Override
    public E remove(int index) {
        if (index < 0 || index >= mSize) {
            throw new RuntimeException("remove : invalid index " + index);
        }
        return this.remove(REMOVE_BY_INDEX, index, null, null);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        // Remove objects in collection
        return this.remove(REMOVE_COLLECTION, 0, null, collection) != null;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        // Only retain objects in collection, and remove others.
        return this.remove(RETAIN_COLLECTION, 0, null, collection) != null;
    }

    // private remove item method
    private E remove(int type, int index, E obj, Collection<?> collection) {
        E result = null;
        MiniItem<E> item = mBeginItem;
        MiniItem<E> previous = null;
        // Must remember the count, because mSize will be reduced when remove item
        int count = mSize;
        for (int i = 0; i < count; i++) {
            if (type == REMOVE_BY_INDEX && index == i) {
                result = item.mValue;
                removeItem(item, previous);
                break; // break
            } else if (type == REMOVE_BY_VALUE && obj.equals(item.mValue)) {
                result = obj;
                removeItem(item, previous);
                break; // Only remove the 1st matched value
            } else if (type == REMOVE_COLLECTION && collection.contains(item.mValue)) {
                result = item.mValue;
                removeItem(item, previous);
            } else if (type == RETAIN_COLLECTION && !collection.contains(item.mValue)) {
                result = item.mValue;
                removeItem(item, previous);
            } else {
                previous = item;
            }
            item = item.mNext;
        }
        return result;
    }

    // private remove item
    private void removeItem(MiniItem<E> item, MiniItem<E> previous) {
        if (item == mBeginItem && previous == null) {
            mBeginItem = item.mNext;
        } else if (previous.mNext == item) {
            previous.mNext = item.mNext;
        } else {
            throw new RuntimeException("removeItem : invalid item");
        }
        // Set the end item
        if (mEndItem == item) {
            mEndItem = previous;
        }
        mSize--;
    }

    @Override
    public E set(int index, E value) {
        if (index < 0 || index >= mSize || value == null) {
            throw new RuntimeException("set : invalid index or value");
        }
        E oldValue = null;
        MiniItem<E> item = mBeginItem;
        for (int i = 0; i < mSize; i++) {
            if (i == index) {
                oldValue = item.mValue;
                item.mValue = value;
                break;
            }
            item = item.mNext;
        }
        return oldValue;
    }

    @Override
    public List<E> subList(int begin, int end) {
        List<E> result = null;
        if (begin >= 0 && begin < end && end <= mSize) {
            result = new MiniList<E>();
            MiniItem<E> item = mBeginItem;
            for (int i = 0; i < end; i++) {
                if (i >= begin) {
                    result.add(item.mValue);
                }
                item = item.mNext;
            }
        } else {
            throw new RuntimeException("subList : invalid range");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] toArray() {
        E[] result = null;
        if (mSize > 0) {
            //ParameterizedType para = (ParameterizedType) getClass().getGenericSuperclass();
            //Class<E> type = (Class<E>)para.getActualTypeArguments()[0];
            Class<?> type = mBeginItem.mValue.getClass();
            result = (E[]) Array.newInstance(type, mSize);
            MiniItem<E> item = mBeginItem;
            for (int i = 0; i < mSize; i++) {
                result[i] = item.mValue;
                item = item.mNext;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] array) {
        T[] result = (T[]) this.toArray();
        return result;
    }

    // Test MiniList and MiniMap
    public static void main(String[] args) throws Exception {
        // Test MiniList
        List<Integer> list1 = new MiniList<Integer>();
        for (int i = 0; i < 10; i++) {
            list1.add(i);
        }
        List<Integer> list2 = new MiniList<Integer>();
        for (int i = 0; i < 5; i++) {
            list2.add(i+100);
        }
        list1.addAll(5, list2);
        //list1 = list1.subList(5, 12);
        System.out.println("========== list iterator ==========");
        Iterator<Integer> iterator = list1.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + ",");
        }
        // Test MiniMap
        System.out.println("\n\n========== map test ==========");
        Map<Integer, String> map1 = new MiniMap<>();
        for (int i = 0; i < 10; i++) {
            map1.put(i, "=" + i);
        }
        Map<Integer, String> map2 = new MiniMap<>();
        for (int i = 0; i < 5; i++) {
            map2.put(i, "=" + i + 10);
        }
        map1.putAll(map2);
        //System.out.println();
        for (Map.Entry<Integer, String> entry : map1.entrySet()) {
            System.out.print(entry.getKey() + entry.getValue() + ", ");
        }
        for (Integer k : map1.keySet()) {
            System.out.print(k + ", ");
        }
        for (String v : map1.values()) {
            System.out.print(v + ", ");
        }
    }

}
