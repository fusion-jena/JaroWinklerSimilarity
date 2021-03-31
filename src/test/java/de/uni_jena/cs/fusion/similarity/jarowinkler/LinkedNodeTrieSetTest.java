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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

/**
 * @author Jan Martin Keil
 * @since 0.1
 */
public class LinkedNodeTrieSetTest {

	@Test
	public void add() {
		LinkedNodeTrieSet trieSet = new LinkedNodeTrieSet();

		// Case 1: empty map
		assertTrue(trieSet.add("xyz"));
		assertTrue(trieSet.contains("xyz"));

		// Case 2: key not contained
		assertTrue(trieSet.add("abcde"));
		assertTrue(trieSet.contains("abcde"));

		// Case 3: key contained without node
		assertTrue(trieSet.add("abc"));
		assertTrue(trieSet.contains("abc"));

		// Case 4: key contained with node
		assertFalse(trieSet.add("abc"));
		assertTrue(trieSet.contains("abc"));

		// Case 5: put empty string
		assertTrue(trieSet.add(""));
		assertTrue(trieSet.contains(""));

		// Case 6: key not contained and will be last child
		assertTrue(trieSet.add("y"));
		assertTrue(trieSet.contains("y"));

		// Case 7: key not contained and sibling of contained key with same length
		assertTrue(trieSet.add("abcdf"));
		assertTrue(trieSet.contains("abcdf"));

		// Case 8: key not contained and sibling of contained key with other length
		assertTrue(trieSet.add("abgh"));
		assertTrue(trieSet.contains("abgh"));
	}

	@Test
	public void addAll() {
		LinkedNodeTrieSet trieSet = new LinkedNodeTrieSet();

		// case 1: not sorted map
		Set<String> unsortedSet = new LinkedHashSet<String>();
		unsortedSet.add("xyz");
		unsortedSet.add("abcde");
		unsortedSet.add("abc");
		unsortedSet.add("");
		unsortedSet.add("y");
		unsortedSet.add("abcdf");
		unsortedSet.add("abgh");

		trieSet = new LinkedNodeTrieSet(unsortedSet);

		assertTrue(trieSet.contains("xyz"));
		assertTrue(trieSet.contains("abcde"));
		assertTrue(trieSet.contains("abc"));
		assertTrue(trieSet.contains(""));
		assertTrue(trieSet.contains("y"));
		assertTrue(trieSet.contains("abcdf"));
		assertTrue(trieSet.contains("abgh"));

		// case 2: sorted map
		SortedSet<String> sortedSet = new TreeSet<String>(unsortedSet);

		trieSet = new LinkedNodeTrieSet(sortedSet);

		assertTrue(trieSet.contains("xyz"));
		assertTrue(trieSet.contains("abcde"));
		assertTrue(trieSet.contains("abc"));
		assertTrue(trieSet.contains(""));
		assertTrue(trieSet.contains("y"));
		assertTrue(trieSet.contains("abcdf"));
		assertTrue(trieSet.contains("abgh"));
	}

	@Test
	public void addAllIssue1() {
		// according to https://github.com/fusion-jena/JaroWinklerSimilarity/issues/1
		List<String> list = Arrays.asList("d", "dindy", "impasse");
		LinkedNodeTrieSet trie = new LinkedNodeTrieSet(list);
		assertTrue(trie.contains("impasse"));
	}

	@Test
	public void childrenIterator() {
		LinkedNodeTrieSet tireSet = new LinkedNodeTrieSet();

		tireSet.add("a");
		tireSet.add("b");
		tireSet.add("c");
		tireSet.add("d");
		tireSet.add("e");

		Iterator<? extends Trie<String>> iterator = tireSet.childrenIterator();

		assertTrue(iterator.hasNext());
		assertEquals("a", iterator.next().key());
		assertTrue(iterator.hasNext());
		assertEquals("b", iterator.next().key());
		assertTrue(iterator.hasNext());
		assertEquals("c", iterator.next().key());
		assertTrue(iterator.hasNext());
		assertEquals("d", iterator.next().key());
		assertTrue(iterator.hasNext());
		assertEquals("e", iterator.next().key());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void containedLengths() {
		LinkedNodeTrieSet trieSet = new LinkedNodeTrieSet();

		Collection<Integer> lengths = trieSet.containedLengths();
		assertTrue(lengths.isEmpty());

		trieSet.add("");
		lengths = trieSet.containedLengths();
		assertEquals(1, lengths.size());
		assertTrue(lengths.contains(0));

		trieSet.add("a");
		lengths = trieSet.containedLengths();
		assertEquals(2, lengths.size());
		assertTrue(lengths.contains(0));
		assertTrue(lengths.contains(1));

		trieSet.add("ab");
		lengths = trieSet.containedLengths();
		assertEquals(3, lengths.size());
		assertTrue(lengths.contains(0));
		assertTrue(lengths.contains(1));
		assertTrue(lengths.contains(2));
	}

	@Test
	public void containsLength() {
		LinkedNodeTrieSet trieSet = new LinkedNodeTrieSet();

		assertFalse(trieSet.containsLength(0));
		assertFalse(trieSet.containsLength(1));
		assertFalse(trieSet.containsLength(2));

		trieSet.add("");
		assertTrue(trieSet.containsLength(0));
		assertFalse(trieSet.containsLength(1));
		assertFalse(trieSet.containsLength(2));

		trieSet.add("a");
		assertTrue(trieSet.containsLength(0));
		assertTrue(trieSet.containsLength(1));
		assertFalse(trieSet.containsLength(2));

		trieSet.add("ab");
		assertTrue(trieSet.containsLength(0));
		assertTrue(trieSet.containsLength(1));
		assertTrue(trieSet.containsLength(2));
	}

	@Test
	public void depth() {
		LinkedNodeTrieSet root = new LinkedNodeTrieSet();

		root.add("a");
		root.add("ab");
		root.add("b");

		assertEquals(0, root.depth());
		Iterator<? extends Trie<String>> rootIterator = root.childrenIterator();
		Trie<String> aNode = rootIterator.next();
		assertEquals(0, aNode.depth());
		Iterator<? extends Trie<String>> aIterator = aNode.childrenIterator();
		Trie<String> abNode = aIterator.next();
		assertEquals(1, abNode.depth());
		Trie<String> bNode = rootIterator.next();
		assertEquals(0, bNode.depth());
	}

	@Test
	public void isPopulated() {
		LinkedNodeTrieSet trieSet = new LinkedNodeTrieSet();

		trieSet.add("aa");
		trieSet.add("ab");

		Trie<String> aNode = trieSet.childrenIterator().next();

		assertFalse(aNode.isPopulated());
		trieSet.add("a");
		assertTrue(aNode.isPopulated());
	}

	@Test
	public void key() {
		LinkedNodeTrieSet root = new LinkedNodeTrieSet();

		root.add("a");
		root.add("ab");
		root.add("b");

		assertEquals("", root.key());
		Iterator<? extends Trie<String>> rootIterator = root.childrenIterator();
		Trie<String> aNode = rootIterator.next();
		assertEquals("a", aNode.key());
		Iterator<? extends Trie<String>> aIterator = aNode.childrenIterator();
		Trie<String> abNode = aIterator.next();
		assertEquals("ab", abNode.key());
		Trie<String> bNode = rootIterator.next();
		assertEquals("b", bNode.key());
	}

	@Test
	public void keyLength() {
		LinkedNodeTrieSet root = new LinkedNodeTrieSet();

		root.add("a");
		root.add("ab");
		root.add("b");

		assertEquals(0, root.keyLength());
		Iterator<? extends Trie<String>> rootIterator = root.childrenIterator();
		Trie<String> aNode = rootIterator.next();
		assertEquals(1, aNode.keyLength());
		Iterator<? extends Trie<String>> aIterator = aNode.childrenIterator();
		Trie<String> abNode = aIterator.next();
		assertEquals(2, abNode.keyLength());
		Trie<String> bNode = rootIterator.next();
		assertEquals(1, bNode.keyLength());
	}

	@Test
	public void size() {
		LinkedNodeTrieSet trieSet = new LinkedNodeTrieSet();

		// initial
		assertEquals(0, trieSet.size());

		// after put case 1: empty map
		trieSet.add("xyz");
		assertEquals(1, trieSet.size());

		// after put case 2: key not contained
		trieSet.add("abcde");
		assertEquals(2, trieSet.size());

		// after put case 3: key contained without node
		trieSet.add("abc");
		assertEquals(3, trieSet.size());

		// after put case 4: key contained with node
		trieSet.add("abc");
		assertEquals(3, trieSet.size());

		// after put case 5: put empty string
		trieSet.add("");
		assertEquals(4, trieSet.size());
	}

	@Test
	public void symbol() {
		LinkedNodeTrieSet root = new LinkedNodeTrieSet();

		root.add("a");
		root.add("ab");
		root.add("b");

		assertEquals("", root.symbol());
		Iterator<? extends Trie<String>> rootIterator = root.childrenIterator();
		Trie<String> aNode = rootIterator.next();
		assertEquals("a", aNode.symbol());
		Iterator<? extends Trie<String>> aIterator = aNode.childrenIterator();
		Trie<String> abNode = aIterator.next();
		assertEquals("b", abNode.symbol());
		Trie<String> bNode = rootIterator.next();
		assertEquals("b", bNode.symbol());
	}

	@Test
	public void value() {
		LinkedNodeTrieSet root = new LinkedNodeTrieSet();

		root.add("a");
		root.add("ab");
		root.add("b");

		Iterator<? extends Trie<String>> rootIterator = root.childrenIterator();
		Trie<String> aNode = rootIterator.next();
		assertEquals("a", aNode.value());
		Iterator<? extends Trie<String>> aIterator = aNode.childrenIterator();
		Trie<String> abNode = aIterator.next();
		assertEquals("ab", abNode.value());
		Trie<String> bNode = rootIterator.next();
		assertEquals("b", bNode.value());
	}

	@Test
	public void memoryConsumption() throws IOException {
		System.out.println(String.format("Memory consumption of dataset 1 in %s: %s byte",
				LinkedNodeTrieSet.class.getName(), MemoryConsumption.of(() -> {
					LinkedNodeTrieSet trie = new LinkedNodeTrieSet();
					try (BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(
									new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream(
											"dataset1/dbpedia_2016-10_persondata_en_names_unique_sorted.gz")),
									"UTF8"))) {
						String line = null;
						while ((line = bufferedReader.readLine()) != null) {
							trie.add(line);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return trie;
				})));
	}
}
