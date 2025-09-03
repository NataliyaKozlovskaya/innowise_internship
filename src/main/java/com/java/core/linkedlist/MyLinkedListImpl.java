package com.java.core.linkedlist;

import java.util.NoSuchElementException;

/**
 * Реализация двусвязного списка
 */
public class MyLinkedListImpl<T> implements MyLinkedList<T> {
    private static final String EMPTY_LIST = "List is empty";
    private static final String INDEX = "Index: ";
    private static final String LIST_SIZE = ", Size: ";
    private static class Node<T> {
        T data;
        Node<T> next;
        Node<T> prev;
        Node(T data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public MyLinkedListImpl() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void addFirst(T element) {
        Node<T> newNode = new Node<>(element);

        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        size++;
    }

    @Override
    public void addLast(T element) {
        Node<T> newNode = new Node<>(element);

        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    @Override
    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(INDEX + index + LIST_SIZE + size);
        }

        if (index == 0) {
            addFirst(element);
        } else if (index == size) {
            addLast(element);
        } else {
            Node<T> newNode = new Node<>(element);

            // Определяем, с какой стороны ближе идти
            if (index < size / 2) {
                Node<T> current = head;
                for (int i = 0; i < index - 1; i++) {
                    current = current.next;
                }

                newNode.next = current.next;
                newNode.prev = current;
                current.next.prev = newNode;
                current.next = newNode;
            } else {
                Node<T> current = tail;
                for (int i = size - 1; i > index; i--) {
                    current = current.prev;
                }

                newNode.prev = current.prev;
                newNode.next = current;
                current.prev.next = newNode;
                current.prev = newNode;
            }
            size++;
        }
    }

    @Override
    public T getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException(EMPTY_LIST);
        }
        return head.data;
    }

    @Override
    public T getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException(EMPTY_LIST);
        }
        return tail.data;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(INDEX + index + LIST_SIZE + size);
        }

        // Определяем, с какой стороны ближе идти
        if (index < size / 2) {
            Node<T> current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current.data;
        } else {
            Node<T> current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.prev;
            }
            return current.data;
        }
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException(EMPTY_LIST);
        }

        T removedData = head.data;

        if (size == 1) {
            head = null;
            tail = null;
        } else {
            head = head.next;
            head.prev = null;
        }

        size--;
        return removedData;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException(EMPTY_LIST);
        }

        T removedData = tail.data;

        if (size == 1) {
            head = null;
            tail = null;
        } else {
            tail = tail.prev;
            tail.next = null;
        }

        size--;
        return removedData;
    }

    @Override
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(INDEX + index + LIST_SIZE + size);
        }

        if (index == 0) {
            return removeFirst();
        } else if (index == size - 1) {
            return removeLast();
        } else {
            Node<T> nodeToRemove;

            // Определяем, с какой стороны ближе идти
            if (index < size / 2) {
                nodeToRemove = head;
                for (int i = 0; i < index; i++) {
                    nodeToRemove = nodeToRemove.next;
                }
            } else {
                nodeToRemove = tail;
                for (int i = size - 1; i > index; i--) {
                    nodeToRemove = nodeToRemove.prev;
                }
            }

            T removedData = nodeToRemove.data;

            // Обновляем ссылки соседних узлов
            nodeToRemove.prev.next = nodeToRemove.next;
            nodeToRemove.next.prev = nodeToRemove.prev;

            size--;
            return removedData;
        }
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        Node<T> current = head;
        while (current != null) {
            Node<T> next = current.next;
            current.prev = null;
            current.next = null;
            current = next;
        }

        head = null;
        tail = null;
        size = 0;
    }
}