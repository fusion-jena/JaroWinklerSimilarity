package de.uni_jena.cs.fusion.similarity.jarowinkler;

/*-
 * #%L
 * Jaro Winkler Similarity
 * %%
 * Copyright (C) 2018 - 2023 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

/**
 * Benchmark of the {@link JaroWinklerSimilarity} that measures throughput and GC metrics.
 */
public class JaroWinklerSimilarityBenchmark {
    private static final String DATASET_PATH = "dataset1/dbpedia_2016-10_persondata_en_names_unique_sorted.gz";
    private static final int QUERIES_SAMPLE_SIZE = 1000;
    private static final double SIMILARITY_THRESHOLD = 0.95;

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(JaroWinklerSimilarityBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    @Warmup(iterations = 3)
    @Measurement(iterations = 3)
    public void benchmark(Blackhole bh, BenchmarkState state) {
        for (String query : state.queriesSample) {
            bh.consume(state.jaroWinklerSimilarity.apply(query));
        }
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private JaroWinklerSimilarity jaroWinklerSimilarity;
        private List<String> queriesSample;

        @Setup
        public void setup() {
            List<String> dataset = loadDataset();
            this.jaroWinklerSimilarity = JaroWinklerSimilarity.with(dataset, SIMILARITY_THRESHOLD);
            this.queriesSample = IntStream.range(0, dataset.size() / QUERIES_SAMPLE_SIZE)
                    .mapToObj(i -> dataset.get(i * QUERIES_SAMPLE_SIZE))
                    .collect(Collectors.toList());
        }

        private List<String> loadDataset() {
            try (GZIPInputStream gis = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream(DATASET_PATH));
                 InputStreamReader isr = new InputStreamReader(gis, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {

                String line = br.readLine();
                List<String> dataset = new ArrayList<>();
                while (line != null) {
                    dataset.add(line);
                    line = br.readLine();
                }

                return dataset;
            } catch (IOException ex) {
                throw new RuntimeException("failed to load dataset", ex);
            }
        }
    }

}
