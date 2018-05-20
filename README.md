# kafka-spark

RUN /opt/spark/bin/spark-submit \
  --master local[*] \
  --class nl.marije.kafkaspark.WordCountApplication ../data-processing-service/build/kafka-spark-streaming.jar