[![Build Status](https://travis-ci.org/fusion-jena/JaroWinklerSimilarity.svg?branch=master)](https://travis-ci.org/fusion-jena/JaroWinklerSimilarity)
[![Javadocs](https://javadoc.io/badge/de.uni_jena.cs.fusion/similarity.jarowinkler.svg)](https://javadoc.io/doc/de.uni_jena.cs.fusion/similarity.jarowinkler)
[![Maven Central](https://img.shields.io/maven-central/v/de.uni_jena.cs.fusion/similarity.jarowinkler.svg?label=Maven%20Central)](https://search.maven.org/artifact/de.uni_jena.cs.fusion/similarity.jarowinkler/)

# Jaro Winkler Similarity

This is a Java implementation of the Jaro Winkler Similarity, which is optimized for the search of similar strings in a large set of strings.

## Usage

Binaries are available on the central Maven repositories:

```
<dependency>
    <groupId>de.uni_jena.cs.fusion</groupId>
    <artifactId>similarity.jarowinkler</artifactId>
    <version>1.0.1</version>
</dependency>
```

Usage example for similar string search:

```java
// prepare some Strings
List<String> terms = Arrays.asList("hello world", "hello universe");
// prepare the JaroWinklerSimilarity instance
JaroWinklerSimilarity<String> jws = JaroWinklerSimilarity.with(terms, 0.95);
// search similar strings
Map<String, Double> similarStrings = jws.apply("hello word"); // please note the missing "l"
// results
assert similarStrings.get("hello world").equals(0.9818181818181819d);
assert !similarStrings.containsKey("hello universe");
```

Usage example for object search:

```java
// prepare some data
Map<String, Collection<Integer>> programs = new HashMap<String, Collection<Integer>>();
Collection<Integer> list = new ArrayList<>();
Collection<Integer> set = new HashSet<>();
programs.put("hello world", list);
programs.put("hello universe", set);
// prepare the JaroWinklerSimilarity instance
JaroWinklerSimilarity<Collection<Integer>> jws = JaroWinklerSimilarity.with(programs, 0.95);
// search matching data
Map<Collection<Integer>, Double> searchResult = jws.apply("hello word"); // please note the missing "l"
// results
assert searchResult.get(list).equals(0.9818181818181819d);
assert !searchResult.containsKey(set);
```

## Publication
In case you use this implementation for your scientific work, please consider to cite the related paper:

*Keil, Jan Martin (2019). **Efficient Bounded Jaro-Winkler Similarity Based Search**. In:  Grust, Torsten et al. (Eds.): Datenbanksysteme für Business, Technologie und Web (BTW 2019), Lecture Notes in Informatics (LNI), Gesellschaft für Informatik, Bonn. (pp. 207-216). DOI:[10.18420/btw2019-13](https://doi.org/10.18420/btw2019-13).*

## Acknowledgments
The development of this Jaro Winkler Similarity implementation was funded by DFG in the scope of the LakeBase project within the Scientific Library Services and Information Systems (LIS) program.
