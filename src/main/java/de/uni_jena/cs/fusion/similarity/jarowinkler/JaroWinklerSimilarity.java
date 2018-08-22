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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * Matches the content of a prepared {@link Map} or {@link Set} against given
 * queries using the Jaro Winkler similarity.
 * </p>
 * <p>
 * <b>Acknowledgments:</b> The development of this Jaro Winkler Similarity
 * implementation was funded by DFG in the scope of the LakeBase project within
 * the Scientific Library Services and Information Systems (LIS) program.
 * </p>
 * 
 * @param <T>
 *            Type of the ranked values returned by the matching.
 * 
 * @author Jan Martin Keil
 * @since 0.1
 */
public class JaroWinklerSimilarity<T> {
	public final static int COMMON_PREFIX_LENGTH_LIMIT = 4;
	public final static double BOOST_THRESHOLD = 0.7;
	public final static double BOOST_FACTOR = 0.1;

	private Trie<T> trie;

	private JaroWinklerSimilarity(Trie<T> trie) {
		this.trie = trie;
	}

	/**
	 * Returns the Jaro Winkler similarity of two given {@link String}s.
	 * 
	 * <b>Note:</b> If a {@link threshold} can be provided, it is recommended to use
	 * {@link #calculate(String, String, double)} to improve performance.
	 * 
	 * @param first
	 *            First {@link String} to match.
	 * @param second
	 *            Second {@link String} to match.
	 * @return Jaro Winkler similarity of the given {@link String}s.
	 * 
	 * @see {@link #calculate(String, String, double)}
	 * 
	 * @author Jan Martin Keil
	 * @since 0.1
	 */
	public static Double calculate(String first, String second) {
		return calculate(first, second, 0.0d);
	}

	/**
	 * Returns the Jaro Winkler similarity of the given {@link String}s.
	 * 
	 * <b>Note:</b> For the calculation of the similarity of one {@link String} to a
	 * number of {@link String}s it is recommended to use
	 * {@link #match(String, double)} to improve performance.
	 * 
	 * @param first
	 *            First {@link String} to match.
	 * @param second
	 *            Second {@link String} to match.
	 * @param threshold
	 *            Minimum similarity of the given {@link String}s.
	 * @return Jaro Winkler similarity of the given {@link String}s or {@code null}
	 *         if the given {@link String}s do not meet the threshold.
	 * 
	 * 
	 * @see {@link #calculate(String, String)}
	 * 
	 * @author Jan Martin Keil
	 * @since 0.1
	 */
	public static Double calculate(String first, String second, double threshold) {
		return new JaroWinklerSimilarity<String>(Tries.singletonTrieSet(first)).match(second, threshold).get(first);
	}

	/**
	 * 
	 * Prepares a {@link JaroWinklerSimilarity} instance to match the content of the
	 * given {@link Map}. The matching will search for similar keys, but return the
	 * corresponding values. The created {@link JaroWinklerSimilarity} is not backed
	 * by the {@link Map}, so it will not reflect changes of the {@link Map}.
	 * 
	 * @param terms
	 *            {@link Map} of matched terms and returned values.
	 * @return A {@link JaroWinklerSimilarity} instance to match the content of the
	 *         given {@link Map}.
	 * 
	 * @param <T>
	 *            Type of the map values and returned values by the matching.
	 * 
	 * @author Jan Martin Keil
	 * @since 0.1
	 */
	public static <T> JaroWinklerSimilarity<T> of(Map<String, T> terms) {
		return new JaroWinklerSimilarity<T>(new LinkedListTrieMap<T>(terms));
	}

	/**
	 * Prepares a {@link JaroWinklerSimilarity} instance to match the content of the
	 * given {@link Set}. The created {@link JaroWinklerSimilarity} is not backed by
	 * the {@link Set}, so it will not reflect changes of the {@link Set}.
	 * 
	 * @param terms
	 *            {@link Set} of matched and returned terms.
	 * @return A {@link JaroWinklerSimilarity} instance to match the content of the
	 *         given {@link Set}.
	 * 
	 * @author Jan Martin Keil
	 * @since 0.1
	 */
	public static JaroWinklerSimilarity<String> of(Collection<String> terms) {
		return new JaroWinklerSimilarity<String>(new LinkedNodeTrieSet(terms));
	}

	private int equalInRange(boolean[] array, boolean expected, int lowerBound, int upperBound) {
		int result = 0;
		for (int i = lowerBound; i <= upperBound; i++) {
			if (array[i] == expected) {
				result++;
			}
		}
		return result;
	}

	/**
	 * @param commonCharacters
	 *            characters in common in pair of strings
	 * @param length1
	 *            length of first string
	 * @param length2
	 *            length of second string
	 * @param halfTranspositions
	 *            number of half transpositions
	 * @return
	 */
	private static double jaroSimilarity(double commonCharacters, int length1, int length2, double halfTranspositions) {
		if (commonCharacters > 0) {
			return (commonCharacters * commonCharacters * 2 / length1
					+ commonCharacters * commonCharacters * 2 / length2 + commonCharacters * 2 - halfTranspositions)
					/ (3 * commonCharacters * 2);
		} else {
			return 0;
		}
	}

	private static double jaroWinklerSimilarity(double commonCharacters, int length1, int length2,
			double halfTranspositions, int commonPrefixLength) {

		double jaroSimilarity = jaroSimilarity(commonCharacters, length1, length2, halfTranspositions);

		if (jaroSimilarity >= BOOST_THRESHOLD) {
			return jaroSimilarity + commonPrefixLength * BOOST_FACTOR * (1 - jaroSimilarity);
		} else {
			return jaroSimilarity;
		}
	}

	/**
	 * Matches {@link Collection} of {@link String}s against the terms of this
	 * {@link JaroWinklerSimilarity} instance.
	 * 
	 * <b>Note:</b> This is a shortcut for the repeated call of
	 * {@link #match(String, double)}, but does not provide performance advantages.
	 * 
	 * @param queries
	 *            {@link Collection} of {@link String}s that will be compared to the
	 *            terms to calculate the similarity.
	 * @param threshold
	 *            Minimum similarity of matching terms.
	 * @return {@link Map} of the queries and the corresponding results consisting
	 *         of a {@link Map} of the matching values and their ranking.
	 * 
	 * @see {@link #match(String, double)}
	 * 
	 * @author Jan Martin Keil
	 * @since 0.1
	 */
	public Map<String, Map<T, Double>> match(Collection<String> queries, double threshold) {
		Map<String, Map<T, Double>> results = new HashMap<String, Map<T, Double>>();
		for (String string : queries) {
			results.put(string, match(string, threshold));
		}
		return results;
	}

	/**
	 * Matches a {@link String} against the terms of this
	 * {@link JaroWinklerSimilarity} instance.
	 * 
	 * @param query
	 *            {@link String} that will be compared to the terms to calculate the
	 *            similarity.
	 * @param threshold
	 *            Minimum similarity of matching terms.
	 * @return {@link Map} of the matching values and their ranking.
	 * 
	 * @see {@link #match(Collection, double)}
	 * 
	 * @author Jan Martin Keil
	 * @since 0.1
	 */
	public Map<T, Double> match(String query, double threshold) {
		// initialize result
		Map<T, Double> results = new HashMap<T, Double>();

		int queryLength = query.length(); // length of first string

		// iterate possible lengths of string2
		for (Integer termTargetLength : this.trie.containedLengths()) {
			// calculate window size for common characters
			int windowSize = windowSize(queryLength, termTargetLength);
			// max value of l = the size of the emphasized first few characters
			int maxCommonPrefixSize = Math.min(COMMON_PREFIX_LENGTH_LIMIT, Math.min(queryLength, termTargetLength));
			// recursive traverse of the trie to get matching strings of length2
			this.match(this.trie, threshold, query, queryLength, termTargetLength, windowSize, 0 // minCommonCharacters
					, 0 // minHalfTranspositions
					, maxCommonPrefixSize, 0 // saveCommonCharsQuery
					, new boolean[queryLength] // assignedQuery
					, new boolean[termTargetLength] // assignedTerm
					, new char[Math.min(queryLength, termTargetLength)] // commonCharsTerm
					, results);
		}
		return results;
	}

	/**
	 * @param threshold
	 *            Minimum similarity of matching terms.
	 * @param termTrie
	 *            Current node of the term trie to process.
	 * @param query
	 *            Characters of the query string.
	 * @param termString
	 *            second string
	 * @param queryLength
	 *            Length of the query.
	 * @param termTargetLength
	 *            Total length of the term to process.
	 * @param windowSize
	 *            Window size to search for common characters.
	 * @param minCommonCharacters
	 *            Min number of characters in common in term and query.
	 * @param minHalfTranspositions
	 *            Min number of half transpositions
	 * @param maxCommonPrefixSize
	 *            Max number of characters in common in pair of strings in
	 *            emphasized beginning.
	 * @param saveCommonCharsQuery
	 *            Assigned characters of the query whose predecessors are already
	 *            outside of the matching window.
	 * @param assignedQuery
	 *            array of booleans stating which characters of first string have
	 *            been assigned (TRUE = assigned)
	 * @param assignedTerm
	 *            array of booleans stating which characters of second string have
	 *            been assigned (TRUE = assigned)
	 * @param commonCharsTerm
	 *            Assigned characters of the term.
	 */
	private void match(Trie<T> termTrie, double threshold, String query, int queryLength, int termTargetLength,
			int windowSize, int minCommonCharacters, int minHalfTranspositions, int maxCommonPrefixSize,
			int saveCommonCharsQuery, boolean[] assignedQuery, boolean[] assignedTerm, char[] commonCharsTerm,
			Map<T, Double> results) {

		if (termTrie.containsLength(termTargetLength)) {
			// current branch contains string of target length

			// get current position on term string
			final int termCurrentNodeDepth = termTrie.depth();
			final int termCurrentNodeLength = termTrie.keyLength();

			// iterate new characters
			for (int termCurrentLength = termCurrentNodeDepth
					+ 1; termCurrentLength <= termCurrentNodeLength; termCurrentLength++) {
				// get character at current position
				final char currentTermChar = termTrie.symbol().charAt(termCurrentLength - 1 - termCurrentNodeDepth);

				// get window on query string
				/**
				 * First character of query that can still become assigned.
				 */
				final int assignableQueryWindowLowerBoundIndex = Math.max(termCurrentLength - 1 - windowSize, 0);
				/**
				 * Last character of query that can still become assigned by the current
				 * character of term.
				 */
				final int assignableQueryCurrentWindowUpperBoundIndex = Math.min(termCurrentLength + windowSize,
						queryLength) - 1;

				// update maxCommonPrefixSize
				if (termCurrentLength <= maxCommonPrefixSize && query.charAt(termCurrentLength - 1) != currentTermChar)
				// currently in the prefix and characters at current position
				// do not match
				{
					// reduce maxCommonPrefixSize to current depth
					maxCommonPrefixSize = termCurrentLength - 1;
				}

				// search matching char for current term char in window
				for (int i = assignableQueryWindowLowerBoundIndex; i <= assignableQueryCurrentWindowUpperBoundIndex; i++) {
					if (!assignedQuery[i] && query.charAt(i) == currentTermChar) {
						// unassigned common character was found

						assignedQuery[i] = true;
						assignedTerm[termCurrentLength - 1] = true;
						commonCharsTerm[minCommonCharacters] = currentTermChar;
						minCommonCharacters++;
						break;
					}
				}

				// update minHalfTranspositions
				if (windowSize < termCurrentLength && termCurrentLength - windowSize <= queryLength) {
					// window lower bound inside of query string
					if (assignedQuery[assignableQueryWindowLowerBoundIndex]) {
						// character at window lower bound is assigned
						if (query.charAt(
								assignableQueryWindowLowerBoundIndex) != commonCharsTerm[saveCommonCharsQuery]) {
							// common characters at last save position not equal
							minHalfTranspositions++;
						}
						saveCommonCharsQuery++;
					}
				}
			}

			// get window bounds
			int assignableQueryWindowLowerBoundIndex = Math.max(termCurrentNodeLength - 1 - windowSize, 0);
			/**
			 * Last character of query that can still become assigned by any character of
			 * term.
			 */
			int assignableQueryTotalWindowUpperBoundIndex = Math.min(termTargetLength + windowSize, queryLength) - 1;
			// get number of characters that can still become assigned
			int assignableQuery = equalInRange(assignedQuery, false, assignableQueryWindowLowerBoundIndex,
					assignableQueryTotalWindowUpperBoundIndex);
			/**
			 * Number of characters of term that can still become assigned after processing
			 * current character.
			 */
			int assignableTerm = termTargetLength - termCurrentNodeLength;
			// get maximum number of common characters
			double maxCommonCharacters = Math.min(assignableQuery, assignableTerm) + minCommonCharacters;

			// get remaining half transpositions
			if (termCurrentNodeLength == termTargetLength) {
				// termString has been completed

				// iterate assignments not covered by minHalfTransposition yet
				for (int i = Math.max(termCurrentNodeLength - windowSize,
						0); i <= assignableQueryTotalWindowUpperBoundIndex; i++) {

					if (assignedQuery[i]) {
						// position is assigned

						if (query.charAt(i) != commonCharsTerm[saveCommonCharsQuery]) {
							// common characters at current position not equal

							minHalfTranspositions++;
						}
						saveCommonCharsQuery++;
					}
				}
			}

			// calculate max similarity
			double maxSimilarity = jaroWinklerSimilarity(maxCommonCharacters, queryLength, termTargetLength,
					minHalfTranspositions, maxCommonPrefixSize);

			// check against threshold
			if (maxSimilarity >= threshold) {
				// threshold is meet
				if (termTargetLength == termCurrentNodeLength) {
					// current node has target depth

					if (termTrie.isPopulated()) {
						// current node is contained
						// add object of current node to results
						results.put(termTrie.value(), maxSimilarity);
					}
				} else {
					// iterate children
					Iterator<? extends Trie<T>> children = termTrie.childrenIterator();

					while (children.hasNext()) {

						boolean[] termAssignedCopy = Arrays.copyOf(assignedTerm, termTargetLength);
						boolean[] queryAssignedCopy = Arrays.copyOf(assignedQuery, queryLength);

						Trie<T> child = children.next();

						// traverse child
						match(child, threshold, query, queryLength, termTargetLength, windowSize, minCommonCharacters,
								minHalfTranspositions, maxCommonPrefixSize, saveCommonCharsQuery, queryAssignedCopy,
								termAssignedCopy, commonCharsTerm, results);
					}
				}
			}
		}
	}

	private static int windowSize(int length1, int length2) {
		return Math.max(0, Math.max(length1, length2) / 2 - 1);
	}
}
