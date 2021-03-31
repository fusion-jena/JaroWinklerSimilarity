# creates a list of English person names from the 2016-10 DBpedia dump from https://wiki.dbpedia.org/downloads-2016-10

# load dump | decompress | filter person names | sort & eliminate duplicates
wget -qO- http://downloads.dbpedia.org/2016-10/core-i18n/en/persondata_en.ttl.bz2 | bzip2 -dkc | grep -oPi '(?<=<http:\/\/xmlns\.com\/foaf\/0\.1\/name> ").*(?=")' | sort -u > dbpedia_2016-10_persondata_en_names_unique_sorted
# remove final linebreak
truncate -s -1 dbpedia_2016-10_persondata_en_names_unique_sorted
# compress
gzip dbpedia_2016-10_persondata_en_names_unique_sorted
