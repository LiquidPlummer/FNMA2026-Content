import java.util.Iterator;

public class SimpleArrayList<E> implements SimpleList<E>{
    private int BASE_SIZE = 4;
    private int MAX_SIZE;
    private int cursor;
    private Object[] array;


    //constructor
    public SimpleArrayList() {
        init();
    }

    private void init() {
        //initialize an empty list
    }

    private void grow() {
        //grow the underlying array
        //Create a new array that is larger and can hold more elements
        //copy the existing elements into the new array
        //now add the new element to tne end
    }

    @Override
    public void add(E e) {
        //add element to the end of the array
    }

    @Override
    public void add(int index, E e) throws IndexOutOfBoundsException {
        //add element to specified position, shifting all later elements one space
        //need to make sure index is withing our array's limits
            //if index >= MAX_SIZE - now we need to grow
            //if index < 0 - this is bad times
        //if index < cursor - we MAY need to shift
            //if array[index] == null, then we do not need to shift
            //start at end of array, use for loop to traverse backwards toward cursor shifting elements one space
        //if index = cursor - all we need to do is the same as the add operation above
        //if index > cursor - we need to add the element there, and advance cursor to an appropriate position

    }

    @Override
    public void clear() {
        //empty array - returning to the original BASE_SIZE
    }

    @Override
    public void remove(int index) throws IndexOutOfBoundsException {
        //remove an element from a specified position, shifting all later elements back one space to fill in the gap

    }

    @Override
    public E get(int index) throws IndexOutOfBoundsException{
        return (E)array[index];
        //retrieve a value from the list at position specified
    }

    @Override
    public int contains(E e) {
        //traverse the list looking for the specified element, if it is found return the index, otherwise return -1
    }

    @Override
    public int length() {
        //return the current number of elements in the array,  (not the max size)
    }


    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            //implement arraylist iterator. We need to track an index as it traverses.
            @Override
            public boolean hasNext() {
                //return true if the array has more elements to iterate
            }

            @Override
            public E next() {
                //return the next element and move the iterator
            }
        };
    }
}
