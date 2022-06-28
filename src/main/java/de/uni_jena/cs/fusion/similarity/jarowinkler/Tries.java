package de.uni_jena.cs.fusion.similarity.jarowinkler;

/*-
 * #%L
 * Jaro Winkler Similarity
 * %%
 * Copyright (C) 2018 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class consists of static methods that return {@link Trie}s.
 * 
 * @see Trie
 * @see Collections
 * @author Jan Martin Keil
 * @since 0.1
 */
class Tries {

	private static class EmptyTrie<V> implements Trie<V> {

		@Override
		public Iterator<Trie<V>> childrenIterator() {
			return Collections.emptyIterator();
		}

		@Override
		public Collection<Integer> containedLengths() {
			return Collections.emptyList();
		}

		@Override
		public int depth() {
			return 0;
		}

		@Override
		public boolean isPopulated() {
			return false;
		}

		@Override
		public String key() {
			return "";
		}

		@Override
		public int keyLength() {
			return 0;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public String symbol() {
			return "";
		}

		@Override
		public V value() throws NoSuchElementException {
			throw new NoSuchElementException();
		}
	}

	private static class SingletonIterator<E> implements Iterator<E> {
		boolean hasNext = true;
		final E element;

		public SingletonIterator(E element) {
			this.element = element;
		}

		@Override
		public boolean hasNext() {
			return this.hasNext;
		}

		@Override
		public E next() {
			if (this.hasNext) {
				this.hasNext = false;
				return element;
			} else {
				throw new NoSuchElementException();
			}
		}
	}

	private static class SingletonTrieMap<V> extends AbstractMap<String, V> implements Trie<V> {
		private final String key;
		private final V value;

		SingletonTrieMap(String key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Iterator<Trie<V>> childrenIterator() {
			return Collections.emptyIterator();
		}

		@Override
		public Collection<Integer> containedLengths() {
			return Collections.singleton(key.length());
		}

		@Override
		public int depth() {
			return 0;
		}

		@Override
		public Set<java.util.Map.Entry<String, V>> entrySet() {
			return Collections.singleton(mapEntry(key, value));
		}

		@Override
		public V get(Object key) {
			if (key instanceof String && this.key.equals(key)) {
				return value;
			} else {
				return null;
			}
		}

		@Override
		public boolean isPopulated() {
			return true;
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public int keyLength() {
			return key.length();
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public String symbol() {
			return this.key;
		}

		@Override
		public V value() {
			return this.value;
		}
	}

	private static class SingletonTrieSet extends AbstractSet<String> implements Trie<String> {

		private final String element;

		SingletonTrieSet(String element) {
			this.element = element;
		}

		@Override
		public Iterator<Trie<String>> childrenIterator() {
			return Collections.emptyIterator();
		}

		@Override
		public Collection<Integer> containedLengths() {
			return Collections.singleton(element.length());
		}

		@Override
		public int depth() {
			return 0;
		}

		@Override
		public boolean isPopulated() {
			return true;
		}

		@Override
		public Iterator<String> iterator() {
			return singletonIterator(element);
		}

		@Override
		public String key() {
			return element;
		}

		@Override
		public int keyLength() {
			return element.length();
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public String symbol() {
			return this.element;
		}

		@Override
		public String value() {
			return this.element;
		}
	}

	private static final Trie<?> EMPTY_TRIE = new EmptyTrie<>();

	@SuppressWarnings("unchecked")
	public static <T> Trie<T> emptyTrie() {
		return (Trie<T>) EMPTY_TRIE;
	}

	private static <K, V> Entry<K, V> mapEntry(K k, V v) {
    return new AbstractMap.SimpleEntry<K, V>(k, v);
	}

	protected static <E> Iterator<E> singletonIterator(E element) {
		return new SingletonIterator<E>(element);
	}

	public static <V> Trie<V> singletonTrieMap(String key, V value) {
		return new SingletonTrieMap<V>(key, value);
	}

	public static Trie<String> singletonTrieSet(String element) {
		return new SingletonTrieSet(element);
	}

	private Tries() {
	}
}
