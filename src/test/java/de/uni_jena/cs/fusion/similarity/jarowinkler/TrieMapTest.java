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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

public class TrieMapTest {

	@Test
	public void childrenIterator() {
		TrieMap<String> tireMap = new TrieMap<>();

		tireMap.put("a", "");
		tireMap.put("b", "");
		tireMap.put("c", "");
		tireMap.put("d", "");
		tireMap.put("e", "");

		Iterator<? extends Trie<String>> iterator = tireMap.childrenIterator();

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
		TrieMap<String> trieMap = new TrieMap<>();

		Collection<Integer> lengths = trieMap.containedLengths();
		assertTrue(lengths.isEmpty());

		trieMap.put("", "");
		lengths = trieMap.containedLengths();
		assertEquals(1, lengths.size());
		assertTrue(lengths.contains(0));

		trieMap.put("a", "");
		lengths = trieMap.containedLengths();
		assertEquals(2, lengths.size());
		assertTrue(lengths.contains(0));
		assertTrue(lengths.contains(1));

		trieMap.put("ab", "");
		lengths = trieMap.containedLengths();
		assertEquals(3, lengths.size());
		assertTrue(lengths.contains(0));
		assertTrue(lengths.contains(1));
		assertTrue(lengths.contains(2));
	}

	@Test
	public void containsLength() {
		TrieMap<String> trieMap = new TrieMap<>();

		assertFalse(trieMap.containsLength(0));
		assertFalse(trieMap.containsLength(1));
		assertFalse(trieMap.containsLength(2));

		trieMap.put("", "");
		assertTrue(trieMap.containsLength(0));
		assertFalse(trieMap.containsLength(1));
		assertFalse(trieMap.containsLength(2));

		trieMap.put("a", "");
		assertTrue(trieMap.containsLength(0));
		assertTrue(trieMap.containsLength(1));
		assertFalse(trieMap.containsLength(2));

		trieMap.put("ab", "");
		assertTrue(trieMap.containsLength(0));
		assertTrue(trieMap.containsLength(1));
		assertTrue(trieMap.containsLength(2));
	}

	@Test
	public void depth() {
		TrieMap<String> root = new TrieMap<>();

		root.put("a", "a");
		root.put("ab", "ab");
		root.put("b", "b");

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
	public void get() {
		TrieMap<String> trieMap = new TrieMap<>();

		// Case 1: empty map
		assertNull(trieMap.get(""));
		assertNull(trieMap.get("x"));
		assertNull(trieMap.get("xy"));
		assertNull(trieMap.get("xyz"));

		// Case 2: map not empty
		trieMap.put("xy", "1");
		assertNull(trieMap.get(""));
		assertNull(trieMap.get("x"));
		assertEquals("1", trieMap.get("xy"));
		assertNull(trieMap.get("xyz"));
	}

	@Test
	public void isPopulated() {
		TrieMap<String> trieMap = new TrieMap<>();

		trieMap.put("aa", "");
		trieMap.put("ab", "");

		Trie<String> aNode = trieMap.childrenIterator().next();

		assertFalse(aNode.isPopulated());
		trieMap.put("a", "");
		assertTrue(aNode.isPopulated());
	}

	@Test
	public void key() {
		TrieMap<String> root = new TrieMap<>();

		root.put("a", "a");
		root.put("ab", "ab");
		root.put("b", "b");

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
		TrieMap<String> root = new TrieMap<>();

		root.put("a", "a");
		root.put("ab", "ab");
		root.put("b", "b");

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
	public void put() {
		TrieMap<String> trieMap = new TrieMap<>();

		// Case 1: empty map
		assertNull(trieMap.put("xyz", "1"));
		assertEquals("1", trieMap.get("xyz"));

		// Case 2: key not contained
		assertNull(trieMap.put("abcde", "2"));
		assertEquals("2", trieMap.get("abcde"));

		// Case 3: key contained without node
		assertNull(trieMap.put("abc", "3"));
		assertEquals("3", trieMap.get("abc"));

		// Case 4: key contained with node
		assertEquals("3", trieMap.put("abc", "4"));
		assertEquals("4", trieMap.get("abc"));

		// Case 5: put empty string
		assertNull(trieMap.put("", "5"));
		assertEquals("5", trieMap.get(""));

		// Case 6: key not contained and will be last child
		assertNull(trieMap.put("y", "6"));
		assertEquals("6", trieMap.get("y"));

		// Case 7: key not contained and will be sibling of contained key with same
		// length
		assertNull(trieMap.put("abcdf", "7"));
		assertEquals("7", trieMap.get("abcdf"));

		// Case 8: key not contained and will be sibling of contained key with other
		// length
		assertNull(trieMap.put("abgh", "8"));
		assertEquals("8", trieMap.get("abgh"));

		// Case 9: value null
		assertNull(trieMap.put("null", null));
		assertNull(trieMap.get("null"));
	}

	@Test
	public void putAll() {
		TrieMap<String> trieMap = new TrieMap<>();

		// case 1: not sorted map
		Map<String, String> unsortedMap = new LinkedHashMap<String, String>();
		unsortedMap.put("xyz", "1");
		unsortedMap.put("abcde", "2");
		unsortedMap.put("abc", "4");
		unsortedMap.put("", "5");
		unsortedMap.put("y", "6");
		unsortedMap.put("abcdf", "7");
		unsortedMap.put("abgh", "8");

		trieMap = new TrieMap<>(unsortedMap);

		assertEquals("1", trieMap.get("xyz"));
		assertEquals("2", trieMap.get("abcde"));
		assertEquals("4", trieMap.get("abc"));
		assertEquals("5", trieMap.get(""));
		assertEquals("6", trieMap.get("y"));
		assertEquals("7", trieMap.get("abcdf"));
		assertEquals("8", trieMap.get("abgh"));

		// case 2: sorted map
		SortedMap<String, String> sortedMap = new TreeMap<String, String>(unsortedMap);

		trieMap = new TrieMap<>(sortedMap);

		assertEquals("1", trieMap.get("xyz"));
		assertEquals("2", trieMap.get("abcde"));
		assertEquals("4", trieMap.get("abc"));
		assertEquals("5", trieMap.get(""));
		assertEquals("6", trieMap.get("y"));
		assertEquals("7", trieMap.get("abcdf"));
		assertEquals("8", trieMap.get("abgh"));
	}

	@Test
	public void size() {
		TrieMap<String> trieMap = new TrieMap<>();

		// initial
		assertEquals(0, trieMap.size());

		// after put case 1: empty map
		trieMap.put("xyz", "1");
		assertEquals(1, trieMap.size());

		// after put case 2: key not contained
		trieMap.put("abcde", "2");
		assertEquals(2, trieMap.size());

		// after put case 3: key contained without node
		trieMap.put("abc", "3");
		assertEquals(3, trieMap.size());

		// after put case 4: key contained with node
		trieMap.put("abc", "4");
		assertEquals(3, trieMap.size());

		// after put case 5: put empty string
		trieMap.put("", "5");
		assertEquals(4, trieMap.size());
	}

	@Test
	public void symbol() {
		TrieMap<String> root = new TrieMap<>();

		root.put("a", "a");
		root.put("ab", "ab");
		root.put("b", "b");

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
		TrieMap<String> root = new TrieMap<>();

		root.put("a", "a");
		root.put("ab", "ab");
		root.put("b", "b");

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
		System.out.println(String.format("Memory consumption of dataset 1 in %s: %s byte", TrieMap.class.getName(),
				MemoryConsumption.of(() -> {
					TrieMap<String> trie = new TrieMap<>();
					try (BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(
									new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream(
											"dataset1/dbpedia_2016-10_persondata_en_names_unique_sorted.gz")),
									"UTF8"))) {
						String line = null;
						while ((line = bufferedReader.readLine()) != null) {
							trie.put(line, line);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return trie;
				})));
	}

}
