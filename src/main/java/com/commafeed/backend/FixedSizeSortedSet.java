package com.commafeed.backend;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;

public class FixedSizeSortedSet<E> extends TreeSet<E> {

	private static final long serialVersionUID = 1L;

	private final Comparator<? super E> comparator;
	private final int maxSize;

	public FixedSizeSortedSet(int maxSize, Comparator<? super E> comparator) {
		super(comparator);
		this.maxSize = maxSize;
		this.comparator = comparator;
	}

	@Override
	public boolean add(E e) {
		if (size() == maxSize) {
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

	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (CollectionUtils.isEmpty(c)) {
			return false;
		}

		boolean success = true;
		for (E e : c) {
			success &= add(e);
		}
		return success;
	}

	@SuppressWarnings("unchecked")
	public List<E> asList() {
		return (List<E>) Lists.newArrayList(toArray());
	}
}