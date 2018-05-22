package nl.marije.kafkaspark

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.count
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StringType, StructType, TimestampType, LongType}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.Try
import java.text.SimpleDateFormat


//The base for the streaming spark project was taken from [this post](http://blog.antlypls.com/blog/2017/10/15/using-spark-sql-and-spark-streaming-together/).
object TagCountApplication {
	def main(args: Array[String]): Unit = {
		// Configurations for kafka consumer
		val kafkaBrokers = sys.env.get("KAFKA_BROKERS")
		val kafkaGroupId = sys.env.get("KAFKA_GROUP_ID")
		val kafkaTopic = sys.env.get("KAFKA_TOPIC")

		// Verify that all settings are set
		require(kafkaBrokers.isDefined, "KAFKA_BROKERS has not been set")
		require(kafkaGroupId.isDefined, "KAFKA_GROUP_ID has not been set")
		require(kafkaTopic.isDefined, "KAFKA_TOPIC has not been set")

		// Create Spark Session
		val spark = SparkSession
				.builder()
				.appName("WordCountApplication")
				.getOrCreate()

		import spark.implicits._


		// Create Streaming Context and Kafka Direct Stream with provided settings and 10 seconds batches
		val ssc = new StreamingContext(spark.sparkContext, Seconds(10))

		val kafkaParams = Map[String, Object](
			"bootstrap.servers" -> kafkaBrokers.get,
			"key.deserializer" -> classOf[StringDeserializer],
			"value.deserializer" -> classOf[StringDeserializer],
			"group.id" -> kafkaGroupId.get,
			"auto.offset.reset" -> "latest"
		)

		val topics = Array(kafkaTopic.get)
		val stream = KafkaUtils.createDirectStream[String, String](
			ssc,
			PreferConsistent,
			Subscribe[String, String](topics, kafkaParams)
		)

		// example json from twitter-data
		//
		//    {
		//      "created_at":"Thu Apr 06 15:24:15 +0000 2017",
		//      "id": 850006245121695744,
		//      "id_str": "850006245121695744",
		//      "text": "1/ Today we’re sharing our vision for the future of the Twitter API platform!nhttps://t.co/XweGngmxlP",
		//      "user": {},
		//      "entities": {}
		//    }

		val schema = new StructType()
				.add("created_at", StringType)
				.add("id", LongType)
				.add("id_str", StringType)
				.add("text", StringType)

		/**
		  * Find the number of tags per tweet
		  * @param text the text-body of the tweet
		  * @return number of tags (as an integer, 0 if no tags are found)
		  */
		def detectNumberOfTags(text: String): Int = {

			Try {
				val tags = text.split(" ").filter(_.startsWith("#"))
				tags.length
			}.getOrElse(0)
		}

		val udf_detectNumberOfTags = udf(detectNumberOfTags _)

		/**
		  * Transform the date to the correct format
		  * @param dateAsString
		  * @return dateAsString in the format 'yyy-MM-dd HH:mm'(or 'unknown' if no date is found)
		  */
		def getDate(dateAsString: String): String = {
			Try {

				val TWITTER: String = "EEE MMM dd HH:mm:ss ZZZZZ yyyy"
				val REQUIREDFORMAT: String = "yyyy-MM-dd HH:mm"
				val sf: SimpleDateFormat = new SimpleDateFormat(TWITTER)
				val format: SimpleDateFormat = new SimpleDateFormat(REQUIREDFORMAT)


				sf.setLenient(true)
				val dateAsDate = sf.parse(dateAsString)

				format.format(dateAsDate)

			}.getOrElse("unknown")
		}

		val udf_getDate = udf(getDate _)


		// Process batches:
		// Parse JSON and create Data Frame
		// Execute computation on that Data Frame and print result
		stream.foreachRDD { (rdd, time) =>
			val data = rdd.map(record => record.value)
			val json = spark.read.schema(schema).json(data)
			val enhencedjson = json.withColumn("number_of_tags", udf_detectNumberOfTags($"text"))
			val enhencedjson2 = enhencedjson.withColumn("date", udf_getDate($"created_at"))
			val result = enhencedjson2.filter($"date" !== "unknown").groupBy($"date", $"number_of_tags").agg(count("*").alias("count")).orderBy($"date", $"number_of_tags")

			result.show
			result.rdd.map(x => x(0) + "," + x(1) + "," + x(2)).saveAsTextFile("/user/spark/numberoftags.csv")

		}

		// Start Stream
		ssc.start()

		// Make sure it doesn't only run once
		ssc.awaitTermination()
	}


}
