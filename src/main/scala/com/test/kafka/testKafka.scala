package com.test.kafka

import org.apache.spark.sql.SparkSession

object testKafka
{
    def main(args : Array[String]) : Unit = {
    
        val spark = SparkSession
            .builder()
            .appName("testKafka")
            .master("local[*]")
            .getOrCreate()
        println("<><><><><><><><><><><><><><><><><><><><><>>><><><><><><><><><><><><><><><><><><><><><><><><>>><><><><><><><><><><><><><><><><><><><><><><><><>>><><><>")
        
        val df = spark
            .readStream
            .format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("subscribe", "sampleTopic")
            .load()
        
        
    }
}
