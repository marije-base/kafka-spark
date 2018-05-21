Kafka and Spark Streaming 
=============================================

This is a R&D project that combines Kafka with Spark Streaming using Docker and Docker Compose.

The base for the streaming spark project was taken from [this post](http://blog.antlypls.com/blog/2017/10/15/using-spark-sql-and-spark-streaming-together/).

##### The Data pipeline created in this R&D project contains:
1. Apache NiFi: To load the twitter-feed and publish it to the Kafka Pub/Sub system
2. Apache Kafka: To create a distributed Pub/Sub Streaming platform
3. Spark Streaming: To consume the data from the Kafka Topic and perform a little transformation

A small presentation including the challenge and the solution is provided in the root of the repository.

How to Run
----------

##### Build fat jar: `sbt assembly`.

If you have problem with the build of the spark-streaming application you might want to try: 
`export SBT_OPTS="-XX:+CMSClassUnloadingEnabled -XX:PermSize=512M -XX:MaxPermSize=1024M"`


##### Build the docker-containers required for the pipeline

run `sudo docker-compose build`.
The configuration for this build are in the root of the project ./docker-compose.yml

The NiFi container for loading the twitter-feed into the application requires a flow-configuration to be present in the conf-folder. (/conf/nifi) This file however contains the encrypted passwords for the twitter-application and is therefore ignored from the git-pushes. Remove the 'COPY' part from the dockerfile and add you own processors in the GUI (localhost:9092/nifi) to start the application without this flow-configuration automatically being added.

##### Run The spark Node and all other required Docker Containers
run `sudo docker-compose run --rm --service-ports spark`.

This will besides the spark-container, also start the docker-containers 'called' kafka and nifi.


##### Run the Spark Application
To run the actual data-processing service in the `spark` container terminal run:

```
KAFKA_BROKERS=kafka:9092 \
KAFKA_GROUP_ID=kafka-spark-streaming \
KAFKA_TOPIC=twitter-topics \
spark-submit \
  --master local[*] \
  --class nl.marije.kafkaspark.TagCountApplication kafka-spark-streaming.jar
```

