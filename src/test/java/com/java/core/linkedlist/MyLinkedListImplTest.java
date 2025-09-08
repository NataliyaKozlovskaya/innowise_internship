package com.java.core.linkedlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;


/**
 * Unit tests for MyLinkedList using JUnit 5
 */
@DisplayName("Testing MyLinkedListImplTest")
class MyLinkedListImplTest {

  private MyLinkedList<Integer> list;

  @BeforeEach
  void setUp() {
    list = new MyLinkedListImpl<>();
  }

  @Nested
  @DisplayName("Empty List Tests")
  class EmptyListTests {

    @Test
    @DisplayName("The size of an empty list must be 0")
    void testEmptyListSize() {
      assertEquals(0, list.size());
    }

    @Test
    @DisplayName("An empty list should return true for isEmpty")
    void testEmptyListIsEmpty() {
      assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Getting the first element from an empty list should throw an exception")
    void testGetFirstFromEmptyList() {
      assertThrows(NoSuchElementException.class, () -> list.getFirst());
    }

    @Test
    @DisplayName("Getting the last element from an empty list should throw an exception")
    void testGetLastFromEmptyList() {
      assertThrows(NoSuchElementException.class, () -> list.getLast());
    }

    @Test
    @DisplayName("Removing from an empty list should throw an exception")
    void testRemoveFromEmptyList() {
      assertThrows(NoSuchElementException.class, () -> list.removeFirst());
      assertThrows(NoSuchElementException.class, () -> list.removeLast());
    }
  }

  @Nested
  @DisplayName("Tests for adding elements")
  class AddTests {

    @Test
    @DisplayName("Adding to the beginning should increase the size")
    void testAddFirstIncreasesSize() {
      list.addFirst(10);
      assertEquals(1, list.size());
      assertFalse(list.isEmpty());
    }

    @Test
    @DisplayName("Adding to the end should increase the size")
    void testAddLastIncreasesSize() {
      list.addLast(20);
      assertEquals(1, list.size());
      assertFalse(list.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Adding elements multiple times")
    void testMultipleAdditions(int count) {
      for (int i = 0; i < count; i++) {
        list.addLast(i);
      }
      assertEquals(count, list.size());
    }

    @Test
    @DisplayName("Add by index to the middle of the list")
    void testAddAtIndexMiddle() {
      list.addLast(1);
      list.addLast(3);
      list.add(1, 2);

      assertEquals(3, list.size());
      assertEquals(1, list.get(0));
      assertEquals(2, list.get(1));
      assertEquals(3, list.get(2));
    }

    @Test
    @DisplayName("Adding at an invalid index should throw an exception")
    void testAddAtInvalidIndex() {
      assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 10));
      assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 10));

      list.addFirst(5);
      assertThrows(IndexOutOfBoundsException.class, () -> list.add(2, 10));
    }
  }

  @Nested
  @DisplayName("Element Retrieval Tests")
  class GetTests {

    @BeforeEach
    void setUp() {
      list.addLast(10);
      list.addLast(20);
      list.addLast(30);
    }

    @Test
    @DisplayName("Getting the first element")
    void testGetFirst() {
      assertEquals(10, list.getFirst());
    }

    @Test
    @DisplayName("Getting the last element")
    void testGetLast() {
      assertEquals(30, list.getLast());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 10",
        "1, 20",
        "2, 30"
    })
    @DisplayName("Getting an element by index")
    void testGetByIndex(int index, int expected) {
      assertEquals(expected, list.get(index));
    }

    @Test
    @DisplayName("Getting by invalid index should throw an exception")
    void testGetInvalidIndex() {
      assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
      assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
    }
  }

  @Nested
  @DisplayName("Element Removal Tests")
  class RemoveTests {

    @BeforeEach
    void setUp() {
      list.addLast(10);
      list.addLast(20);
      list.addLast(30);
      list.addLast(40);
    }

    @Test
    @DisplayName("Removing the first element")
    void testRemoveFirst() {
      assertEquals(10, list.removeFirst());
      assertEquals(3, list.size());
      assertEquals(20, list.getFirst());
    }

    @Test
    @DisplayName("Removing the last element")
    void testRemoveLast() {
      assertEquals(40, list.removeLast());
      assertEquals(3, list.size());
      assertEquals(30, list.getLast());
    }

    @Test
    @DisplayName("Delete by index")
    void testRemoveAtIndex() {
      assertEquals(20, list.remove(1));
      assertEquals(3, list.size());
      assertEquals(30, list.get(1));
    }

    @Test
    @DisplayName("Deleting by invalid index should throw an exception")
    void testRemoveInvalidIndex() {
      assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
      assertThrows(IndexOutOfBoundsException.class, () -> list.remove(4));
    }
  }

  @Test
  @DisplayName("Clear method test")
  void testClear() {
    list.addFirst(1);
    list.addLast(2);
    list.addLast(3);

    assertEquals(3, list.size());
    list.clear();

    assertTrue(list.isEmpty());
    assertEquals(0, list.size());
  }
}