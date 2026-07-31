import java.util.Iterator;

public class SimpleLinkedList<E> implements SimpleList<E>{
    private Node<E> HEAD;
    private int length = 0;

    //constructor
    public SimpleLinkedList() {
        HEAD = null;
    }

    @Override
    public void add(E e) {
        //add element to end of list
        //How do we do this?
        //create a new element (Node)
        Node<E> node = new Node<>();
        node.e = e;
        //before traversing, handle the edge case of an empty list.
        if(HEAD == null) {
            HEAD = node;
            length++;
            return;
        }
        //traverse the list to the end
        Node<E> tail = findTail();
        //append to end by having the final node with null ref point to new node
        tail.next = node;
        length++;
    }

    @Override//01234
    public void add(int index, E e) {
        //add element to specified position, splicing it into place
        //create new node
        Node<E> newNode = new Node<>();
        newNode.e = e;
        //traverse to index
        Node<E> cursor = traverse(index);
        //point the new node to the cursor's reference
        newNode.next = cursor.next;
        //point the indexed node to our new node
        cursor.next = newNode;
        length++;
    }

    public void replace(int index, E e) {
        //traverse to index
        Node<E> cursor = traverse(index);
        //we could just swap out the value in the node
        cursor.e = e;
    }

    @Override
    public void clear() {
        //empty array
        HEAD = null;
        length = 0;
    }

    @Override
    public void remove(int index) {
        //remove an element from a specified position, splicing it out
        //traverse to the node before the one to be removed
        Node<E> cursor = traverse(index-1);
        //cut out the next node (the one at index) by bypassing it to the next next node
        cursor.next = cursor.next.next;
        length--;
    }

    @Override
    public E get(int index) {
        //traverse the list to retrieve a value at position specified
        Node<E> cursor = traverse(index);
        return cursor.e;
    }

    @Override
    public int contains(E e) {
        //keep an idex counter while we go
        int index = 0;
        Node<E> cursor = HEAD;
        //traverse the list looking for the specified element, if it is found return the index, otherwise return -1
        //start by checking if HEAD has the value
        if(cursor == null) {
            return -1;
        }
        while(cursor.next != null){
            if(cursor.e == e) {
                return index;
            }
            cursor = cursor.next;
            index++;
        }
        return -1;
    }

    @Override
    public int length() {
        return length;
    }

    private Node<E> traverse(int index) {
        Node<E> cursor = HEAD;
        for(int i = 0; i < index; i++) {
            cursor = cursor.next;
        }

        return cursor;
    }

    private Node<E> findTail() {
        Node<E> cursor = HEAD;
        if(HEAD == null) {
            return HEAD;
        }
        while(cursor.next != null) {
            cursor = cursor.next;
            if(cursor.next == null) {
                return cursor;
            }
        }
        return cursor;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            //implement linkedlist iterator. We need to track the node as we traverse
            Node<E> cursor = HEAD;

            @Override
            public boolean hasNext() {
                if(cursor != null) {
                    return true;
                }
                return false;
            }

            @Override
            public E next() {
                //return the next element in the list, and advance the cursor
                E value = cursor.e;
                cursor = cursor.next;
                return value;
            }
        };
    }


    private class Node<E> {
        E e;
        Node<E> next;
    }
}
