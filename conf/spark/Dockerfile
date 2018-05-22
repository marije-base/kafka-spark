FROM java:openjdk-8-jdk

ENV SPARK_VERSION 2.2.0

# Get Spark from US Apache mirror.
RUN mkdir -p /opt && \
    cd /opt && \
    curl http://www.us.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop2.7.tgz | \
        tar -zx && \
    ln -s spark-${SPARK_VERSION}-bin-hadoop2.7 spark && \
    echo Spark ${SPARK_VERSION} installed in /opt


COPY ./log4j.properties /opt/spark/conf/log4j.properties
ENV PATH $PATH:/opt/spark/bin