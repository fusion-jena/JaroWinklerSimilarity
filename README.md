[![Build Status](https://travis-ci.org/fusion-jena/JaroWinklerSimilarity.svg?branch=master)](https://travis-ci.org/fusion-jena/JaroWinklerSimilarity)
[![Javadocs](https://javadoc.io/badge/de.uni_jena.cs.fusion/similarity.jarowinkler.svg)](https://javadoc.io/doc/de.uni_jena.cs.fusion/similarity.jarowinkler)
[![Maven Central](https://img.shields.io/maven-central/v/de.uni_jena.cs.fusion/similarity.jarowinkler.svg?label=Maven%20Central)](https://search.maven.org/artifact/de.uni_jena.cs.fusion/similarity.jarowinkler/)

# Jaro Winkler Similarity

This is a Java implementation of the Jaro Winkler Similarity, which is optimized for the search of similar strings in a large set of strings.

## Usage

Binaries are available on the central Maven repositories.

Simple example:

```java
List<String> terms = Arrays.asList("hello world","goodbye world");
JaroWinklerSimilarity<String> jws = JaroWinklerSimilarity.with(terms, 0.95);
Map<String, Double> match = jws.apply("hello word");
System.out.println(match);
```

This returns:

```
{hello world=0.9818181818181819}
```

## Acknowledgments
The development of this Jaro Winkler Similarity implementation was funded by DFG in the scope of the LakeBase project within the Scientific Library Services and Information Systems (LIS) program.
