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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * @author Jan Martin Keil
 * @since 0.1
 */
class TrieSet implements Trie<String> {

	protected int size = 0;
	protected int depth = 0;
	protected boolean contained = false;
	protected BitSet lengths = new BitSet();
	protected TrieSet sibling = null;
	protected TrieSet child = null;
	protected TrieSet parent = null;
	protected String symbol = "";

	TrieSet() {
	}

	private TrieSet(String symbol, TrieSet parent) {
		this.symbol = symbol;
		this.parent = parent;
		this.depth = parent.keyLength();
	}

	public TrieSet(Collection<? extends String> c) {
		this.addAll(c);
	}

	private TrieSet(String symbol, BitSet lengths, boolean contained, int size, int depth,
			TrieSet sibling, TrieSet child, TrieSet parent) {
		this.child = child;
		this.contained = contained;
		this.depth = depth;
		this.symbol = symbol;
		this.lengths = lengths;
		this.parent = parent;
		this.sibling = sibling;
		this.size = size;
	}

	private boolean add() {
		if (this.contained) {
			// e is already contained
			return false;
		} else {
			// e is not already contained
			this.lengths.set(this.keyLength());
			this.size++;
			this.contained = true;
			return true;
		}
	}

	boolean add(String e) {
		int commonPrefixLength = commonLength(this.symbol, e);
		if (commonPrefixLength == 0 && this.symbol.length() != 0 && e.length() != 0) {
			// this and e are siblings
			if (symbol.charAt(0) < e.charAt(0)) {
				// symbol[0] < e[0]: e after symbol
				return this.addSibling(e);
			} else {
				// symbol > e: e before symbol
				this.shiftRight(e);
				return this.add();
			}
		} else {
			if (commonPrefixLength == this.symbol.length()) {
				if (commonPrefixLength == e.length()) {
					// e equals symbol
					return this.add();
				} else {
					// e contains symbol
					return this.addChild(e.substring(commonPrefixLength));
				}
			} else if (commonPrefixLength == e.length()) {
				// symbol contains e
				this.shiftDown(commonPrefixLength);
				return this.add();
			} else {
				// symbol and e have common prefix (must both be child of a
				// new node)
				this.shiftDown(commonPrefixLength);
				return this.addChild(e.substring(commonPrefixLength));
			}
		}
	}

	boolean addAll(Collection<? extends String> c) throws ClassCastException {
		// copy list
		List<String> list = new ArrayList<String>(c);
		// sort list
		list.sort(Comparator.naturalOrder());

		// initializations
		boolean collectionChanged = false;
		boolean elementNodeChanged;
		boolean elementAdded;
		String prevElement = "";
		Stack<TrieSet> nodeStack = new Stack<TrieSet>();
		nodeStack.push(this);
		Stack<Integer> keyLengthStack = new Stack<Integer>();
		keyLengthStack.push(this.keyLength());

		//// special cases ////
		if (list.size() > 0 && list.get(0).length() == 0) {
			// firstElement is empty

			// add first element
			elementAdded = this.add();
		}

		//// regular case ////
		for (String currentElement : list) {

			if (!currentElement.equals(prevElement)) {
				// not a duplicate in the list

				int currentElementLength = currentElement.length();

				// get common prefix length with prevElement
				{
					int commonPrefixLength = commonLength(currentElement, prevElement);

					// get starting node (prevent full trie traverse)
					while (keyLengthStack.peek() > commonPrefixLength) {
						nodeStack.pop();
						keyLengthStack.pop();
					}
				}
				TrieSet currentNode = nodeStack.peek();

				// update prevElement
				prevElement = currentElement;
				// remove prefix from current element
				currentElement = currentElement.substring(currentNode.keyLength());

				// reset break condition
				elementAdded = false;
				elementNodeChanged = false;

				while (!elementAdded) {
					if (currentElement.length() != 0) {
						// currentElement is not empty
						// -> target node not reached
						// -> insert currentElement into a child of currentNode

						// get first child
						TrieSet currentChild = currentNode.child;

						if (currentChild == null) {
							// current node has no children

							elementAdded = true;
							elementNodeChanged = true;
							currentNode.child = new TrieSet(currentElement, currentNode);
							currentNode.child.contained = true;
							currentNode = currentNode.child;

						} else {
							// current node has at least one child

							// iterate to relevant child
							while (currentChild.sibling != null) {
								if (currentChild.sibling.symbol.charAt(0) > currentElement.charAt(0)) {
									// symbol of next child of current node is
									// greater than currentElement
									break;
								}
								currentChild = currentChild.sibling;
							}
							// now the current child is the relevant child
							if (currentChild != null && currentChild.symbol.charAt(0) == currentElement.charAt(0)) {
								// element must be inserted at the current child
								int commonPrefixLength = commonLength(currentElement, currentChild.symbol);

								if (commonPrefixLength != currentChild.symbol.length()) {
									// element not starts with symbol of current
									// child
									// -> element must be a sibling branch of
									// the current child

									// create new branch
									currentChild.shiftDown(commonPrefixLength);
								}

								currentElement = currentElement.substring(commonPrefixLength);
								currentNode = currentChild;
							} else {
								// element must be inserted after current child
								elementNodeChanged = true;
								TrieSet newSibling = new TrieSet(currentElement, currentNode);
								newSibling.contained = true;
								newSibling.sibling = currentChild.sibling;
								currentChild.sibling = newSibling;
								currentNode = currentChild.sibling;
								currentElement = "";
							}
						}
					} else {
						// reached target node = currentElement is empty

						elementAdded = true;
						elementNodeChanged = currentNode.add();
					}
					if (nodeStack.peek() != currentNode) {
						// current element changed

						nodeStack.push(currentNode);
						keyLengthStack.push(currentNode.keyLength());
					}

					if (elementNodeChanged) {
						for (TrieSet trie : nodeStack) {
							trie.size++;
							trie.lengths.set(currentElementLength);
						}
						collectionChanged = true;
					}
				}
			}
		}

		return collectionChanged;
	}

	private boolean addChild(String e) {
		if (this.child == null) {
			// this has no child
			this.child = new TrieSet(e, this);
		}
		// add e to child
		if (this.child.add(e)) {
			this.size++;
			this.lengths.set(e.length() + this.depth);
			return true;
		} else {
			return false;
		}
	}

	private boolean addSibling(String e) {
		if (this.sibling == null) {
			// this has no child
			this.sibling = new TrieSet(e, this.parent);
		}
		// add e to child
		if (this.sibling.add(e)) {
			this.size++;
			this.lengths.set(e.length() + this.depth);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Iterator<Trie<String>> childrenIterator() {
		return new Iterator<Trie<String>>() {
			private TrieSet next = child;
			private TrieSet current = null;

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public TrieSet next() {
				current = next;
				next = next.sibling;
				return current;
			}
		};
	}

	boolean contains(Object o) {
		TrieSet node = this.getNode((String) o);
		return node != null && node.isPopulated();
	}

	@Override
	public boolean containsLength(int length) {
		return this.lengths.get(length);
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

	private TrieSet copy() {
		return new TrieSet(this.symbol, this.lengths, this.contained, this.size, this.depth, this.sibling,
				this.child, this.parent);
	}

	@Override
	public int depth() {
		return this.depth;
	}

	private TrieSet getNode(String key) {
		// catch special cases
		if (this.depth != 0) {
			// this is not the root node
			if (key.startsWith(this.key())) {
				// this.key is prefix of key = key can be contained

				// remove prefix from key
				key = key.substring(this.depth);
			} else {
				// this.key is not prefix of key = key can not be contained
				return null;
			}
		}

		// regular case
		TrieSet currentNode = this;
		while (key.length() != 0) {
			if (key.length() >= currentNode.symbol.length()
					&& key.substring(0, currentNode.symbol.length()).equals(currentNode.symbol)) {

				// remove prefix from key
				key = key.substring(currentNode.symbol.length());

				if (key.length() != 0) {
					// did not reach the target node

					// get relevant child node
					Iterator<Trie<String>> childrenIterator = currentNode.childrenIterator();
					currentNode = null;
					while (childrenIterator.hasNext()) {
						TrieSet child = (TrieSet) childrenIterator.next();
						if (child.symbol.charAt(0) == key.charAt(0)) {
							// child is the relevant child

							currentNode = child;
							break;
						}
					}

					if (currentNode == null) {
						// no relevant child was found
						return null;
					}
				}
			} else {
				// symbol of current node is prefix of key
				return null;
			}
		}
		if (currentNode.isPopulated()) {
			return currentNode;
		} else {
			return null;
		}
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

	public boolean remove(Object o) {
		// get element
		TrieSet node = this.getNode((String) o);

		if (node != null && node.isPopulated()) {
			// element is contained

			int elementLength = node.keyLength();

			// remove element
			if (node.child != null) {
				// node has child
				if (node.child.sibling != null || node.parent == null) {
					// child has sibling or node is root
					node.contained = false;
					node.size--;
				} else {
					// child has no sibling
					node.shiftUp();
				}
			} else {
				// node has no child
				if (node.sibling != null) {
					// node has sibling
					node.shiftLeft();
				} else {
					// node has no sibling
					node.parent.child = null;
				}
			}

			// update parents
			boolean lengthContained = false;

			// iterate all parents
			TrieSet currentParent = node;
			while (currentParent.parent != null) {
				currentParent = currentParent.parent;

				// decrease size
				currentParent.size--;

				if (currentParent.parent != null) {
					// current parent has parent
					if (currentParent.size <= 0) {
						// current parent node is not required anymore
						currentParent.shiftLeft();
					} else {
						// current parent node is still required

						// iterate siblings
						Iterator<Trie<String>> childenIterator = currentParent.childrenIterator();
						while (!lengthContained && childenIterator.hasNext()) {

							// check if any sibling contains the elementLength
							lengthContained = childenIterator.next().containsLength(elementLength);
						}
						if (!lengthContained) {
							// no child anymore contains the elementLength

							// update lengths
							currentParent.lengths.clear(elementLength);
						}
					}
				}
			}
			return true;
		} else {
			// element is not contained
			return false;
		}
	}

	/**
	 * Split the symbol of this node and move the content of this node into a new
	 * child node.
	 * 
	 * @param i position to split the symbol
	 */
	private void shiftDown(int i) {
		this.child = this.copy();
		this.contained = false;
		// this.depth does not change
		this.symbol = this.symbol.substring(0, i);
		this.lengths = (BitSet) this.lengths.clone();
		// this.parent does not change
		// this.sibling does not change
		// this.size does not change

		// this.child.child does not change
		// this.child.contained does not change
		this.child.depth = this.depth + i;
		this.child.symbol = this.child.symbol.substring(i);
		// this.child.lengths does not change
		this.child.sibling = null;
		this.child.parent = this; // TODO also update siblings
		// this.child.size does not change

		Iterator<Trie<String>> grandchildren = this.child.childrenIterator();
		while (grandchildren.hasNext()) {
			TrieSet grandchild = (TrieSet) grandchildren.next();
			grandchild.parent = this.child;
		}
	}

	/**
	 * Move the content of this trie node into a new sibling node and give this trie
	 * node a new symbol. The resulting new key is not contained in the trie.
	 * 
	 * @param e new symbol of this node
	 */
	private void shiftRight(String e) {
		// TODO create new trie node for e and update parents or siblings
		// pointer
		this.sibling = this.copy();
		this.child = null;
		this.contained = false;
		// this.depth does not change
		this.symbol = e;
		this.lengths = new BitSet();
		// this.parent does not change
		this.size = 0;
	}

	/**
	 * <p>
	 * Replaces this trie node by its sibling trie node.
	 * </p>
	 * <p>
	 * <b>Attention:</b><br>
	 * If the trie node has children or represents a contained key, they will
	 * <em>get lost</em>. <code>size</code> and <code>lengths</code> of parent nodes
	 * will <em>not</em> be updated.
	 * </p>
	 */
	private void shiftLeft() {
		if (this.parent.child == this) {
			// this is the first child of the parent

			// update pointer on this node to the next sibling
			this.parent.child = this.sibling;
		} else {
			// this is not the first child of the parent

			// get sibling trie node pointing on this trie node
			TrieSet prevSibling = this.parent.child;
			while (prevSibling.sibling != this) {
				prevSibling = prevSibling.sibling;
			}

			// update pointer on this trie node to the next sibling
			prevSibling.sibling = this.sibling;

		}
		// TODO add a removed flag
	}

	/**
	 * <p>
	 * Replaces this trie node by its first child trie node.
	 * </p>
	 * <p>
	 * <b>Attention:</b><br>
	 * If this node has more than one child all children except the first child will
	 * get lost. If this trie node is contained, that information will get lost.
	 * <code>size</code> and <code>lengths</code> of parent nodes will <em>not</em>
	 * be updated.
	 * </p>
	 */
	private void shiftUp() {
		if (this.parent.child == this) {
			// this is the first child of the parent

			// update pointer on this node to the next sibling
			this.parent.child = this.child;
		} else {
			// this is not the first child of the parent

			// get sibling trie node pointing on this trie node
			TrieSet prevSibling = this.parent.child;
			while (prevSibling.sibling != this) {
				prevSibling = prevSibling.sibling;
			}

			// update pointer on this trie node to the next sibling
			prevSibling.sibling = this.child;

		}
		this.child.sibling = this.sibling;
		this.child.parent = this.parent;
		// TODO add a removed flag
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
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		Iterator<? extends Trie<String>> iterator = this.populatedNodeIterator();
		while (iterator.hasNext()) {
			Trie<String> node = iterator.next();
			builder.append(node.key().toString());
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append(']');
		return builder.toString();
	}

	private static int commonLength(String s, String t) {
		if (s.length() < t.length()) {
			String swap = s;
			s = t;
			t = swap;
		}
		int i;
		for (i = 0; i < t.length(); i++) {
			if (s.charAt(i) != t.charAt(i)) {
				break;
			}
		}
		return i;
	}

	@Override
	public String value() throws NoSuchElementException {
		if (this.isPopulated()) {
			return this.key();
		} else {
			throw new NoSuchElementException();
		}
	}
}
