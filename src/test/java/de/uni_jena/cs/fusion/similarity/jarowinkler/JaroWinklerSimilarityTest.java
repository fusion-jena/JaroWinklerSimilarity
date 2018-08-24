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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

public class JaroWinklerSimilarityTest {

	@Test
	public void apply() {
		SortedMap<String, String> terms;
		for (Map<String, Map<String, Double>> testCase : testData()) {
			terms = new TreeMap<String, String>();
			for (Map<String, Double> queryResults : testCase.values()) {
				for (String term : queryResults.keySet()) {
					terms.put(term, term);
				}
			}
			for (Entry<String, Map<String, Double>> queryResults : testCase.entrySet()) {
				Map<String, Double> result = JaroWinklerSimilarity.with(terms, 0.5).apply(queryResults.getKey());
				for (Entry<String, Double> expected : queryResults.getValue().entrySet()) {
					String caseDescription = "Query: \"" + queryResults.getKey() + "\", Terms: " + terms.keySet()
							+ ", Result: \"" + expected.getKey() + "\"";
					assertEquals(caseDescription, expected.getValue(), result.get(expected.getKey()), 0.01);
				}
			}
		}
	}

	@Test
	public void of() {
		SortedMap<String, String> terms;
		for (Map<String, Map<String, Double>> testCase : testData()) {
			terms = new TreeMap<String, String>();
			for (Map<String, Double> queryResults : testCase.values()) {
				for (String term : queryResults.keySet()) {
					terms.put(term, term);
				}
			}
			for (Entry<String, Map<String, Double>> queryResults : testCase.entrySet()) {
				for (Entry<String, Double> expected : queryResults.getValue().entrySet()) {
					Double result = JaroWinklerSimilarity.of(expected.getKey(),
							queryResults.getKey(), 0.5);
					String caseDescription = "First: \"" + queryResults.getKey() + "\", Second: " + terms.keySet();
					assertEquals(caseDescription, expected.getValue(), result, 0.01);
				}
			}
		}
	}

	/**
	 * @return Cases causing problems in earlier versions or other implementations
	 *         and some extreme cases.
	 */
	private static Collection<Map<String, Map<String, Double>>> testData() {
		Collection<Map<String, Map<String, Double>>> testData = new ArrayList<Map<String, Map<String, Double>>>();
		Map<String, Map<String, Double>> testCase;
		Map<String, Double> queryResult;

		///////////////////////////////
		testCase = new HashMap<String, Map<String, Double>>();
		testData.add(testCase);
		queryResult = new HashMap<String, Double>();
		testCase.put("aaaaaa", queryResult);
		queryResult.put("aaaaaa", 1.00);
		queryResult.put("aaaaba", 0.93);
		queryResult = new HashMap<String, Double>();
		testCase.put("aaaaba", queryResult);
		queryResult.put("aaaaba", 1.00);
		queryResult.put("aaaaaa", 0.93);

		///////////////////////////////
		testCase = new HashMap<String, Map<String, Double>>();
		testData.add(testCase);
		queryResult = new HashMap<String, Double>();
		testCase.put("Ronald Alexander", queryResult);
		queryResult.put("Roland Alexander", 0.983);
		queryResult.put("Ronald Alexander", 1.00);
		queryResult = new HashMap<String, Double>();
		testCase.put("Roland Alexander", queryResult);
		queryResult.put("Ronald Alexander", 0.983);
		queryResult.put("Roland Alexander", 1.00);

		///////////////////////////////
		testCase = new HashMap<String, Map<String, Double>>();
		testData.add(testCase);
		queryResult = new HashMap<String, Double>();
		testCase.put("a", queryResult);
		queryResult.put("a", 1.00);

		///////////////////////////////
		testCase = new HashMap<String, Map<String, Double>>();
		testData.add(testCase);
		queryResult = new HashMap<String, Double>();
		testCase.put("ab", queryResult);
		queryResult.put("ab", 1.00);

		///////////////////////////////
		testCase = new HashMap<String, Map<String, Double>>();
		testData.add(testCase);
		queryResult = new HashMap<String, Double>();
		testCase.put("Duke of Brunswick-Harburg Otto II", queryResult);
		queryResult.put("Duke of Brunswick-Harburg Otto II", 1.00);
		queryResult.put("Duke of Brunswick-Harburg Otto III", 0.99);
		queryResult = new HashMap<String, Double>();
		testCase.put("Duke of Brunswick-Harburg Otto III", queryResult);
		queryResult.put("Duke of Brunswick-Harburg Otto III", 1.00);
		queryResult.put("Duke of Brunswick-Harburg Otto II", 0.99);

		///////////////////////////////
		testCase = new HashMap<String, Map<String, Double>>();
		testData.add(testCase);
		queryResult = new HashMap<String, Double>();
		testCase.put("Andre Lichtenberger", queryResult);
		queryResult.put("Andre Lichtenberger", 1.00);
		queryResult.put("Andrew Lichtenberger", 0.99);
		queryResult = new HashMap<String, Double>();
		testCase.put("Andrew Lichtenberger", queryResult);
		queryResult.put("Andrew Lichtenberger", 1.00);
		queryResult.put("Andre Lichtenberger", 0.99);

		///////////////////////////////
		testCase = new HashMap<String, Map<String, Double>>();
		testData.add(testCase);
		queryResult = new HashMap<String, Double>();
		testCase.put("Ida Bauer", queryResult);
		queryResult.put("Ida Bauer", 1.00);
		queryResult = new HashMap<String, Double>();
		testCase.put("Ihor Korotetskyi", queryResult);
		queryResult.put("Ihor Korotetskyi", 1.00);
		queryResult = new HashMap<String, Double>();
		testCase.put("Li Du", queryResult);
		queryResult.put("Li Du", 1.00);
		queryResult = new HashMap<String, Double>();
		testCase.put("Liping Ji", queryResult);
		queryResult.put("Liping Ji", 1.00);

		///////////////////////////////
		testCase = new HashMap<String, Map<String, Double>>();
		testData.add(testCase);
		queryResult = new HashMap<String, Double>();
		testCase.put("Colleen D'Agostino", queryResult);
		queryResult.put("Raymond Bark-Jones", 0.50);

		return testData;
	}

}
