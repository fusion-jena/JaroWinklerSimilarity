package de.uni_jena.cs.fusion.similarity.jarowinkler;

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
