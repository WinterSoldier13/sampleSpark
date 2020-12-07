package com.activeMQ

import org.apache.spark.sql.SparkSession

object prog
{
    val spark: SparkSession = SparkSession
        .builder()
        .appName("CSVReader")
        .master("local[*]")
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("OFF")
    import spark.implicits._
    
    def main(args : Array[String]): Unit =
    {
        val df = spark
            .readStream
            .format("org.apache.bahir.sql.streaming.mqtt.MQTTStreamSourceProvider")
            .option("brokerUrl","tcp://localhost:1883")
            .option("topic","sample_topic2")
            .load()
        
        df.printSchema()
    
        df
            .writeStream
            .outputMode("append")
            .format("console")
            .start
            .awaitTermination()
    }
}
