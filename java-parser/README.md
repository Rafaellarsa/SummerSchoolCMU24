
# Java Parser

This is our concept parser for java code. The repo has been recenlty updated in 2019 to support a json format that is accessible via a NodeJS service.

## Maven

This project uses maven and can be compiled using

```
mvn compile
```

To package it as a jar use the following command:

```
mvn clean compile assembly:single
```

## Docker support

### JSON

Support for docker has been included for both db and json support. Please keep in mind that performance will likely be improved if you run java natively on your os. To use docker for json, you need to first run. 

```
docker-compose build
```

This copies the current files into a container (thus any modifications require a build to update). Once done you will want to run the container using:

```
docker-compose up -d
```

The container will then be used to execute java from NodeJS by using docker-compose exec java

### DB

The original DB format is still supported and now includes docker support. Docker will launch three containers, including a mysql and phpmyadmin. You'll need to launch them with a seperate .yml file

```
docker-compose -f docker-compose.db.yml up -d
```

After they are launched, you will need to import parser.sql to the db using phpmyadmin (localhost:5555)by default. Root password is set in the compose file (e.g example).

You can then run your commands using

```
docker-compose exec java [commands, e.g ls]
```

or shell in using

```
docker-compose exec java bash
```

Please note source files cannot be outside this folder.


## Node and JSON

### Node Server
You can run the server use 

```
node javaParser.js
```

There are the following options:

- `-docker`: Use the running docker container for parsing
- `-port`: Use the following port to run the server

### JSON
If you desire JSON you should only interact with the node server. This can be done with a post request to /parse_java. You'll want to send json in the body as an array of objects. Each object should have the properties `id` and `code`.


## DB Format

### Input Format

The code snippets should be stored in a single txt file.  See input.txt in the repository that shows an input file for three code snippets with ids: #C1, #C2, #C3

Note that the each code snippet starts with a unique identifier that must start with "#C".
"EOF" should be added to the end of the file.


### MySQL

The output of the parser is stored in a mysql table: parser.ent_content_concept
You need to import the "parser.sql" in your local MySQL server.
Below are the description of the fields in parser table:

id: is a unique incremental identifier  (you can ignore it)
content_id: is the identifier for each code (e.g., #C1, #C2, ...)
concept: is the name of the concept
sline: is the line that the concept appeared in the code 
eline: is the same as sline when the concept is not referring to a block. Otherwise, an eline refers to the end of the block.

### How to Run the Parser

Type the following command in the terminal after you replaced A1, A2, A3, A4 with your own arguments:
```
A1: pathToInputFile or string;    A2: ExportType (json or db) A3: mysqlUser;      A4: mysqlPass;       A5: mysqlHostPort
```

docker
```
docker-compose exec java java -jar target/java-parser.jar input.txt db root example db:3306
```

reg
```
java -jar java-parser.jar A1 A2 A3 A4
e.g.: java -jar java-parser.jar /Users/roya/Desktop/input.txt db user root localhost:3306
```


## How to Cite The Parser

Hosseini, R., & Brusilovsky, P. (2013). Javaparser: A fine-grain concept indexing tool for java problems. In The First Workshop on AI-supported Education for Computer Science (AIEDCS 2013) (pp. 60-63). University of Pittsburgh.

## Authors

- Roya Hosseini
- Zak Risha (JSON + maintainer)
