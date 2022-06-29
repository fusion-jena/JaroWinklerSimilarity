package de.uni_jena.cs.fusion.similarity.jarowinkler;

/*-
 * #%L
 * Jaro Winkler Similarity
 * %%
 * Copyright (C) 2018 - 2022 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
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

import java.lang.management.ManagementFactory;
import java.util.function.Supplier;

public class MemoryConsumption {

	public static long of(Supplier<?> supplierUnderTest) {
		long memoryBefore, memoryAfter = 0;
		// warm up call to not measure e.g. ClassLoader footprint
		Object warmUpObject = supplierUnderTest.get();
		Runtime.getRuntime().gc();
		memoryBefore = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		// measured call
		Object measuredObject = supplierUnderTest.get();
		Runtime.getRuntime().gc();
		memoryAfter = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		// use objects to avoid garbage collection
		System.out.print(warmUpObject.getClass().toString().substring(0, 0));
		System.out.print(measuredObject.getClass().toString().substring(0, 0));
		return memoryAfter - memoryBefore;
	}
}
