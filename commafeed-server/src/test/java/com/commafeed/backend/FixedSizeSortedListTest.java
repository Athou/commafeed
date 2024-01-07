package com.commafeed.backend;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FixedSizeSortedListTest {

	private static final Comparator<String> COMP = ObjectUtils::compare;

	private FixedSizeSortedList<String> list;

	@BeforeEach
	public void init() {
		list = new FixedSizeSortedList<>(3, COMP);
	}

	@Test
	void testSimpleAdd() {
		list.add("0");
		list.add("1");
		list.add("2");

		Assertions.assertEquals("0", list.asList().get(0));
		Assertions.assertEquals("1", list.asList().get(1));
		Assertions.assertEquals("2", list.asList().get(2));
	}

	@Test
	void testIsFull() {
		list.add("0");
		list.add("1");

		Assertions.assertFalse(list.isFull());
		list.add("2");
		Assertions.assertTrue(list.isFull());
	}

	@Test
	void testOrder() {
		list.add("2");
		list.add("1");
		list.add("0");

		Assertions.assertEquals("0", list.asList().get(0));
		Assertions.assertEquals("1", list.asList().get(1));
		Assertions.assertEquals("2", list.asList().get(2));
	}

	@Test
	void testEviction() {
		list.add("7");
		list.add("8");
		list.add("9");

		list.add("0");
		list.add("1");
		list.add("2");

		Assertions.assertEquals("0", list.asList().get(0));
		Assertions.assertEquals("1", list.asList().get(1));
		Assertions.assertEquals("2", list.asList().get(2));
	}

	@Test
	void testCapacity() {
		list.add("0");
		list.add("1");
		list.add("2");
		list.add("3");

		Assertions.assertEquals(3, list.asList().size());
	}

	@Test
	void testLast() {
		list.add("0");
		list.add("1");
		list.add("2");

		Assertions.assertEquals("2", list.last());

		list.add("3");

		Assertions.assertEquals("2", list.last());
	}
}
