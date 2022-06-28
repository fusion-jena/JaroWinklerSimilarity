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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;

/**
 * 
 * @author Jan Martin Keil
 * @since 0.1
 *
 * @param <V> the type of mapped values
 */
class TrieMap<V> implements Trie<V> {

	// content
	private V value = null;
	private String symbol = "";
	private boolean contained = false;

	// navigation
	private TrieMap<V> parent = null;
	private List<TrieMap<V>> children = new ArrayList<TrieMap<V>>();
	private List<? extends Trie<V>> childrenUnmodifiable = Collections.unmodifiableList(children);

	// performance
	private int size = 0;
	private int depth = 0;
	private BitSet lengths = new BitSet();

	public TrieMap() {
	}

	public TrieMap(Map<String, V> m) {
		this.putAll(m);
	}

	@Override
	public Iterator<? extends Trie<V>> childrenIterator() {
		return childrenUnmodifiable.iterator();
	}

	@Override
	public Collection<Integer> containedLengths() {
		Collection<Integer> result = new ArrayList<Integer>(this.lengths.cardinality());
		int length = this.lengths.nextSetBit(0);
		while (length != -1) {
			result.add(length);
			length = this.lengths.nextSetBit(length + 1);
		}
		return result;
	}

	@Override
	public boolean containsLength(int length) {
		return this.lengths.get(length);
	}

	@Override
	public int depth() {
		return this.depth;
	}

	V get(Object key) {
		if (key instanceof String) {
			TrieMap<V> node = this.getNode((String) key);
			if (node != null) {
				// NOTE: do not use ::value() to avoid exception
				return node.value;
			}
		}
		return null;
	}

	/**
	 * Returns the node indicated by the given key, or null.
	 * 
	 * @param suffix
	 * @return
	 */
	private TrieMap<V> getNode(String key) {
		TrieMap<V> candidate = getClosestNode(key);
		if (candidate.keyLength() == this.depth() + key.length() && key.endsWith(candidate.symbol())) {
			return candidate;
		} else {
			return null;
		}
	}

	/**
	 * Returns the closest node to the given key. There are four possible final
	 * states:
	 * <dl>
	 * <dt>Key of the returned node equals the given key</dt>
	 * <dd>The given key is contained. The returned node represents the given
	 * key.</dd>
	 * <dt>Key of the returned node is a prefix of the given key</dt>
	 * <dd>The given key is not contained yet. The returned node would be the parent
	 * of the node representing the given key, after it has been inserted.</dd>
	 * <dt>Given key is a prefix of the key of the returned node</dt>
	 * <dd>The given key is contained, but only as part of an other key and is not
	 * represented by an particular node. The returned node would be a child of the
	 * node representing the given key, after it has been inserted.</dd>
	 * <dt>Given key and key of the returned node have common prefix</dt>
	 * <dd>The given key is not contained yet. The returned node would be a sibling
	 * of the node representing the given key, after is has been inserted.</dd>
	 * </dl>
	 * 
	 * @param key key to search
	 * @return closest node to the given key
	 */
	private TrieMap<V> getClosestNode(String key) {
		if (key.startsWith(this.key())) {
			key = key.substring(this.keyLength());
			if (this.children.isEmpty()) {
				return this;
			} else if (key.length() == 0) {
				return this;
			} else {
				Iterator<TrieMap<V>> iterator = this.children.iterator();
				while (iterator.hasNext()) {
					TrieMap<V> current = iterator.next();
					if (current.symbol().charAt(0) == key.charAt(0)) {
						// current has appropriate first char
						if (key.startsWith(current.symbol())) {
							// current has appropriate symbol
							key = key.substring(current.symbol().length());
							if (key.length() == 0) {
								// current node symbol equals given suffix
								return current;
							} else {
								// current node symbol is prefix of given
								// suffix
								if (current.children.isEmpty()) {
									// current node has no children
									return current;
								} else {
									// current node has children

									// start next level iterator
									iterator = current.children.iterator();
								}
							}
						} else {
							// current has appropriate first char but not symbol
							return current;
						}
					} else if (current.symbol().charAt(0) > key.charAt(0)) {
						// parent not contains child with appropriate first char
						return current.parent;
					} else {
						if (!iterator.hasNext()) {
							return current.parent;
						}
						// else continue loop
					}
				}
			}
		} else {
			throw new RuntimeException("Failed to get closest node: Key out of key scope of this.");
		}
		throw new RuntimeException("Failed to get closest node: Unexpected state.");
	}

	@Override
	public boolean isPopulated() {
		return this.contained;
	}

	@Override
	public String key() {
		if (parent == null) {
			return this.symbol;
		} else {
			return this.parent.key() + this.symbol;
		}
	}

	V put(String key, V value) {
		TrieMap<V> node = getClosestNode(key);
		String nodeKey = node.key();
		if (key.equals(nodeKey)) {
			// got appropriate node
			return node.setValue(value);
		} else if (key.startsWith(nodeKey)) {
			// got parent node
			node.addChild(key, value, true);
			return null;
		} else if (nodeKey.startsWith(key)) {
			// got child node
			node.splitNode(key, value, true);
			return null;
		} else {
			// got sibling
			node.addSibling(key, value);
			return null;
		}
	}

	private TrieMap<V> addSibling(String key, V value) {
		StringBuilder prefix = new StringBuilder(this.parent.key());
		for (int i = this.depth(); i < Math.min(this.keyLength(), key.length())
				&& key.charAt(i) == this.symbol().charAt(i - this.depth()); i++) {
			prefix.append(key.charAt(i));
		}
		return this.splitNode(prefix.toString(), null, false).addChild(key, value, true);
	}

	private V setValue(V value) {
		V prev = this.value;
		this.value = value;
		this.contained = true;
		this.updateSizeAndLength();
		return prev;
	}

	private TrieMap<V> splitNode(String key, V value, boolean contained) {
		// update parent
		this.parent.children.remove(this);
		TrieMap<V> node = this.parent.addChild(key, value, contained);

		// update new node
		node.children.add(this);
		node.size += this.size;
		node.lengths.or(this.lengths);

		// update this
		this.depth = node.keyLength();
		this.symbol = this.symbol.substring(node.symbol.length());
		this.parent = node;

		return node;
	}

	private TrieMap<V> addChild(String key, V value, boolean contained) {
		// create node
		TrieMap<V> node = new TrieMap<V>();
		node.symbol = key.substring(this.keyLength());
		node.value = value;
		node.contained = contained;
		node.parent = this;
		node.depth = this.keyLength();
		node.updateSizeAndLength();

		// add node to parent
		if (this.children.isEmpty()) {
			this.children.add(node);
		} else {
			ListIterator<TrieMap<V>> siblings = this.children.listIterator();
			while (siblings.hasNext()) {
				TrieMap<V> sibling = siblings.next();
				if (sibling.symbol().charAt(0) > node.symbol.charAt(0)) {
					siblings.previous();
					siblings.add(node);
					break;
				} else if (!siblings.hasNext()) {
					siblings.add(node);
					break;
				}
			}
		}

		return node;
	}

	private void updateSizeAndLength() {
		if (this.contained && !this.lengths.get(this.keyLength())) {
			TrieMap<V> node = this;
			while (node != null) {
				node.lengths.set(this.keyLength());
				node.size++;
				node = node.parent;
			}
		}
	}

	void putAll(Map<? extends String, ? extends V> m) {
		if (m instanceof SortedMap<?, ?>) {
			SortedMap<? extends String, ? extends V> sorted = (SortedMap<? extends String, ? extends V>) m;
			// TODO make use of sorting
			for (Map.Entry<? extends String, ? extends V> entry : sorted.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}
		} else {
			for (Map.Entry<? extends String, ? extends V> entry : m.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}
		}

	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public String symbol() {
		return this.symbol;
	}

	@Override
	public V value() throws NoSuchElementException {
		if (this.isPopulated()) {
			return this.value;
		} else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		Iterator<? extends Trie<V>> iterator = this.populatedNodeIterator();
		while (iterator.hasNext()) {
			Trie<V> node = iterator.next();
			builder.append(node.key().toString());
			builder.append('=');
			builder.append(node.value().toString());
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append('}');
		return builder.toString();
	}
}
