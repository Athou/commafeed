package com.commafeed.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * List wrapper that sorts its elements in the order provided by given comparator and ensure a maximum capacity.
 * 
 * 
 */
public class FixedSizeSortedSet<E> {

	private List<E> inner;

	private final Comparator<? super E> comparator;
	private final int capacity;

	public FixedSizeSortedSet(int capacity, Comparator<? super E> comparator) {
		this.inner = new ArrayList<E>(Math.max(0, capacity));
		this.capacity = capacity < 0 ? Integer.MAX_VALUE : capacity;
		this.comparator = comparator;
	}

	public void add(E e) {
		int position = Math.abs(Collections.binarySearch(inner, e, comparator) + 1);
		if (isFull()) {
			if (position < inner.size()) {
				inner.remove(inner.size() - 1);
				inner.add(position, e);
			}
		} else {
			inner.add(position, e);
		}
	}

	public E last() {
		return inner.get(inner.size() - 1);
	}

	public boolean isFull() {
		return inner.size() == capacity;
	}

	public List<E> asList() {
		return inner;
	}
}