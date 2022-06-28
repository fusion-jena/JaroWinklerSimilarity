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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * TODO docu
 * 
 * @author Jan Martin Keil
 * @since 0.1
 */
interface Trie<V> {

	public class PopulatedNodeIterator<V> implements Iterator<Trie<V>> {

		private Stack<Iterator<? extends Trie<V>>> iteratorStack = new Stack<Iterator<? extends Trie<V>>>();
		private Trie<V> next;

		PopulatedNodeIterator(Trie<V> root) {
			this.iteratorStack.push(Tries.singletonIterator(root));
			this.prepareNext();
		}

		@Override
		public boolean hasNext() {
			return this.next != null;
		}

		@Override
		public Trie<V> next() {
			if (this.next != null) {
				Trie<V> now = this.next;
				this.prepareNext();
				return now;
			} else {
				throw new NoSuchElementException();
			}
		}

		private void prepareNext() {
			while (!this.iteratorStack.empty()) {
				if (this.iteratorStack.peek().hasNext()) {
					this.next = this.iteratorStack.peek().next();
					this.iteratorStack.push(this.next.childrenIterator());
					if (this.next.isPopulated()) {
						return;
					}
				} else {
					this.iteratorStack.pop();
				}
			}
			this.next = null;
		}
	}

	/**
	 * Returns an {@link Iterator} of the child trie nodes of this trie node
	 * 
	 * @return {@link Iterator} of the child trie nodes of this trie node
	 */
	Iterator<? extends Trie<V>> childrenIterator();

	/**
	 * Returns a {@link Collection} of the contained key lengths.
	 * 
	 * @return {@link Collection} of the contained key lengths
	 */
	Collection<Integer> containedLengths();

	/**
	 * 
	 * @param length
	 * @return {@code true} if the length of the key of this trie node or any of its
	 *         children equals the given {@code length} and that trie node is
	 *         contained, otherwise {@code false}
	 */
	default boolean containsLength(int length) {
		return this.containedLengths().contains(length);
	}

	/**
	 * Returns the depth of this trie node that is the key length of its parent trie
	 * node.
	 * 
	 * @return depth of this trie node
	 */
	int depth();

	/**
	 * Returns {@code true}, if the string represented by the current node is
	 * contained in the trie, otherwise {@code false}.
	 * 
	 * @return {@code true}, if the string represented by the current node is
	 *         contained in the trie, otherwise {@code false}
	 */
	boolean isPopulated();

	/**
	 * Returns the key of this trie node.
	 * 
	 * @return the key of this trie node
	 */
	String key();

	/**
	 * Returns the length of the key of this trie node. This might be faster than
	 * calling {@code key().length()}.
	 * 
	 * @return the length of the key of this trie node
	 */
	default int keyLength() {
		return this.depth() + this.symbol().length();
	}

	/**
	 * Returns an {@link Iterator} of the populated trie nodes.
	 * 
	 * @return {@link Iterator} of the populated trie nodes
	 */
	default Iterator<? extends Trie<V>> populatedNodeIterator() {
		return new PopulatedNodeIterator<V>(this);
	}

	int size();

	/**
	 * Returns the symbol of this trie node that is a suffix of its key.
	 * 
	 * @return symbol of this trie node
	 */
	String symbol();

	/**
	 * Returns the value of this trie node. Throws {@link NoSuchElementException},
	 * if this node is not populated.
	 * 
	 * @return value of this trie node
	 * @throws NoSuchElementException
	 */
	V value() throws NoSuchElementException;

}
