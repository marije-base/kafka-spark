Kafka and Spark Streaming 
=============================================

This is a R&D project that combines Kafka with Spark Streaming using Docker and Docker Compose.

The base for this project was taken from [this post](http://blog.antlypls.com/blog/2017/10/15/using-spark-sql-and-spark-streaming-together/).

How to Run
----------

Build fat jar: `sbt assembly`.

Run `sudo docker-compose run --rm --service-ports java`.

In the `java` container terminal run:

```
KAFKA_BROKERS=kafka:9092 \
KAFKA_GROUP_ID=kafka-spark-streaming \
KAFKA_TOPIC=words \
spark-2.2.0-bin-hadoop2.7/bin/spark-submit \
  --master local[*] \
  --class nl.marije.kafkaspark.WordCountApplication kafka-spark-streaming.jar
```

In a separate terminal (let's call that the 'kafka-terminal') run

```
sudo docker exec -it $(sudo docker-compose ps -q kafka) kafka-console-producer.sh --broker-list localhost:9092 --topic words
```

In the 'kafka-terminal' paste JSON events like:

```
{"word":"create","timestamp":"2017-10-05T23:01:17Z"}
{"word":"update","timestamp":"2017-10-05T23:01:19Z"}
{"word":"update","timestamp":"2017-10-05T23:02:51Z"}
```

Some usefull sites I used as resources during the R&D process: 

[spark-streaming and spark-sql using kafka](http://blog.antlypls.com/blog/2017/10/15/using-spark-sql-and-spark-streaming-together/)

[reading twitter messages into kafka](https://github.com/saurzcode/twitter-stream/blob/master/src/main/java/com/saurzcode/twitter/TwitterKafkaProducer.java)