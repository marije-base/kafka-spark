Kafka and Spark Streaming 
=============================================

This is a R&D project that combines Kafka with Spark Streaming using Docker and Docker Compose.

The base for this project was taken from [this post](http://blog.antlypls.com/blog/2017/10/15/using-spark-sql-and-spark-streaming-together/).

How to Run
----------

Build fat jar: `sbt assembly`.

Run `sudo docker-compose run --rm --service-ports spark`.

In the `spark` container terminal run:

```
KAFKA_BROKERS=kafka:9092 \
KAFKA_GROUP_ID=kafka-spark-streaming \
KAFKA_TOPIC=words \
spark-submit \
  --master local[*] \
  --class nl.marije.kafkaspark.WordCountApplication kafka-spark-streaming.jar
```


Some usefull sites I used as resources during the R&D process: 

[spark-streaming and spark-sql using kafka](http://blog.antlypls.com/blog/2017/10/15/using-spark-sql-and-spark-streaming-together/)

[reading twitter messages into kafka](https://github.com/saurzcode/twitter-stream/blob/master/src/main/java/com/saurzcode/twitter/TwitterKafkaProducer.java)

You can use the apache/nifi Docker container found here as a starting point, and use a Docker RUN/COPY command to inject your desired flow. There are three ways to load an existing flow into a NiFi instance.

Export the flow as a template (an XML file containing the exported flow segment) and import it as a template into your running Nifi instance. This requires the "destination" NiFi instance to be running and uses the NiFi API.
Create the flow you want, manually extract the entire flow from the "source" NiFi instance by copying $NIFI_HOME/conf/flow.xml.gz, and overwrite the flow.xml.gz file in the "destination" NiFi's conf directory. This does not require the destination NiFi instance to be running, but it must occur before the destination NiFi starts.
Use the NiFi Registry to version control the original flow segment from the source NiFi and make it available to the destination NiFi. This seems like overkill for your scenario.
I would recommend Option 2, as you should have the desired flow as you want it. Simply use COPY /src/flow.xml.gz /destination/flow.xml.gz in your Dockerfile.

If you literally want it to "run my template every time", you probably want to ensure that the processors are all in enabled state (showing a "Play" icon) when you copy/save off the flow.xml.gz file, and that in your nifi.properties, nifi.flowcontroller.autoResumeState=true.

docker cp wordcountsbt_nifi_run_1:/opt/nifi/nifi-1.6.0/conf/flow.xml.gz ~/Documents/kafkatry/kafka-spark