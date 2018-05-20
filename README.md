Kafka and Spark Streaming 
=============================================

This is a R&D project that combines Kafka with Spark Streaming using Docker and Docker Compose.

The base for this project was taken from [this post](http://blog.antlypls.com/blog/2017/10/15/using-spark-sql-and-spark-streaming-together/).

How to Run
----------

Build fat jar: `sbt assembly`.

Run `sudo docker-compose build`.
The NiFi container for loading the twitter-feed into the application requires a flow-congiruation to be present in the conf-folder. (/conf/nifi) This file however contains the encrypted passwords for the twitter-application and is therefore ignored from the git-pushes.

Run `sudo docker-compose run --rm --service-ports spark`.
This will besides the spark-container, also start the docker-containers 'called' kafka and nifi.


To run the actual data-processing service in the `spark` container terminal run:

```
KAFKA_BROKERS=kafka:9092 \
KAFKA_GROUP_ID=kafka-spark-streaming \
KAFKA_TOPIC=words \
spark-submit \
  --master local[*] \
  --class nl.marije.kafkaspark.WordCountApplication kafka-spark-streaming.jar
```