package eda.domain.data.cache;

class DoublyLinkedList <E> {
    private int size = 0;
    private Node<E> head;

    DoublyLinkedList() {
        head = new Node<>();
        head.next = head;
        head.prev = head;
    }

    Node<E> pushFront(E e) {
        Node<E> node = new Node<>();
        node.value = e;
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
        size += 1;
        return node;
    }

    E popBack() {
        if (size == 0)
            throw new IllegalStateException("List is empty");
        Node<E> node = head.prev;
        node.prev.next = node.next;
        node.next.prev = node.prev;
        size -= 1;
        return node.value;
    }

    void remove(Node<E> e) {
        if (size == 0)
            throw new IllegalStateException("List is empty");
        e.next.prev = e.prev;
        e.prev.next = e.next;
        size -= 1;
    }

    void moveToFront(Node<E> e) {
        if (e.prev == head)
            return;

        remove(e);
        pushFront(e.value);
    }

    void clear() {
        size = 0;
        head.next = head;
        head.prev = head;
    }

    int size() {
        return size;
    }

    static class Node <E> {
        E value;
        Node<E> next;
        Node<E> prev;
    }
}
