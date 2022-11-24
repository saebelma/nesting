package nesting.util;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>ArrayList</code>-based implementation of the <code>CircularList</code> interface.
 *
 * @param <T> the type of element in the list
 */
public class CircularArrayList<T> extends ArrayList<T>
        implements CircularList<T> {

    /**
     * Constructs a <code>CircularArrayList</code> instance containing the elements of the specified
     * collection, in the order they are returned by the collection's iterator.
     * 
     * @param list a list
     */
    public CircularArrayList(List<T> list) {
        super(list);
    }

    /**
     * Constructs an empty <code>CircularArrayList</code> instance.
     */
    public CircularArrayList() {
        super();
    }

    @Override
    public void add(int i, T newElement) {
        super.add(modulo(i), newElement);
    }

    @Override
    public T get(int i) {
        return super.get(modulo(i));
    }

    @Override
    public T set(int i, T newElement) {
        return super.set(modulo(i), newElement);
    }

    @Override
    public T remove(int i) {
        return super.remove(modulo(i));
    }

    @Override
    public int modulo(int i) {
        int i_modulo = i % this.size();
        return i_modulo < 0 ? i_modulo + this.size() : i_modulo;
    }

    @Override
    public boolean consecutive(int i, int j) {
        return modulo(i + 1) == j || modulo(j + 1) == i;
    }

    @Override
    public void removeRange(int fromIndex, int toIndex) {
        int from_modulo = modulo(fromIndex);
        int to_modulo = modulo(toIndex);
        if (from_modulo == to_modulo) return;
        if (to_modulo > from_modulo) super.removeRange(from_modulo, to_modulo);
        if (to_modulo < from_modulo) {
            super.removeRange(fromIndex, this.size());
            super.removeRange(0, to_modulo);
        }
    }
}