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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

import de.uni_jena.cs.fusion.similarity.jarowinkler.Trie;
import de.uni_jena.cs.fusion.similarity.jarowinkler.Tries;

public class TriesTest {

	@Test
	public void emptyTrie() {

		Trie<?> trie = Tries.emptyTrie();

		assertFalse(trie.childrenIterator().hasNext());

		assertEquals(0, trie.containedLengths().size());

		assertFalse(trie.containsLength(0));
		assertFalse(trie.containsLength(1));
		assertFalse(trie.containsLength(2));

		assertEquals(0, trie.depth());

		assertFalse(trie.equals(Tries.singletonTrieSet("")));
		assertFalse(trie.equals(Tries.singletonTrieSet("a")));
		assertFalse(trie.equals(Tries.singletonTrieSet("b")));
		assertFalse(trie.equals(Tries.singletonTrieSet("aa")));

		assertFalse(trie.isPopulated());

		assertEquals("", trie.key());

		assertEquals(0, trie.keyLength());

		assertFalse(trie.populatedNodeIterator().hasNext());

		assertEquals(0, trie.size());

		assertEquals("", trie.symbol());

		assertThrows(NoSuchElementException.class, () -> {
			trie.value();
		});
	}

	@Test
	public void singletonTrieSet() {

		Trie<String> trie = Tries.singletonTrieSet("a");

		assertFalse(trie.childrenIterator().hasNext());

		assertEquals(1, trie.containedLengths().size());
		assertTrue(trie.containedLengths().contains(1));

		assertFalse(trie.containsLength(0));
		assertTrue(trie.containsLength(1));
		assertFalse(trie.containsLength(2));

		assertEquals(0, trie.depth());

		assertFalse(trie.equals(Tries.singletonTrieSet("")));
		assertTrue(trie.equals(Tries.singletonTrieSet("a")));
		assertFalse(trie.equals(Tries.singletonTrieSet("b")));
		assertFalse(trie.equals(Tries.singletonTrieSet("aa")));

		assertTrue(trie.isPopulated());

		assertEquals("a", trie.key());

		assertEquals(1, trie.keyLength());

		Iterator<? extends Trie<?>> nodeIterator = trie.populatedNodeIterator();
		assertTrue(nodeIterator.hasNext());
		assertEquals(trie, nodeIterator.next());
		assertFalse(nodeIterator.hasNext());

		assertEquals(1, trie.size());

		assertEquals("a", trie.symbol());

		assertEquals("a", trie.value());

	}

	@Test
	public void singletonTrieMap() {

		Trie<String> trie = Tries.singletonTrieMap("a", "z");

		assertFalse(trie.childrenIterator().hasNext());

		assertEquals(1, trie.containedLengths().size());
		assertTrue(trie.containedLengths().contains(1));

		assertFalse(trie.containsLength(0));
		assertTrue(trie.containsLength(1));
		assertFalse(trie.containsLength(2));

		assertEquals(0, trie.depth());

		assertFalse(trie.equals(Tries.singletonTrieMap("", "")));
		assertTrue(trie.equals(Tries.singletonTrieMap("a", "z")));
		assertFalse(trie.equals(Tries.singletonTrieMap("b", "z")));
		assertFalse(trie.equals(Tries.singletonTrieMap("a", "y")));
		assertFalse(trie.equals(Tries.singletonTrieMap("aa", "z")));

		assertTrue(trie.isPopulated());

		assertEquals("a", trie.key());

		assertEquals(1, trie.keyLength());

		Iterator<? extends Trie<?>> nodeIterator = trie.populatedNodeIterator();
		assertTrue(nodeIterator.hasNext());
		assertEquals(trie, nodeIterator.next());
		assertFalse(nodeIterator.hasNext());

		assertEquals(1, trie.size());

		assertEquals("a", trie.symbol());

		assertEquals("z", trie.value());
	}

	private void assertThrows(Class<? extends Exception> expected, Runnable runnable) {
		try {
			runnable.run();
			Assert.fail();
		} catch (Exception e) {
			assertTrue(expected.isAssignableFrom(e.getClass()));
		}
	}
}
