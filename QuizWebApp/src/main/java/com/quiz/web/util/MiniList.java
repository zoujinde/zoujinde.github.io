package com.quiz.web.util;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class MiniList<E> implements List<E>{

    private static final int ADD_TO_TOP = 0;
    private static final int ADD_TO_END = -1;
    private static final int REMOVE_BY_INDEX = 0;
    private static final int REMOVE_BY_VALUE = 1;
    private static final int REMOVE_BY_COLLECTION = 2;
    private static final int RETAIN_BY_COLLECTION = 3;

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
        if (index < 0 || index >= mSize) {
            throw new RuntimeException("Invalid index : " + index);
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
    public boolean add(E obj) {
        this.add(ADD_TO_END, obj);
        return true;
    }

    @Override
    public void add(int index, E obj) {
        if (obj == null) {
            throw new RuntimeException("Object is null");
        }
        if (mBeginItem == null) {
            MiniItem<E> item = new MiniItem<E>();
            item.mValue = obj;
            mBeginItem = item;
            mEndItem = item;
            mSize++;
        } else if (index == ADD_TO_TOP) {
            MiniItem<E> item = new MiniItem<E>();
            item.mValue = obj;
            item.mNext = mBeginItem;
            mBeginItem = item;
            mSize++;
        } else if (index == ADD_TO_END) {
            MiniItem<E> item = new MiniItem<E>();
            item.mValue = obj;
            mEndItem.mNext = item;
            mEndItem = item;
            mSize++;
        } else { // Insert new item
            MiniItem<E> previous = this.getItem(index - 1);
            MiniItem<E> item = new MiniItem<E>();
            item.mValue = obj;
            item.mNext = previous.mNext;
            previous.mNext = item;
            mSize++;
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E obj : collection) {
            this.add(ADD_TO_END, obj);
        }
        return true;
    }

    @Override
    public boolean addAll(final int index, Collection<? extends E> collection) {
        int i = index;
        for (E obj : collection) {
            this.add(i++, obj);
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

    // private
    private boolean hasNextNode(int index) {
        return this.mSize > 0 && index < this.mSize;
    }

    // Iterator E
    private final class IteratorE implements Iterator<E> {
        private MiniItem<E> mEntry = null;
        private int mIndex = 0;
        @Override
        public boolean hasNext() {
            return hasNextNode(mIndex);
        }
        @Override
        public E next() {
            E value = null;
            if (hasNextNode(mIndex)) {
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
        @Override
        public void remove() {
            throw new RuntimeException("Unsupported method : remove");
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
        return this.remove(REMOVE_BY_INDEX, index, null, null);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        // Remove objects in collection
        return this.remove(REMOVE_BY_COLLECTION, 0, null, collection) != null;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        // Only retain objects in collection, and remove others.
        return this.remove(RETAIN_BY_COLLECTION, 0, null, collection) != null;
    }

    // private remove item method
    private E remove(int type, int index, E obj, Collection<?> collection) {
        E result = null;
        MiniItem<E> item = mBeginItem;
        MiniItem<E> previous = null;
        for (int i = 0; i < mSize; i++) {
            if (type == REMOVE_BY_INDEX && index == i) {
                result = item.mValue;
                removeItem(item, previous);
                break; // break
            } else if (type == REMOVE_BY_VALUE && obj.equals(item.mValue)) {
                result = obj;
                removeItem(item, previous);
                break; // Only remove the 1st matched value
            } else if (type == REMOVE_BY_COLLECTION && collection.contains(item.mValue)) {
                result = item.mValue;
                removeItem(item, previous);
            } else if (type == RETAIN_BY_COLLECTION && !collection.contains(item.mValue)) {
                result = item.mValue;
                removeItem(item, previous);
            } else {
                throw new RuntimeException("Invalid remove type");
            }
            previous = item;
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
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] toArray() {
        E[] result = null;
        if (mSize > 0) {
            ParameterizedType para = (ParameterizedType) getClass().getGenericSuperclass();
            Class<E> type = (Class<E>)para.getActualTypeArguments()[0];
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
        Object[] result = this.toArray();
        return (T[])result;
    }

}
