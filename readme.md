# Akka-neo-fun

Just playing around with akka, spray and neo4j to try and record relationships between users and dois they like.

## Prerequisites

sbt 13
scala
neo4j 2.0

## To do

- Make dois and people unique, dont reinsert.
    - http://docs.neo4j.org/chunked/milestone/rest-api-cypher.html#rest-api-create-a-node
    - Probably have to use cypher for this

- some queries, e.g. given a DOI find other DOIs liked.

- Write it again using tdd :p



http://docs.neo4j.org/chunked/milestone/rest-api-unique-indexes.html

http post http://localhost:7474/db/data/node/0/relationships to=http://localh
