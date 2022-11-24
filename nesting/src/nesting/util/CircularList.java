package nesting.util;

import java.util.List;

/**
 * When dealing with vertices, edges, angles, etc. in a polygon, you often need the next and the
 * previous element. It's cumbersome to do this with regular lists because the next element to the
 * last element in the list is the first element and the previous element to the first element is
 * the last element. <code>CircularList</code> provides an interface for implementing a list data
 * structure that automatically wraps around the beginning and end of a list.
 *
 * @param <T> the type of element in the list
 */
public interface CircularList<T> extends List<T> {

    /**
     * This function transforms indices that would normally throw an exception because they're out of
     * range into a legal index. There is no need to write something like
     * <code>myList.get(myList.modulo(i + 1))</code> if you want to get the next element in the list
     * because the overridden <code>get</code> method does this for you, but in some cases you may want
     * to know what the index of the element before or after a certain index is explicitly.
     * 
     * @param i index of an element in the list
     * @return the index modulo the size of the list
     */
    public int modulo(int i);

    /**
     * Returns <code>true</code> if the elements are consecutive in the list. It doesn't matter in which
     * order. The <code>modulo</code> function is used to ensure that the indices are legal.
     * 
     * @param i index of an element in the list
     * @param j index of another element in the list
     * @return <code>true</code> if the elements are consecutive in the list
     */
    public boolean consecutive(int i, int j);

    /**
     * Removes a range of elements from the list. Indices are taken modulo the size of the list. If the
     * range wraps around the end of the list, elements are removed first from the end, then from the
     * beginning of the list.
     * 
     * @param fromIndex index of starting element (inclusive)
     * @param toIndex   index of end element (exclusive)
     */
    public void removeRange(int fromIndex, int toIndex);

    /**
     * Implementation of <code>get</code> that wraps around the beginning and end of list by calling
     * <code>modulo</code>.
     */
    @Override
    public T get(int i);

    /**
     * Implementation of <code>set</code> that wraps around the beginning and end of list by calling
     * <code>modulo</code>.
     */
    @Override
    public T set(int i, T newElement);

    /**
     * Implementation of <code>remove</code> that wraps around the beginning and end of list by calling
     * <code>modulo</code>.
     */
    @Override
    public T remove(int i);
}
