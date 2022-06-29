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
import java.util.function.Function;

/**
 * <p>
 * {@link Function} to calculate the Jaro Winkler similarity of a given
 * {@link String} to the value {@link String}s of a {@link Collection} or the
 * key {@link String}s of a {@link Map} and return the ranked values with a Jaro
 * Winkler similarity meeting a threshold.
 * </p>
 * <p>
 * <b>Acknowledgments:</b> The development of this Jaro Winkler Similarity
 * implementation was funded by DFG in the scope of the LakeBase project within
 * the Scientific Library Services and Information Systems (LIS) program.
 * </p>
 * 
 * @param <T> Type of the returned ranked values
 * 
 * @author Jan Martin Keil
 * @since 0.1
 */
public class JaroWinklerSimilarity<T> implements Function<String, Map<T, Double>> {
	public final static int COMMON_PREFIX_LENGTH_LIMIT = 4;
	public final static double BOOST_THRESHOLD = 0.7;
	public final static double BOOST_FACTOR = 0.1;

	private static int equalInRange(boolean[] array, boolean expected, int lowerBound, int upperBound) {
		int result = 0;
		for (int i = lowerBound; i <= upperBound; i++) {
			if (array[i] == expected) {
				result++;
			}
		}
		return result;
	}

	/**
	 * @param commonCharacters   characters in common in pair of strings
	 * @param length1            length of first string
	 * @param length2            length of second string
	 * @param halfTranspositions number of half transpositions
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
	 * @param threshold             Minimum similarity of matching terms.
	 * @param termTrie              Current node of the term trie to process.
	 * @param query                 Characters of the query string.
	 * @param queryLength           Length of the query.
	 * @param termTargetLength      Total length of the term to process.
	 * @param windowSize            Window size to search for common characters.
	 * @param minCommonCharacters   Min number of characters in common in term and
	 *                              query.
	 * @param minHalfTranspositions Min number of half transpositions
	 * @param maxCommonPrefixSize   Max number of characters in common in pair of
	 *                              strings in emphasized beginning.
	 * @param saveCommonCharsQuery  Assigned characters of the query whose
	 *                              predecessors are already outside of the matching
	 *                              window.
	 * @param assignedQuery         array of booleans stating which characters of
	 *                              first string have been assigned (TRUE =
	 *                              assigned)
	 * @param assignedTerm          array of booleans stating which characters of
	 *                              second string have been assigned (TRUE =
	 *                              assigned)
	 * @param commonCharsTerm       Assigned characters of the term.
	 */
	private static <R> void match(Trie<R> termTrie, double threshold, String query, int queryLength,
			int termTargetLength, int windowSize, int minCommonCharacters, int minHalfTranspositions,
			int maxCommonPrefixSize, int saveCommonCharsQuery, boolean[] assignedQuery, boolean[] assignedTerm,
			char[] commonCharsTerm, Map<R, Double> results) {

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
						results.merge(termTrie.value(), maxSimilarity, Math::max);
					}
				} else {
					// iterate children
					Iterator<? extends Trie<R>> children = termTrie.childrenIterator();

					while (children.hasNext()) {

						boolean[] termAssignedCopy = Arrays.copyOf(assignedTerm, termTargetLength);
						boolean[] queryAssignedCopy = Arrays.copyOf(assignedQuery, queryLength);

						Trie<R> child = children.next();

						// traverse child
						match(child, threshold, query, queryLength, termTargetLength, windowSize, minCommonCharacters,
								minHalfTranspositions, maxCommonPrefixSize, saveCommonCharsQuery, queryAssignedCopy,
								termAssignedCopy, commonCharsTerm, results);
					}
				}
			}
		}
	}

	/**
	 * Returns the Jaro Winkler similarity of two given {@link String}s.
	 * 
	 * <b>Note:</b> Use {@link #apply(String)} to calculate the similarity of one
	 * {@link String} to many {@link String}s for performance reasons.
	 * 
	 * @param first     First {@link String} to match.
	 * @param second    Second {@link String} to match.
	 * @param threshold Minimum similarity of the strings.
	 * @return Jaro Winkler similarity of {@code first} and {@code second} or
	 *         {@code null} if they do not meet the threshold.
	 * 
	 * @since 1.0
	 */
	public static Double of(String first, String second, double threshold) {
		// initialize result
		Map<String, Double> results = new HashMap<>();
		// get lengths
		int firstLength = first.length();
		int secondLength = second.length();
		// calculate window size for common characters
		int windowSize = windowSize(secondLength, firstLength);
		// max value of l = the size of the emphasized first few characters
		int maxCommonPrefixSize = Math.min(COMMON_PREFIX_LENGTH_LIMIT, Math.min(secondLength, firstLength));
		// recursive traverse of the trie to get matching strings of length2
		match(Tries.singletonTrieSet(first), threshold, second, secondLength, firstLength, windowSize, 0 // minCommonCharacters
				, 0 // minHalfTranspositions
				, maxCommonPrefixSize, 0 // saveCommonCharsQuery
				, new boolean[secondLength] // assignedQuery
				, new boolean[firstLength] // assignedTerm
				, new char[Math.min(secondLength, firstLength)] // commonCharsTerm
				, results);
		return results.get(first);
	}

	private static int windowSize(int length1, int length2) {
		return Math.max(0, Math.max(length1, length2) / 2 - 1);
	}

	/**
	 * Prepares a {@link JaroWinklerSimilarity} instance to match the content of a
	 * given {@link Collection} considering a given threshold. The created
	 * {@link JaroWinklerSimilarity} is not backed by the {@link Collection}, so it
	 * will not reflect changes of the {@link Collection}.
	 * 
	 * @param terms            {@link Collection} of matched and returned terms.
	 * @param defaultThreshold Default minimum similarity of matching terms.
	 * @return A {@link JaroWinklerSimilarity} instance to match the content of the
	 *         given {@link Collection} considering the given threshold.
	 * 
	 * @since 1.0
	 */
	public static JaroWinklerSimilarity<String> with(Collection<String> terms, double defaultThreshold) {
		return new JaroWinklerSimilarity<String>(new TrieSet(terms), defaultThreshold);
	}

	/**
	 * 
	 * Prepares a {@link JaroWinklerSimilarity} instance to match the content of a
	 * given {@link Map} considering a given threshold. The matching will search for
	 * similar keys, but return the corresponding values. The created
	 * {@link JaroWinklerSimilarity} is not backed by the {@link Map}, so it will
	 * not reflect changes of the {@link Map}.
	 * 
	 * @param terms            {@link Map} of matched terms and returned values.
	 * @param defaultThreshold Default minimum similarity of matching terms.
	 * @return A {@link JaroWinklerSimilarity} instance to match the content of the
	 *         given {@link Map} considering the given threshold.
	 * 
	 * @param <T> Type of the map values and returned values by the matching.
	 * 
	 * @since 1.0
	 */
	public static <T> JaroWinklerSimilarity<T> with(Map<String, T> terms, double defaultThreshold) {
		return new JaroWinklerSimilarity<T>(new TrieMap<T>(terms), defaultThreshold);
	}

	private final Trie<T> trie;

	private double defaultThreshold;

	private JaroWinklerSimilarity(Trie<T> trie, double defaultThreshold) {
		this.trie = trie;
		this.defaultThreshold = defaultThreshold;
	}

	/**
	 * Matches a {@link String} against the terms of this
	 * {@link JaroWinklerSimilarity} instance, considering a given threshold.
	 * 
	 * @param query     {@link String} that will be compared to the terms to
	 *                  calculate the similarity.
	 * @param threshold Minimum similarity of matching terms.
	 * @return {@link Map} of the matching values and their ranking.
	 * 
	 * @since 1.1.0
	 */
	public Map<T, Double> apply(String query, double threshold) {
		// initialize result
		Map<T, Double> results = new HashMap<>();

		// get length of query
		int queryLength = query.length();

		// iterate possible lengths of terms
		for (Integer termTargetLength : this.trie.containedLengths()) {
			// calculate window size for common characters
			int windowSize = windowSize(queryLength, termTargetLength);
			// max value of l = the size of the emphasized first few characters
			int maxCommonPrefixSize = Math.min(COMMON_PREFIX_LENGTH_LIMIT, Math.min(queryLength, termTargetLength));
			// recursive traverse of the trie to get matching strings of length2
			match(this.trie, threshold, query, queryLength, termTargetLength, windowSize, 0 // minCommonCharacters
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
	 * Matches a {@link String} against the terms of this
	 * {@link JaroWinklerSimilarity} instance using the default threshold.
	 *
	 * @param query {@link String} that will be compared to the terms to calculate
	 *              the similarity.
	 * @return {@link Map} of the matching values and their ranking.
	 *
	 * @since 1.0
	 */
	@Override
	public Map<T, Double> apply(String query) {
		return apply(query, defaultThreshold);
	}

	/**
	 * Changes the default threshold.
	 * 
	 * @param defaultThreshold Default minimum similarity of matching terms.
	 */
	public void setThreshold(double defaultThreshold) {
		this.defaultThreshold = defaultThreshold;
	}
}
