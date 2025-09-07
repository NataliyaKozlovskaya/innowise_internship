package com.java.core.linkedlist;

import java.util.NoSuchElementException;

/**
 * Interface for working with a doubly linked list
 */
public interface MyLinkedList<T> {

  /**
   * Method returns the size of the list
   *
   * @return the number of elements in the list
   */
  int size();

  /**
   * Method adds an element to the beginning of the list
   *
   * @param element element to add
   */
  void addFirst(T element);

  /**
   * Method adds an element to the end of the list
   *
   * @param element element to add
   */
  void addLast(T element);

  /**
   * The method adds an element at the specified index
   *
   * @param index   index to insert
   * @param element element to add
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  void add(int index, T element);

  /**
   * The method returns the first element of the list
   *
   * @return the first element
   * @throws NoSuchElementException if the list is empty
   */
  T getFirst();

  /**
   * The method returns the first element of the list
   *
   * @return the first element
   * @throws NoSuchElementException if the list is empty
   */
  T getLast();

  /**
   * The method returns the element at the specified index
   *
   * @param index element index
   * @return element at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  T get(int index);

  /**
   * Method removes and returns the first element of the list
   *
   * @return the removed first element
   * @throws NoSuchElementException if the list is empty
   */
  T removeFirst();

  /**
   * Method removes and returns the last element of the list
   *
   * @return the removed last element
   * @throws NoSuchElementException if the list is empty
   */
  T removeLast();

  /**
   * Method removes and returns the element at the specified index
   *
   * @param index index of the element to remove
   * @return the removed element
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  T remove(int index);

  /**
   * Method clears the list
   */
  void clear();

  /**
   * Method checks if the list is empty
   *
   * @return true if the list is empty
   */
  boolean isEmpty();
}
