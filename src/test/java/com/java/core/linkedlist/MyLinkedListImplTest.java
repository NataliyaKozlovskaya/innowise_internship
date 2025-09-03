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
 * Unit-тесты для MyLinkedList с использованием JUnit 5
 */
@DisplayName("Тестирование MyLinkedListImplTest")
class MyLinkedListImplTest {

    private MyLinkedList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new MyLinkedListImpl<>();
    }

    @Nested
    @DisplayName("Тесты пустого списка")
    class EmptyListTests {

        @Test
        @DisplayName("Размер пустого списка должен быть 0")
        void testEmptyListSize() {
            assertEquals(0, list.size());
        }

        @Test
        @DisplayName("Пустой список должен возвращать true для isEmpty")
        void testEmptyListIsEmpty() {
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("Получение первого элемента из пустого списка должно бросать исключение")
        void testGetFirstFromEmptyList() {
            assertThrows(NoSuchElementException.class, () -> list.getFirst());
        }

        @Test
        @DisplayName("Получение последнего элемента из пустого списка должно бросать исключение")
        void testGetLastFromEmptyList() {
            assertThrows(NoSuchElementException.class, () -> list.getLast());
        }

        @Test
        @DisplayName("Удаление из пустого списка должно бросать исключение")
        void testRemoveFromEmptyList() {
            assertThrows(NoSuchElementException.class, () -> list.removeFirst());
            assertThrows(NoSuchElementException.class, () -> list.removeLast());
        }
    }

    @Nested
    @DisplayName("Тесты добавления элементов")
    class AddTests {

        @Test
        @DisplayName("Добавление в начало должно увеличивать размер")
        void testAddFirstIncreasesSize() {
            list.addFirst(10);
            assertEquals(1, list.size());
            assertFalse(list.isEmpty());
        }

        @Test
        @DisplayName("Добавление в конец должно увеличивать размер")
        void testAddLastIncreasesSize() {
            list.addLast(20);
            assertEquals(1, list.size());
            assertFalse(list.isEmpty());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        @DisplayName("Многократное добавление элементов")
        void testMultipleAdditions(int count) {
            for (int i = 0; i < count; i++) {
                list.addLast(i);
            }
            assertEquals(count, list.size());
        }

        @Test
        @DisplayName("Добавление по индексу в середину списка")
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
        @DisplayName("Добавление по невалидному индексу должно бросать исключение")
        void testAddAtInvalidIndex() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 10));
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 10));

            list.addFirst(5);
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(2, 10));
        }
    }

    @Nested
    @DisplayName("Тесты получения элементов")
    class GetTests {

        @BeforeEach
        void setUp() {
            list.addLast(10);
            list.addLast(20);
            list.addLast(30);
        }

        @Test
        @DisplayName("Получение первого элемента")
        void testGetFirst() {
            assertEquals(10, list.getFirst());
        }

        @Test
        @DisplayName("Получение последнего элемента")
        void testGetLast() {
            assertEquals(30, list.getLast());
        }

        @ParameterizedTest
        @CsvSource({
            "0, 10",
            "1, 20",
            "2, 30"
        })
        @DisplayName("Получение элемента по индексу")
        void testGetByIndex(int index, int expected) {
            assertEquals(expected, list.get(index));
        }

        @Test
        @DisplayName("Получение по невалидному индексу должно бросать исключение")
        void testGetInvalidIndex() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
        }
    }

    @Nested
    @DisplayName("Тесты удаления элементов")
    class RemoveTests {

        @BeforeEach
        void setUp() {
            list.addLast(10);
            list.addLast(20);
            list.addLast(30);
            list.addLast(40);
        }

        @Test
        @DisplayName("Удаление первого элемента")
        void testRemoveFirst() {
            assertEquals(10, list.removeFirst());
            assertEquals(3, list.size());
            assertEquals(20, list.getFirst());
        }

        @Test
        @DisplayName("Удаление последнего элемента")
        void testRemoveLast() {
            assertEquals(40, list.removeLast());
            assertEquals(3, list.size());
            assertEquals(30, list.getLast());
        }

        @Test
        @DisplayName("Удаление по индексу")
        void testRemoveAtIndex() {
            assertEquals(20, list.remove(1));
            assertEquals(3, list.size());
            assertEquals(30, list.get(1));
        }

        @Test
        @DisplayName("Удаление по невалидному индексу должно бросать исключение")
        void testRemoveInvalidIndex() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(4));
        }
    }

    @Test
    @DisplayName("Тест метода clear")
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