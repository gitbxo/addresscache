
# Description
Address Server to host and lookup addresses

This allows saving and updating Addresses and searching for Addresses


# Instructions to run the simulation
Note: Must install maven and JDK15 to build and run the program
```mvn clean spring-boot:run```

Load the data using python3:
```python3 src/simulator/simulator.py src/simulator/addresses.json```
This also supports commands to get, create, update, search and delete.
```python3 src/simulator/simulator.py src/simulator/commands.json```


# Configuration
File ```src/main/resources/application.yml``` has parameters to
configure the following:

## Port numbers
Main server port is server.port
Management port is management.server.port

## Allow duplicates
Allow duplicates is ```org.bxo.address.allow-dups```
When allow-dups is false, duplicate addresses will not be loaded.
Note: Addresses must be exactly the same to be duplicates.

## Default max results
Default max results is ```org.bxo.address.default-max-results```
This is the max number of results for a search.
Searches may return more results if found at the same level.


# API
## GET /address
```GET /address``` returns the address if it exists
Accepts url param ```addressId```

## POST /address
```POST /address``` creates a new address
Accepts url params ```line1```, ```line2```, ```city```, ```state```,
```zip```
Param line2 is optional,  all other params are required.
Leading and trailing whitespaces are removed from all params.
Line1 and City must contain at least one non-whitespace character.
State must be exactly two characters.
Zip must be exactly five digits.
When allow-dups is false, duplicate addresses will be ignored.
Addresses must be exactly the same to be duplicates.

## PUT /address
```PUT /address``` updates the address if it exists
Accepts url params ```addressId```, ```line1```, ```line2```,
```city```, ```state```, ```zip```
Param addressId is required, all others are optional.
Leading and trailing whitespaces are removed from all params.
When allow-dups is false, address may not match an existing address.
Addresses must be exactly the same to be duplicates.
Note: Only provided parameters are updated.
When no values are updated, this will set line2 to blank.

## DELETE /address
```DELETE /address```deletes the address if it exists
Accepts url param ```addressId```

## GET /search
```GET /search``` returns the list of addresses found
Accepts url params ```query```, ```maxResults```, ```exactMatch```,
```requireAll```
This will search for addresses matching any of the words in query.
All searches are case-insensitive, even when exactMatch is set.
Consecutive alpha-numeric characters are considered a word.
All other characters are treated as whitespace and ignored.
Prefixes are matched when exactMatch is false, so "Mass" will match
"Massachussetts".
The query "1600 Mass" matches "100 Massachussetts" and "1600 Holloway"
When requireAll is true, the address must contain all words.
After finding maxResults, the search will stop.  However, it will
include other addresses found at the same level.

## GET /stats
```GET /stats``` returns request statistics


# Design

## AddressController
AddressController maintains the external API.
It also maintains the statistics for the counts of requests, status
and timing in milliseconds.

## AddressInfo
Address object and validation methods

## SearchNode
SearchNode maintains the search structure.
A node contains a word and the id of addresses containing the exact
word, ignoring case.
Child nodes have one more letter than the parent node.
The root node is the empty string.
Nodes may not have any addresses when children have addresses.
Children with no addresses are removed.
This uses a heuristic by counting when children add entries.
When search results for children return no data, that will also result
in removing the child.

## AddressService
AddressService handles address creation and requests.
It maintains the addresses and keeps a pointer to the root SearchNode.
The addresses are kept in a ConcurrentHashMap.

## Concurrency and Multithreading
The code handles concurrency and multithreading by using the java
concurrent objects.
To prevent issues when iterating over maps, the keys or values are put
in a list, which is then used for iterating.
To handle potential concurrency issues when updating the SearchNode
tree, those sections are put in synchronized blocks.
Counters using AtomicLong and corresponding utilities to support multithreading.


# Testing
Run command ```mvn test```
