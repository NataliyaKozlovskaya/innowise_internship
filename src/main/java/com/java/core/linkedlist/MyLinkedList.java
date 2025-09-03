package com.java.core.linkedlist;

import java.util.NoSuchElementException;

/**
 * Интерфейс для работы с двусвязным списком
 */
public interface MyLinkedList<T> {

    /**
     * Метод возвращает размер списка
     *
     * @return количество элементов в списке
     */
    int size();

    /**
     * Метод добавляет элемент в начало списка
     *
     * @param element элемент для добавления
     */
    void addFirst(T element);

    /**
     * Метод добавляет элемент в конец списка
     *
     * @param element элемент для добавления
     */
    void addLast(T element);

    /**
     * Метод добавляет элемент по указанному индексу
     *
     * @param index   индекс для вставки
     * @param element элемент для добавления
     * @throws IndexOutOfBoundsException если индекс невалидный
     */
    void add(int index, T element);

    /**
     * Метод возвращает первый элемент списка
     *
     * @return первый элемент
     * @throws NoSuchElementException если список пуст
     */
    T getFirst();

    /**
     * Метод возвращает последний элемент списка
     *
     * @return последний элемент
     * @throws NoSuchElementException если список пуст
     */
    T getLast();

    /**
     * Метод возвращает элемент по указанному индексу
     *
     * @param index индекс элемента
     * @return элемент по указанному индексу
     * @throws IndexOutOfBoundsException если индекс невалидный
     */
    T get(int index);

    /**
     * Метод удаляет и возвращает первый элемент списка
     *
     * @return удаленный первый элемент
     * @throws NoSuchElementException если список пуст
     */
    T removeFirst();

    /**
     * Метод удаляет и возвращает последний элемент списка
     *
     * @return удаленный последний элемент
     * @throws NoSuchElementException если список пуст
     */
    T removeLast();

    /**
     * Метод удаляет и возвращает элемент по указанному индексу
     *
     * @param index индекс элемента для удаления
     * @return удаленный элемент
     * @throws IndexOutOfBoundsException если индекс невалидный
     */
    T remove(int index);

    /**
     * Метод очищает список
     */
    void clear();

    /**
     * метод проверяет, пуст ли список
     *
     * @return true если список пуст
     */
    boolean isEmpty();
}
