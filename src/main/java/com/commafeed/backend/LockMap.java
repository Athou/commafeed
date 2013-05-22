package com.commafeed.backend;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

// A map that creates and stores lock objects for arbitrary keys values.
// Lock objects which are no longer referenced are automatically released during garbage collection.
// Author: Christian d'Heureuse, www.source-code.biz
// Based on IdMutexProvider by McDowell, http://illegalargumentexception.blogspot.ch/2008/04/java-synchronizing-on-transient-id.html
// See also http://stackoverflow.com/questions/5639870/simple-java-name-based-locks
public class LockMap<KEY> {

	private WeakHashMap<KeyWrapper<KEY>, WeakReference<KeyWrapper<KEY>>> map;

	public LockMap() {
		map = new WeakHashMap<KeyWrapper<KEY>, WeakReference<KeyWrapper<KEY>>>();
	}

	// Returns a lock object for the specified key.
	public synchronized Object get(KEY key) {
		if (key == null) {
			throw new NullPointerException();
		}
		KeyWrapper<KEY> newKeyWrapper = new KeyWrapper<KEY>(key);
		WeakReference<KeyWrapper<KEY>> ref = map.get(newKeyWrapper);
		KeyWrapper<KEY> oldKeyWrapper = (ref == null) ? null : ref.get();
		if (oldKeyWrapper != null) {
			return oldKeyWrapper;
		}
		map.put(newKeyWrapper,
				new WeakReference<KeyWrapper<KEY>>(newKeyWrapper));
		return newKeyWrapper;
	}

	// Returns the number of used entries in the map.
	public synchronized int size() {
		return map.size();
	}

	// KeyWrapper wraps a key value and is used in three ways:
	// - as the key for the internal WeakHashMap
	// - as the value for the internal WeakHashMap, additionally wrapped in a
	// WeakReference
	// - as the lock object associated to the key
	private static class KeyWrapper<KEY> {
		private KEY key;
		private int hashCode;

		public KeyWrapper(KEY key) {
			this.key = key;
			hashCode = key.hashCode();
		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof KeyWrapper) {
				return ((KeyWrapper<?>) obj).key.equals(key);
			}
			return false;
		}

		public int hashCode() {
			return hashCode;
		}
	}

} // end class LockMap