package com.test.kafka

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

object testKafka
{
    def main(args : Array[String]) : Unit = {

        val spark = SparkSession
            .builder()
            .appName("testKafka")
            .master("local")
            .getOrCreate()
        spark.sparkContext.setLogLevel("OFF")
        import spark.implicits._

        val df = spark
            .readStream
            .format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("subscribe", "sample_topic")
            .load()
            
        df.printSchema()
    
        df
            .writeStream
            .outputMode("append")
            .format("com.databricks.spark.csv")
            .option("checkpointLocation", "/home/wintersoldier/Desktop/checkpoint")
            .option("path","/home/wintersoldier/Documents/tookitaki/sparkTest/src/main/scala/kafka_out/outCSV")
            .start()
            .awaitTermination()
        
//        df
//            .writeStream
//            .outputMode("append")
//            .format("console")
//            .start
//            .awaitTermination()
        
        
        spark.stop()
        spark.close()
    }
}

