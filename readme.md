# Akka-neo-fun

Just playing around with akka, spray and neo4j to try and record relationships between users and dois they like.

## Prerequisites

sbt 13
scala
neo4j 2.0

## To do

- Automate creation of indexes
echo '{"property_keys":["doi"]}' | http post http://localhost:7474/db/data/schema/index/document
echo '{"property_keys":["name"]}' | http post http://localhost:7474/db/data/schema/index/person

- Write it again using tdd :p



http://docs.neo4j.org/chunked/milestone/rest-api-unique-indexes.html

MATCH (docs)-[:tweeted]-()-[:tweeted]-(basedoc) WHERE basedoc.name="a" RETURN docs, COUNT(docs) ORDER BY COUNT(docs) DESC

echo '{"property_keys":["doi"]}' | http post http://localhost:7474/db/data/schema/index/document