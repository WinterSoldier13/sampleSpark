package com.test.kafka

import org.apache.spark.sql.SparkSession

case class kafkaSchema(value: String, partition: Int)

object testKafka
{
    def main(args : Array[String]) : Unit = {
    
        val spark = SparkSession
            .builder()
            .appName("testKafka")
            .master("local[*]")
            .config("spark.streaming.receiver.writeAheadLog.enable", true)
            .getOrCreate()
        println("<><><><><><><><><><><><><><><><><><><><><>>><><><><><><><><><><><><><><><><><><><><><><><><>>><><><><><><><><><><><><><><><><><><><><><><><><>>><><><>")
        import spark.implicits._
        
        val df = spark
            .readStream
            .format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("subscribe", "sample_topic")
            .option("startingoffsets", "latest")
            .load()
    
        df.printSchema()
    
//        root
//        |-- key: binary (nullable = true)
//        |-- value: binary (nullable = true)
//        |-- topic: string (nullable = true)
//        |-- partition: integer (nullable = true)
//        |-- offset: long (nullable = true)
//        |-- timestamp: timestamp (nullable = true)
//        |-- timestampType: integer (nullable = true)
    
        df
            .writeStream
            .outputMode("append")
            .format("csv")
            .option("checkpointLocation", "/home/wintersoldier/Desktop/checkpoint")
            .option("path","/home/wintersoldier/Documents/tookitaki/sparkTest/src/main/scala/kafka_out/outCSV")
            .start()
            .awaitTermination()
        spark.close()
    }
}
