package com.commafeed.backend;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FixedSizeSortedSetTest {

	private static final Comparator<String> COMP = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return ObjectUtils.compare(o1, o2);
		}
	};

	private FixedSizeSortedSet<String> set;

	@BeforeEach
	public void init() {
		set = new FixedSizeSortedSet<>(3, COMP);
	}

	@Test
	void testSimpleAdd() {
		set.add("0");
		set.add("1");
		set.add("2");

		Assertions.assertEquals("0", set.asList().get(0));
		Assertions.assertEquals("1", set.asList().get(1));
		Assertions.assertEquals("2", set.asList().get(2));
	}

	@Test
	void testIsFull() {
		set.add("0");
		set.add("1");

		Assertions.assertFalse(set.isFull());
		set.add("2");
		Assertions.assertTrue(set.isFull());
	}

	@Test
	void testOrder() {
		set.add("2");
		set.add("1");
		set.add("0");

		Assertions.assertEquals("0", set.asList().get(0));
		Assertions.assertEquals("1", set.asList().get(1));
		Assertions.assertEquals("2", set.asList().get(2));
	}

	@Test
	void testEviction() {
		set.add("7");
		set.add("8");
		set.add("9");

		set.add("0");
		set.add("1");
		set.add("2");

		Assertions.assertEquals("0", set.asList().get(0));
		Assertions.assertEquals("1", set.asList().get(1));
		Assertions.assertEquals("2", set.asList().get(2));
	}

	@Test
	void testCapacity() {
		set.add("0");
		set.add("1");
		set.add("2");
		set.add("3");

		Assertions.assertEquals(3, set.asList().size());
	}

	@Test
	void testLast() {
		set.add("0");
		set.add("1");
		set.add("2");

		Assertions.assertEquals("2", set.last());

		set.add("3");

		Assertions.assertEquals("2", set.last());
	}
}
