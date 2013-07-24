package com.commafeed.backend;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class FixedSizeSortedSet<E> extends PriorityQueue<E> {

	private static final long serialVersionUID = 1L;

	private final Comparator<? super E> comparator;
	private final int maxSize;

	public FixedSizeSortedSet(int maxSize, Comparator<? super E> comparator) {
		super(maxSize, comparator);
		this.maxSize = maxSize;
		this.comparator = comparator;
	}

	@Override
	public boolean add(E e) {
		if (isFull()) {
			E last = last();
			int comparison = comparator.compare(e, last);
			if (comparison < 0) {
				remove(last);
				return super.add(e);
			} else {
				return false;
			}
		} else {
			return super.add(e);
		}
	}

	public E last() {
		return Iterables.getLast(this);
	}

	public boolean isFull() {
		return size() == maxSize;
	}

	@SuppressWarnings("unchecked")
	public List<E> asList() {
		return (List<E>) Lists.newArrayList(toArray());
	}
}