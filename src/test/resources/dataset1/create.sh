###
# #%L
# Jaro Winkler Similarity
# %%
# Copyright (C) 2018 - 2022 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
###
# creates a list of English person names from the 2016-10 DBpedia dump from https://wiki.dbpedia.org/downloads-2016-10

# load dump | decompress | filter person names | sort & eliminate duplicates
wget -qO- http://downloads.dbpedia.org/2016-10/core-i18n/en/persondata_en.ttl.bz2 | bzip2 -dkc | grep -oPi '(?<=<http:\/\/xmlns\.com\/foaf\/0\.1\/name> ").*(?=")' | sort -u > dbpedia_2016-10_persondata_en_names_unique_sorted
# remove final linebreak
truncate -s -1 dbpedia_2016-10_persondata_en_names_unique_sorted
# compress
gzip dbpedia_2016-10_persondata_en_names_unique_sorted
