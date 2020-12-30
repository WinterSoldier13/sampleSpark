package com.testVersion

import com.databricks.spark.xml.functions.from_xml
import com.databricks.spark.xml.schema_of_xml
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.streaming.{Seconds, StreamingContext}



object usingCustomRec {
    val spark: SparkSession = SparkSession
        .builder()
        .appName("sampleApp")
        .master("local[*]")
        .getOrCreate()
    
//    spark.sparkContext.setLogLevel("ERROR")
    
    import spark.implicits._
    
    def main(args: Array[String]): Unit = {
        
        val brokerUrl_ : String = "tcp://localhost:61616"
        val topicName_ : String = "sample_topic"
        val username_ : String = "username"
        val password_ : String = "password"
        val path2XML_ : String = "src/main/static/dataset/smallXML.xml"
        val tempTable_ : String = "tempTable"
        val parqOutPath: String = "/home/wintersoldier/Documents/tookitaki/sparkTest/src/main/static/output/fromSparkXML"
        
        //    Format of dataframe:
        //        id : integer, topic: String, payload : binary, timestamp : timestamp
        val df = spark
            .readStream
            .format("com.wintersoldier.jms")
            .option("connection", "activemq")
            .option("brokerUrl", brokerUrl_)
            .option("queue", topicName_)
//            .option("persistence", "memory")
//            .option("cleanSession", "false")
            .option("username", username_)
            .option("password", password_)
            .option("acknowledge", "true")
//            .option("localStorage", "/home/wintersoldier/Desktop/tempS")
            .option("clientId", "ayush")
            .load()
        
        
//        val payload_ = df.select("payload")
//            .as[Array[Byte]]
//            .map(payload => {
//                new String(payload)
//            })
//            .toDF("payload")
        
        df.writeStream
            .outputMode("append")
            .format("console")
            .start
            .awaitTermination()
        
        spark.close()
        spark.stop()
    
    
        
        
    }
    
}
